package org.processmining.plugins.InductiveMiner.mining;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
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
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessorInterleaved;

/**
 * Renamed to MiningParameters IMf
 * @author sleemans
 *
 */

@Deprecated
public class MiningParametersIMi extends MiningParametersIMf {


}
