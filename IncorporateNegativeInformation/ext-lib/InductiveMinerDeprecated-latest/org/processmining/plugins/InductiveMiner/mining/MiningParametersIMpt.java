package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughIM;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.IMpt.LogSplitterIMpt;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersIMpt extends MiningParameters {
	public MiningParametersIMpt() {
		setProcessStartEndComplete(true);

		setLog2LogInfo(new IMLog2IMLogInfoStartEndComplete());

		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(new BaseCaseFinderIM())));

		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(new CutFinderIM())));

		setLogSplitter(new LogSplitterIMpt());

		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(new FallThroughIM())));

		setPostProcessors(new ArrayList<PostProcessor>());
	}
}
