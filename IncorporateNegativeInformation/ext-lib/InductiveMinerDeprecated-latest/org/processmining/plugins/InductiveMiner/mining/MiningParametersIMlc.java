package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoLifeCycle;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMlc.CutFinderIMlc;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.IMlc.FallThroughIMlc;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMlc;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersIMlc extends MiningParameters {
	
	/*
	 * No other parameter, except mentioned in this file, has influence on the mined model
	 */
	
	public MiningParametersIMlc() {
		setRepairLifeCycle(true);
		
		setLog2LogInfo(new IMLog2IMLogInfoLifeCycle());
		
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMlc()
				)));
		
		setLogSplitter(new LogSplitterIMlc());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughIMlc()
				)));
		
		setPostProcessors(new ArrayList<PostProcessor>());
		
		setLifeCycleClassifier(new LifeCycleClassifier());
		
		setReduceParameters(new EfficientTreeReduceParameters(true, false));
	}
}