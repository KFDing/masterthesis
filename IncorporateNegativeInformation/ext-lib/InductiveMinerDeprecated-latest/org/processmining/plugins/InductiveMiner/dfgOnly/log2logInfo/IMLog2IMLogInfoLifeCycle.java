package org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class IMLog2IMLogInfoLifeCycle implements IMLog2IMLogInfo {

	private static class Count {
		long numberOfEvents = 0;
		long numberOfActivityInstances = 0;
		MultiSet<XEventClass> activities = new MultiSet<XEventClass>();
	}

	public IMLogInfo createLogInfo(IMLog log) {
		return log2logInfo(log);
	}

	public static IMLogInfo log2logInfo(IMLog log) {
		Count count = new Count();
		Dfg dfg = log2Dfg(log, count);

		dfg.collapseParallelIntoDirectly();

		TObjectIntHashMap<XEventClass> minimumSelfDistances = new TObjectIntHashMap<>();
		THashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = new THashMap<XEventClass, MultiSet<XEventClass>>();

		return new IMLogInfo(dfg, count.activities, minimumSelfDistancesBetween, minimumSelfDistances,
				count.numberOfEvents, count.numberOfActivityInstances, log.size());
	}

	private static Dfg log2Dfg(IMLog log, Count count) {
		Dfg dfg = new DfgImpl();
		for (IMTrace trace : log) {
			processTrace(log, dfg, trace, count);

			for (XEvent e : trace) {
				if (log.getLifeCycle(e) == Transition.complete) {
					count.numberOfActivityInstances += 1;
				}
			}
			count.numberOfEvents += trace.size();
		}
		return dfg;
	}

	private static void processTrace(IMLog log, Dfg dfg, IMTrace trace, Count count) {
		if (trace.isEmpty()) {
			dfg.addEmptyTraces(1);
			return;
		}

		//directly follows relation
		processDirectlyFollows(log, dfg, trace, count);

		//parallelism
		processParallel(log, dfg, trace);

		//start/end activities
		processStartEnd(log, dfg, trace);
	}

	private static void processStartEnd(IMLog log, Dfg dfg, IMTrace trace) {
		boolean activityOccurrenceCompleted = false;
		MultiSet<XEventClass> activityOccurrencesEndedSinceLastStart = new MultiSet<>();
		MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
		for (XEvent event : trace) {
			XEventClass activity = log.classify(trace, event);

			if (log.getLifeCycle(event) == Transition.start) {
				//start event
				openActivityOccurrences.add(activity);
				if (!activityOccurrenceCompleted) {
					//no activity occurrence has been completed yet. Add to start events.
					dfg.addStartActivity(activity, 1);
				}
				activityOccurrencesEndedSinceLastStart = new MultiSet<>();
			} else if (log.getLifeCycle(event) == Transition.complete) {
				//complete event
				if (openActivityOccurrences.contains(activity)) {
					//this activity occurrence was open; close it
					openActivityOccurrences.remove(activity, 1);
					activityOccurrencesEndedSinceLastStart.add(activity);
				} else {
					//next front is non-started but complete

					if (!activityOccurrenceCompleted) {
						//no activity occurrence has been completed yet. Add to start events.
						dfg.addStartActivity(activity, 1);
					}
					activityOccurrenceCompleted = true;

					activityOccurrencesEndedSinceLastStart = new MultiSet<>();
					activityOccurrencesEndedSinceLastStart.add(activity);
				}
			}

			activityOccurrenceCompleted = activityOccurrenceCompleted || log.getLifeCycle(event) == Transition.complete;
		}
		dfg.addEndActivities(activityOccurrencesEndedSinceLastStart);
	}

	private static void processParallel(IMLog log, Dfg dfg, IMTrace trace) {
		MultiSet<XEventClass> openActivityOccurrences = new MultiSet<XEventClass>();
		for (XEvent event : trace) {
			XEventClass eventClass = log.classify(trace, event);

			if (log.getLifeCycle(event) == Transition.start) {
				//this is a start event
				openActivityOccurrences.add(eventClass);
			} else if (log.getLifeCycle(event) == Transition.complete) {
				//this is a completion event
				openActivityOccurrences.remove(eventClass, 1);

				//this activity occurrence is parallel to all open activity occurrences
				for (XEventClass eventClass2 : openActivityOccurrences) {
					dfg.addParallelEdge(eventClass, eventClass2, openActivityOccurrences.getCardinalityOf(eventClass2));
				}
			}
		}
	}

	private static void processDirectlyFollows(IMLog log, Dfg dfg, IMTrace trace, Count count) {
		IMEventIterator itCurrent = trace.iterator();
		MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

		boolean isStart[] = new boolean[trace.size()];

		int i = 0;
		while (itCurrent.hasNext()) {
			XEvent event = itCurrent.next();
			XEventClass activity = log.classify(trace, event);

			//this is a start event if the log says so, or if we see a complete without corresponding preceding start event. 
			boolean isStartEvent = log.getLifeCycle(event) == Transition.start
					|| !openActivityInstances.contains(activity);
			boolean isCompleteEvent = log.getLifeCycle(event) == Transition.complete;
			isStart[i] = isStartEvent;

			if (isStartEvent) {
				//this is a start event, which means that it could have predecessors
				walkBack(itCurrent, isStart, log, i, dfg, activity);
			}
			if (isCompleteEvent && count != null) {
				//this is a complete event, add it to the activities
				count.activities.add(activity);
			}

			//update the open activity instances
			if (isCompleteEvent && !isStartEvent) {
				//if this ends an activity instance (and it was open already), remove it 
				openActivityInstances.remove(activity);
			}
			if (isStartEvent && !isCompleteEvent) {
				//if this starts an activity instance (and does not immediately close it), it is left open for now
				openActivityInstances.add(activity);
			}
			i++;
		}
	}

	private static void walkBack(IMEventIterator it, boolean[] isStart, IMLog log, int i, Dfg dfg, XEventClass target) {
		it = it.clone();
		MultiSet<XEventClass> completes = new MultiSet<>();
		while (it.hasPrevious()) {
			i--;
			XEvent event = it.previous();
			XEventClass activity = it.classify();

			if (log.getLifeCycle(event) == Transition.complete) {
				completes.add(activity);
				dfg.addDirectlyFollowsEdge(activity, target, 1);
			}
			if (isStart[i] && completes.contains(activity)) {
				return;
			}
		}
	}
	
	public boolean useLifeCycle() {
		return true;
	}

}
