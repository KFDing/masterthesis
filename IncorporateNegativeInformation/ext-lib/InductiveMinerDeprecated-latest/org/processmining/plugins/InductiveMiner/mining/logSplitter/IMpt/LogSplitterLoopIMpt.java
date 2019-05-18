package org.processmining.plugins.InductiveMiner.mining.logSplitter.IMpt;

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
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

public class LogSplitterLoopIMpt implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		assert (log instanceof IMLogStartEndComplete);
		return new LogSplitResult(split((IMLogStartEndComplete) log, cut.getPartition(), minerState),
				new MultiSet<XEventClass>());
	}

	public static List<IMLog> split(IMLogStartEndComplete log, Collection<Set<XEventClass>> partition,
			MinerState minerState) {

		//System.out.println("==before==");
		//System.out.print(log);
		//System.out.println("--");
		List<IMLog> result = new ArrayList<>();
		boolean firstSigma = true;
		//walk through the partition
		for (Set<XEventClass> sigma : partition) {
			IMLogStartEndComplete sublog = log.clone();
			//			System.out.println("sigma " + sigma);

			//walk through traces
			for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {
				IMTrace trace = itTrace.next();

				//System.out.println(" trace " + trace + " sigma " + sigma);

				if (minerState.isCancelled()) {
					return null;
				}

				boolean lastIn = firstSigma; //whether the last seen event was in sigma
				boolean anyIn = false; //whether there is any event in this subtrace
				MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

				boolean lastEventRemoved = false;

				//walk through the events
				for (IMEventIterator itEvent = trace.iterator(); itEvent.hasNext();) {
					XEvent event = itEvent.next();
					XEventClass eventClass = log.classify(trace, event);
					Transition transition = log.getLifeCycle(event);
					lastEventRemoved = false;
					
					//System.out.println(" trace " + trace + " sigma " + sigma);

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

							IMTrace oldTrace = itEvent.split();
							//System.out.println("   split trace " + oldTrace + " # " + trace);
							
							sublog.setStartComplete(trace.getIMTraceIndex(), true);
							sublog.setEndComplete(oldTrace.getIMTraceIndex(), true);

						}
						lastIn = true;
						anyIn = true;

					} else {
						//event of another sigma

						//remove
						itEvent.remove();
						lastEventRemoved = true;

						//the last seen event was not in sigma
						if (openActivityInstances.isEmpty()) {
							//if there are no activity instances open, we can split the trace further ahead
							lastIn = false;
						}
					}
				}

				//check if we are not introducing an empty trace
				if (!anyIn && !firstSigma) {
					itTrace.remove();
				}

				if (lastEventRemoved) {
					sublog.setEndComplete(trace.getIMTraceIndex(), true);
				}
			}
			firstSigma = false;
			//System.out.println("--");
			//System.out.print(sublog);
			//System.out.println("--");
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
