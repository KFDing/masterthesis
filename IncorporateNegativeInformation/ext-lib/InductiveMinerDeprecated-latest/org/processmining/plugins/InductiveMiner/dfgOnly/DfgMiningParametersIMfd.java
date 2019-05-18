package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderNoiseFiltering;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;

public class DfgMiningParametersIMfd extends DfgMiningParameters {
	public DfgMiningParametersIMfd() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));

		setDfgCutFinder(new DfgCutFinderCombination(
				new DfgCutFinderSimple(),
				new DfgCutFinderNoiseFiltering()
				));

		setDfgFallThroughs(new DfgFallThroughCombination());

		setDfgSplitter(
				new SimpleDfgSplitter()
				);

		setNoiseThreshold(0.2f);
	}
}