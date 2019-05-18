package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrent;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;

public class DfgCutFinderSimple extends DfgCutFinderCombination {

	public DfgCutFinderSimple() {
		super(new CutFinderIMExclusiveChoice(), 
				new CutFinderIMSequence(), 
				new CutFinderIMConcurrent(),
				new CutFinderIMLoop());
	}
}
