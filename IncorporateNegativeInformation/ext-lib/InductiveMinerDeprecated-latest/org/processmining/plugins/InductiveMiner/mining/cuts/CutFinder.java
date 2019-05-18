package org.processmining.plugins.InductiveMiner.mining.cuts;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public interface CutFinder {

	/**
	 * Returns a cut, or null if none found.
	 * 
	 * Must be thread-safe and static, i.e, no side-effects allowed.
	 * 
	 * @param log
	 * @param logInfo
	 * @param minerState
	 * @return
	 */
	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState);

}
