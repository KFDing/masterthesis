package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyLog;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyTrace;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiSingleActivity;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMExclusiveChoice;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMa.CutFinderIMaConcurrentOptionalOr;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMa.CutFinderIMaInterleaved;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMfa.CutFinderIMfa;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughIM;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterInterleavedFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterMaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterOr;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterSequenceFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXorFiltering;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;

public class MiningParametersIMfa extends MiningParameters {
	
	public MiningParametersIMfa() {
		
		setLog2LogInfo(new IMLog2IMLogInfoDefault());
	
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMiEmptyLog(),
				new BaseCaseFinderIMiEmptyTrace(),
				new BaseCaseFinderIMiSingleActivity()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIMExclusiveChoice(),
				new CutFinderIMSequence(),
				new CutFinderIMaConcurrentOptionalOr(),
				new CutFinderIMaInterleaved(),
				new CutFinderIMLoop(),
				new CutFinderIMf(),
				new CutFinderIMfa()
				)));
		
		setLogSplitter(new LogSplitterCombination(
				new LogSplitterXorFiltering(), 
				new LogSplitterSequenceFiltering(), 
				new LogSplitterParallel(),
				new LogSplitterLoop(),
				new LogSplitterMaybeInterleaved(),
				new LogSplitterInterleavedFiltering(),
				new LogSplitterOr()));
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughIM()
				)));
		
		setNoiseThreshold((float) 0.2);
		
		setPostProcessors(new ArrayList<PostProcessor>());
	}
}
