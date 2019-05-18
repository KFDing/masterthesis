package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;

public class DfgCutFinderNoiseFiltering implements DfgCutFinder {

	private static DfgCutFinder cutFinder = new DfgCutFinderSimple();

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		Dfg filteredDfg = filterDfg(dfg, minerState);
		return cutFinder.findCut(filteredDfg, minerState);
	}

	/**
	 * Filter the dfg as in IMi.
	 * 
	 * @param dfg
	 * @param minerState
	 * @return
	 */
	public static Dfg filterDfg(Dfg dfg, DfgMinerState minerState) {
		//filter the Dfg
		float threshold = minerState.getParameters().getNoiseThreshold();

		return CutFinderIMf.filterNoise(dfg, threshold);
	}

}
