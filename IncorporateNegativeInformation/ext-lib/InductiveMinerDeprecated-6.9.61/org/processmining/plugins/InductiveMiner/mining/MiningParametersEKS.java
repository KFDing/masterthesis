package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyLog;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyTrace;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiSingleActivity;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor.CutFinderEKS;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughActivityOncePerTraceConcurrent;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlowerWithEpsilon;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersEKS extends MiningParameters {
	
	/*
	 * No other parameter, except mentioned in this file, has influence on mined model
	 */
	
	public MiningParametersEKS() {
		
		setLog2LogInfo(new IMLog2IMLogInfoLifeCycle());
		
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMiEmptyLog(),
				new BaseCaseFinderIMiEmptyTrace(),
				new BaseCaseFinderIMiSingleActivity()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIM(),
				new CutFinderEKS()
				)));
		
		setLogSplitter(new LogSplitterIMi());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughActivityOncePerTraceConcurrent(true),
				new FallThroughTauLoop(true),
				new FallThroughFlowerWithEpsilon()
				)));
		
		setPostProcessors(new ArrayList<PostProcessor>());
	}
}
