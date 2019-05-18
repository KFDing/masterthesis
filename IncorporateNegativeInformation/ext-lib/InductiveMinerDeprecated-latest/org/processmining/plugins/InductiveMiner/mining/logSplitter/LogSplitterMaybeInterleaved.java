package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;

public class LogSplitterMaybeInterleaved implements LogSplitter {
	
	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return new LogSplitResult(split(log, cut.getPartition(), minerState), new MultiSet<XEventClass>());
	}

	public static List<IMLog> split(IMLog log, Collection<Set<XEventClass>> partition, MinerState minerState) {
		List<IMLog> result = new ArrayList<>();
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = log.clone();
			
			if (minerState.isCancelled()) {
				return null;
			}
			
			for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {
				IMTrace trace = itTrace.next();
				if (trace.isEmpty()) {
					itTrace.remove();
				} else {
					//if the start activity does not match: remove trace 
					XEventClass c = sublog.classify(trace, trace.iterator().next());
					if (!sigma.contains(c)) {
						itTrace.remove();
					}
				}
			}
			result.add(sublog);
		}
		return result;
	}
		
}
