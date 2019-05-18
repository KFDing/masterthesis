package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.DfgSplitter;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities.Probabilities;

public abstract class DfgMiningParameters {

	private Iterable<DfgBaseCaseFinder> dfgBaseCaseFinders;
	private DfgCutFinder dfgCutFinder;
	private DfgSplitter dfgSplitter;
	private Iterable<DfgFallThrough> dfgFallThroughs;
	
	private boolean debug;
	private boolean useMultiThreading;

	private float noiseThreshold = 0.2f;
	private Probabilities satProbabilities = null;
	private float incompleteThreshold = 0;

	public DfgMiningParameters() {
		debug = false;
		useMultiThreading = true;
	}

	public void setUseMultithreading(boolean useMultithreading) {
		this.useMultiThreading = useMultithreading;
	}
	
	public boolean isUseMultiThreading() {
		return useMultiThreading;
	}

	public Iterable<DfgBaseCaseFinder> getDfgBaseCaseFinders() {
		return dfgBaseCaseFinders;
	}

	public void setDfgBaseCaseFinders(Iterable<DfgBaseCaseFinder> baseCaseFinders) {
		this.dfgBaseCaseFinders = baseCaseFinders;
	}

	public DfgCutFinder getDfgCutFinder() {
		return dfgCutFinder;
	}

	public void setDfgCutFinder(DfgCutFinder dfgCutFinder) {
		this.dfgCutFinder = dfgCutFinder;
	}

	public DfgSplitter getDfgSplitter() {
		return dfgSplitter;
	}

	public void setDfgSplitter(DfgSplitter dfgSplitter) {
		this.dfgSplitter = dfgSplitter;
	}

	public Iterable<DfgFallThrough> getDfgFallThroughs() {
		return dfgFallThroughs;
	}

	public void setDfgFallThroughs(Iterable<DfgFallThrough> dfgFallThroughs) {
		this.dfgFallThroughs = dfgFallThroughs;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public float getNoiseThreshold() {
		return noiseThreshold;
	}

	public void setNoiseThreshold(float noiseThreshold) {
		this.noiseThreshold = noiseThreshold;
	}

	public Probabilities getSatProbabilities() {
		return this.satProbabilities;
	}

	public void setSatProbabilities(Probabilities satProbabilities) {
		this.satProbabilities = satProbabilities;
	}

	public float getIncompleteThreshold() {
		return this.incompleteThreshold;
	}
	
	public void setIncompleteThreshold(float incompleteThreshold) {
		this.incompleteThreshold = incompleteThreshold;
	}

}
