package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;

public class DfgMiningParametersIMd extends DfgMiningParameters {
	public DfgMiningParametersIMd() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));

		setDfgCutFinder(new DfgCutFinderSimple());

		setDfgFallThroughs(new DfgFallThroughCombination());

		setDfgSplitter(
				new SimpleDfgSplitter()
				);
	}
}