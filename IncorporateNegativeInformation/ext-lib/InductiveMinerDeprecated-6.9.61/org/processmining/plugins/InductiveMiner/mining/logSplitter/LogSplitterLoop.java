package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

public class LogSplitterLoop implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return new LogSplitResult(split(log, cut.getPartition(), minerState), new MultiSet<XEventClass>());
	}

	public static List<IMLog> split(IMLog log, Collection<Set<XEventClass>> partition, MinerState minerState) {

		//		System.out.println("==before==");
		//		System.out.println(log);
		//		System.out.println("--");
		List<IMLog> result = new ArrayList<>();
		boolean firstSigma = true;
		//walk through the partition
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = log.clone();
//			System.out.println("sigma " + sigma);

			//walk through traces
			for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {
				
				if (minerState.isCancelled()) {
					return null;
				}
				
				IMTrace trace = itTrace.next();

//				System.out.println(" trace " + trace);
				boolean lastIn = firstSigma; //whether the last seen event was in sigma
				boolean anyIn = false; //whether there is any event in this subtrace
				MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

				//walk through the events
				for (IMEventIterator itEvent = trace.iterator(); itEvent.hasNext();) {
					XEvent event = itEvent.next();
					XEventClass eventClass = log.classify(trace, event);
					Transition transition = log.getLifeCycle(event);

					//keep track of open activity instances (by consistency assumption, should work out)
					switch (transition) {
						case start :
							openActivityInstances.add(eventClass);
							break;
						case complete :
							openActivityInstances.remove(eventClass, 1);
							break;
						case other :
							break;
					}

					if (sigma.contains(log.classify(trace, event))) {
						//event of the sigma under consideration

						if (!lastIn && (firstSigma || anyIn)) {
							//this is the first activity of a new subtrace, so the part up till now is a completed subtrace

							itEvent.split();
//							System.out.println("   split trace " + newTrace + " | " + trace);
						}
						lastIn = true;
						anyIn = true;

					} else {
						//event of another sigma

						//remove
						itEvent.remove();

						//the last seen event was not in sigma
						if (openActivityInstances.isEmpty()) {
							//if there are no activity instances open, we can split the trace further ahead
							lastIn = false;
						}
					}
				}

				if (!firstSigma && !anyIn) {
					itTrace.remove();
				}
			}
			firstSigma = false;
//			System.out.println("--");
//			System.out.println(sublog);
//			System.out.println("--");
			result.add(sublog);
		}

		return result;

		/*
		 * //old (copying) log splitter List<XLog> result = new ArrayList<>();
		 * Map<Set<XEventClass>, XLog> mapSigma2Sublog = new THashMap<>();
		 * Map<XEventClass, Set<XEventClass>> mapActivity2sigma = new
		 * THashMap<>(); for (Set<XEventClass> sigma : partition) { XLog sublog
		 * = new XLogImpl(new XAttributeMapImpl()); result.add(sublog);
		 * mapSigma2Sublog.put(sigma, sublog); for (XEventClass activity :
		 * sigma) { mapActivity2sigma.put(activity, sigma); } }
		 * 
		 * //loop through the traces for (IMTrace trace : log) { XTrace
		 * partialTrace = new XTraceImpl(new XAttributeMapImpl());
		 * 
		 * //keep track of the last sigma we were in Set<XEventClass> lastSigma
		 * = partition.iterator().next();
		 * 
		 * for (XEvent event : trace) { XEventClass c = log.classify(event); if
		 * (!lastSigma.contains(c)) {
		 * mapSigma2Sublog.get(lastSigma).add(partialTrace); partialTrace = new
		 * XTraceImpl(new XAttributeMapImpl()); lastSigma =
		 * mapActivity2sigma.get(c); } partialTrace.add(event); }
		 * mapSigma2Sublog.get(lastSigma).add(partialTrace);
		 * 
		 * //add an empty trace if the last event was not of sigma_1 if
		 * (lastSigma != partition.iterator().next()) {
		 * mapSigma2Sublog.get(lastSigma).add(new XTraceImpl(new
		 * XAttributeMapImpl())); } }
		 * 
		 * //wrap in IMLog objects List<IMLog> result2 = new ArrayList<>(); for
		 * (XLog xLog : result) { result2.add(new IMLog(xLog,
		 * minerState.parameters.getClassifier())); } return result2;
		 */
	}

}
