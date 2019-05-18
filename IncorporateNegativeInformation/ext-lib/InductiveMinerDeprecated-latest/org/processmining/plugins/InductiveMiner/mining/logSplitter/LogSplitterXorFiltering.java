package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

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

public class LogSplitterXorFiltering implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return split(log, cut.getPartition(), minerState);
	}

	public static LogSplitResult split(IMLog log, Collection<Set<XEventClass>> partition, MinerState minerState) {

		//map activities to sigmas
		TObjectIntHashMap<XEventClass> eventclass2sigmaIndex = new TObjectIntHashMap<>();
		TIntObjectHashMap<Set<XEventClass>> sigmaIndex2sigma = new TIntObjectHashMap<>();
		{
			int p = 0;
			for (Set<XEventClass> sigma : partition) {
				sigmaIndex2sigma.put(p, sigma);
				for (XEventClass activity : sigma) {
					eventclass2sigmaIndex.put(activity, p);
				}
				p++;
			}
		}

		MultiSet<XEventClass> noise = new MultiSet<>();

		List<IMLog> result = new ArrayList<>();
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = log.clone();
			for (Iterator<IMTrace> it = sublog.iterator(); it.hasNext();) {

				if (minerState.isCancelled()) {
					return null;
				}

				IMTrace trace = it.next();

				//walk through the events and count how many go in each sigma
				int[] sigmaEventCounters = new int[partition.size()];
				int maxCounter = 0;
				Set<XEventClass> maxSigma = null;
				for (XEvent event : trace) {
					int sigmaIndex = eventclass2sigmaIndex.get(log.classify(trace, event));
					sigmaEventCounters[sigmaIndex]++;
					if (sigmaEventCounters[sigmaIndex] > maxCounter) {
						maxCounter = sigmaEventCounters[sigmaIndex];
						maxSigma = sigmaIndex2sigma.get(sigmaIndex);
					}
				}

				//determine whether this trace should go in this sublog
				if (trace.isEmpty()) {
					/*
					 * An empty trace should have been filtered out before
					 * reaching here. We have no information what trace could
					 * have produced it, so we keep it in all sublogs.
					 */
				} else if (maxSigma != sigma) {
					//remove trace
					it.remove();
				} else {
					//keep trace, remove all events not from sigma
					for (Iterator<XEvent> it2 = trace.iterator(); it2.hasNext();) {
						XEvent e = it2.next();
						XEventClass c = sublog.classify(trace, e);
						if (!sigma.contains(c)) {
							it2.remove();
						} else {
							noise.add(c);
						}
					}
				}
			}
			result.add(sublog.decoupleFromXLog());
		}

		return new LogSplitResult(result, noise);
	}
}
