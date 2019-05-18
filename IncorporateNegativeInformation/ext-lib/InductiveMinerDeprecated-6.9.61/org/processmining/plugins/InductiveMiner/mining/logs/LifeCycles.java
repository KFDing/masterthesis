package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;

public class LifeCycles {

	private int eventsRemoved = 0;
	private int eventsAdded = 0;
	private boolean debug = false;

	public LifeCycles(boolean debug) {
		this.debug = debug;
	}

	public IMLog preProcessLog(IMLog log) {
		return new IMLogImpl(preProcessLog(log.toXLog(), log.getClassifier(), log.getLifeCycleClassifier()),
				log.getClassifier(), log.getLifeCycleClassifier());
	}

	public XLog preProcessLog(XLog log, XEventClassifier classifier, XLifeCycleClassifier lifeCycleClassifier) {
		XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());

		eventsRemoved = 0;
		eventsAdded = 0;

		for (XTrace trace : log) {
			XTrace newTrace = preProcessTraceByAddingCompleteEvents(trace, classifier, lifeCycleClassifier);
			if (newTrace != null) {
				result.add(newTrace);
			}
		}

		if (debug) {
			System.out.println("events added:   " + eventsAdded
					+ " (unmatched start events after which a completion was inserted)");
			System.out.println("events removed: " + eventsRemoved + " (other life cycles)");
		}

		return result;
	}

	/**
	 * 
	 * @param trace
	 * @return a copy of the input trace, such that it is consistent.
	 */
	public XTrace preProcessTraceByAddingCompleteEvents(XTrace trace, XEventClassifier classifier,
			XLifeCycleClassifier lifeCycleClassifier) {
		Map<String, TIntArrayList> unmatchedStartEvents = getUnmatchedStartEvents(trace, classifier,
				lifeCycleClassifier);

		//repair the trace
		XTrace result = new XTraceImpl(trace.getAttributes());
		int i = 0;
		for (XEvent event : trace) {

			if (lifeCycleClassifier.getLifeCycleTransition(event) == Transition.complete) {
				//copy to the new trace
				result.add(event);
			} else if (lifeCycleClassifier.getLifeCycleTransition(event) == Transition.start) {
				//copy to the new trace
				result.add(event);

				String activity = classifier.getClassIdentity(event);
				if (unmatchedStartEvents.containsKey(activity) && unmatchedStartEvents.get(activity).contains(i)) {
					//this start event is not matched; add a corresponding complete event

					XAttributeMap map = (XAttributeMap) event.getAttributes().clone();

					//add life cycle transition
					map.put(XLifecycleExtension.KEY_TRANSITION,
							XFactoryRegistry.instance().currentDefault().createAttributeLiteral(
									XLifecycleExtension.KEY_TRANSITION, "complete", XLifecycleExtension.instance()));

					//remove time stamp
					map.remove(XTimeExtension.KEY_TIMESTAMP);

					XEvent newEvent = new XEventImpl(map);
					result.add(newEvent);
					eventsAdded++;
				}
			} else {
				//non-completion non-start event, do not copy to the new trace
				eventsRemoved++;
			}

			i++;
		}

		return result;
	}

	/**
	 * 
	 * @param trace
	 * @param classifier
	 * @return the unmatched start event indices
	 */
	public static Map<String, TIntArrayList> getUnmatchedStartEvents(Iterable<XEvent> trace,
			XEventClassifier classifier, XLifeCycleClassifier lifeCycleClassifier) {
		Map<String, TIntArrayList> unmatchedStartEvents = new THashMap<>();

		int i = 0;
		for (XEvent event : trace) {
			String activity = classifier.getClassIdentity(event);

			if (lifeCycleClassifier.getLifeCycleTransition(event) == Transition.complete) {
				//this is a completion event; check whether there's an open activity instance
				if (unmatchedStartEvents.containsKey(activity)) {
					//there was an open activity instance; close it

					unmatchedStartEvents.get(activity).removeAt(unmatchedStartEvents.get(activity).size() - 1);
					if (unmatchedStartEvents.get(activity).size() == 0) {
						unmatchedStartEvents.remove(activity);
					}
				} else {
					//there was no open activity; skip
				}
			} else if (lifeCycleClassifier.getLifeCycleTransition(event) == Transition.start) {
				//this is a start event; open an activity instance

				if (!unmatchedStartEvents.containsKey(activity)) {
					unmatchedStartEvents.put(activity, new TIntArrayList());
				}
				unmatchedStartEvents.get(activity).add(i);
			} else {
				//ignore this event as it is neither start nor complete
			}

			i++;
		}
		return unmatchedStartEvents;
	}
}
