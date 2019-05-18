package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class DfgCutFinderCombination implements DfgCutFinder {
	private final DfgCutFinder[] cutFinders;

	public DfgCutFinderCombination(DfgCutFinder... cutFinders) {
		this.cutFinders = cutFinders;
	}

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		for (int i = 0; i < cutFinders.length; i++) {
			Cut c = cutFinders[i].findCut(dfg, minerState);
			if (c != null && c.isValid()) {
				return c;
			}
		}
		return null;
	}
}
