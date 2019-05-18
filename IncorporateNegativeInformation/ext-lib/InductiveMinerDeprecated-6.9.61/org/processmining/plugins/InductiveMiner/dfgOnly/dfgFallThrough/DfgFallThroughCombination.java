package org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough;

import java.util.ArrayList;

public class DfgFallThroughCombination extends ArrayList<DfgFallThrough> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5018711336090409641L;

	public DfgFallThroughCombination() {
		super.add(new DfgFallThroughStrictTauLoop());
		super.add(new DfgFallThroughFlower());
	}
}
