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

/**
 * Splits the log by putting each event into its sigma. Empty traces are
 * removed.
 * 
 * @author sleemans
 *
 */
public class LogSplitterXor implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		return new LogSplitResult(split(log, cut.getPartition(), minerState), new MultiSet<XEventClass>());
	}

	public static List<IMLog> split(IMLog log, Collection<Set<XEventClass>> partition, MinerState minerState) {
		List<IMLog> result = new ArrayList<>();
		for (Set<XEventClass> sigma : partition) {
			IMLog sublog = log.clone();
			for (Iterator<IMTrace> itTrace = sublog.iterator(); itTrace.hasNext();) {
				
				if (minerState.isCancelled()) {
					return null;
				}
				
				IMTrace trace = itTrace.next();
				for (Iterator<XEvent> it = trace.iterator(); it.hasNext();) {
					XEventClass c = sublog.classify(trace, it.next());
					if (!sigma.contains(c)) {
						it.remove();
					}
				}
				if (trace.isEmpty()) {
					itTrace.remove();
				}
			}
			result.add(sublog);
		}
		return result;
	}

}
