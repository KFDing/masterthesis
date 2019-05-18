package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMa.CutFinderIMaConcurrentOptionalOr;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMa.CutFinderIMaInterleaved;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughIM;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterMaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterOr;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterSequenceFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXorFiltering;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersIMa extends MiningParameters {
	
	public MiningParametersIMa() {
		
		setLog2LogInfo(new IMLog2IMLogInfoDefault());
	
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMaConcurrentOptionalOr(),
				new CutFinderIMaInterleaved(),
				new CutFinderIMLoop()
				)));
		
		setLogSplitter(new LogSplitterCombination(
				new LogSplitterXorFiltering(), 
				new LogSplitterSequenceFiltering(), 
				new LogSplitterParallel(),
				new LogSplitterLoop(),
				new LogSplitterMaybeInterleaved(),
				//new LogSplitterInterleavedFiltering(),
				new LogSplitterParallel(),
				new LogSplitterOr()));
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughIM()
				)));
		
		setNoiseThreshold(0f);
		
		setPostProcessors(new ArrayList<PostProcessor>());
	}
}
