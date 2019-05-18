package org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class IMLog2IMLogInfoStartEndComplete implements IMLog2IMLogInfo {

	public IMLogInfo createLogInfo(IMLog log) {
		return log2logInfo(log);
	}

	public static IMLogInfo log2logInfo(IMLog log) {

		if (!(log instanceof IMLogStartEndComplete)) {
			log = IMLogStartEndComplete.fromIMLog(log);
		}

		//initialise, read the log
		Dfg dfg = new DfgImpl();
		MultiSet<XEventClass> activities = new MultiSet<XEventClass>();
		TObjectIntHashMap<XEventClass> minimumSelfDistances = new TObjectIntHashMap<>();
		THashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = new THashMap<XEventClass, MultiSet<XEventClass>>();
		long numberOfEvents = 0;
		long numberOfActivityInstances = 0;

		XEventClass fromEventClass;
		XEventClass toEventClass;

		//walk trough the log
		Map<XEventClass, Integer> eventSeenAt;
		List<XEventClass> readTrace;

		int removedEmptyTraces = 0;
		for (IMTrace trace : log) {
			toEventClass = null;
			fromEventClass = null;

			int traceSize = 0;
			eventSeenAt = new THashMap<XEventClass, Integer>();
			readTrace = new ArrayList<XEventClass>();

			for (XEvent e : trace) {
				XEventClass ec = log.classify(trace, e);
				activities.add(ec);
				dfg.addActivity(ec);

				fromEventClass = toEventClass;
				toEventClass = ec;

				readTrace.add(toEventClass);

				if (eventSeenAt.containsKey(toEventClass)) {
					//we have detected an activity for the second time
					//check whether this is shorter than what we had already seen
					int oldDistance = Integer.MAX_VALUE;
					if (minimumSelfDistances.containsKey(toEventClass)) {
						oldDistance = minimumSelfDistances.get(toEventClass);
					}

					if (!minimumSelfDistances.containsKey(toEventClass)
							|| traceSize - eventSeenAt.get(toEventClass) <= oldDistance) {
						//keep the new minimum self distance
						int newDistance = traceSize - eventSeenAt.get(toEventClass);
						if (oldDistance > newDistance) {
							//we found a shorter minimum self distance, record and restart with a new multiset
							minimumSelfDistances.put(toEventClass, newDistance);

							minimumSelfDistancesBetween.put(toEventClass, new MultiSet<XEventClass>());
						}

						//store the minimum self-distance activities
						MultiSet<XEventClass> mb = minimumSelfDistancesBetween.get(toEventClass);
						mb.addAll(readTrace.subList(eventSeenAt.get(toEventClass) + 1, traceSize));
					}
				}
				eventSeenAt.put(toEventClass, traceSize);
				{
					if (fromEventClass != null) {
						//add edge to directly follows graph
						dfg.addDirectlyFollowsEdge(fromEventClass, toEventClass, 1);
					} else {
						//add edge to start activities

						assert (log instanceof IMLogStartEndComplete);
						if (log instanceof IMLogStartEndComplete
								&& ((IMLogStartEndComplete) log).isStartComplete(trace.getIMTraceIndex())) {
							dfg.addStartActivity(toEventClass, 1);
						}
					}
				}

				traceSize += 1;
			}

			numberOfEvents += trace.size();
			numberOfActivityInstances += trace.size();

			//process end activities
			if (toEventClass != null) {
				assert (log instanceof IMLogStartEndComplete);
				if (log instanceof IMLogStartEndComplete
						&& ((IMLogStartEndComplete) log).isEndComplete(trace.getIMTraceIndex())) {
					dfg.addEndActivity(toEventClass, 1);
				}
			}

			//process empty traces
			if (traceSize == 0) {
				assert (log instanceof IMLogStartEndComplete);
				if (((IMLogStartEndComplete) log).isStartComplete(trace.getIMTraceIndex())
						&& ((IMLogStartEndComplete) log).isEndComplete(trace.getIMTraceIndex())) {
					dfg.addEmptyTraces(1);
				} else {
					removedEmptyTraces++;
				}
			}
		}

		return new IMLogInfo(dfg, activities, minimumSelfDistancesBetween, minimumSelfDistances, numberOfEvents,
				numberOfActivityInstances, log.size() - removedEmptyTraces);
	}

	public boolean useLifeCycle() {
		return false;
	}
}
