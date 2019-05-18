package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public interface LogSplitter {
	
	public class LogSplitResult {
		public List<IMLog> sublogs;
		public MultiSet<XEventClass> discardedEvents;
		public LogSplitResult(List<IMLog> sublogs, MultiSet<XEventClass> noise) {
			this.sublogs = sublogs;
			this.discardedEvents = noise;
		}
	}
	
	/**
	 * usage: returns a list of sublogs and a multiset of noise events
	 * 
	 * Must be thread-safe and abstract, i.e, no side-effects allowed.
	 * @param log
	 * @param logInfo
	 * @param cut
	 * @param minerState
	 * @return
	 */
	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState);
}
