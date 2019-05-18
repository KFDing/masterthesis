package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public interface DfgCutFinder {

	/**
	 * Returns a cut, or null if none found.
	 * 
	 * Must be thread-safe and static, i.e, no side-effects allowed.
	 * 
	 * @param dfg
	 * @param minerState
	 * @return
	 */
	public Cut findCut(Dfg dfg, DfgMinerState minerState);
}
