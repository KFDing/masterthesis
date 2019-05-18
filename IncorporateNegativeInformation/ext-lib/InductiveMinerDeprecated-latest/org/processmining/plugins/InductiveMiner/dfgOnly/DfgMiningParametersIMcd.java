package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMc;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities.ProbabilitiesEstimatedZ;

public class DfgMiningParametersIMcd extends DfgMiningParameters {
	public DfgMiningParametersIMcd() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));

		setDfgCutFinder(new DfgCutFinderCombination(
				new DfgCutFinderSimple(),
				new CutFinderIMc()
				));

		setDfgFallThroughs(new DfgFallThroughCombination());

		setDfgSplitter(
				new SimpleDfgSplitter()
				);

		setSatProbabilities(new ProbabilitiesEstimatedZ());
		setIncompleteThreshold(0);
	}
}