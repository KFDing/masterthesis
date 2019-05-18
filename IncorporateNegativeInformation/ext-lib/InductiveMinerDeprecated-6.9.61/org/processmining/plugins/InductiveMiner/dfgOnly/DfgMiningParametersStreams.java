package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderMergeParallelGraph;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderNoiseFiltering;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;

public class DfgMiningParametersStreams extends DfgMiningParameters {

	public DfgMiningParametersStreams() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));
		
		setDfgCutFinder(new DfgCutFinderCombination(
				new DfgCutFinderMergeParallelGraph(),
				new DfgCutFinderSimple(),
				new DfgCutFinderNoiseFiltering()
				));
		
		setDfgFallThroughs(new ArrayList<DfgFallThrough>(Arrays.asList(
				new DfgFallThroughFlower()
				)));
		
		setDfgSplitter(new SimpleDfgSplitter());
		
		setDebug(true);
		setNoiseThreshold(0.2f);
	}

}
