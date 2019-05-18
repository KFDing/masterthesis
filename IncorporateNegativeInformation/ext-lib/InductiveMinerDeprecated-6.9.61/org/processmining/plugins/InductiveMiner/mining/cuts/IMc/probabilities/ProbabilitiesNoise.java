package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;

public class ProbabilitiesNoise extends Probabilities {

	public double getProbabilityXor(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilitySequence(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		if (D(logInfo, a, b) && !D(logInfo, b, a) && !E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityParallel(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && D(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopSingle(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		if (D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public double getProbabilityLoopDouble(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		return 0;
	}

	public double getProbabilityLoopIndirect(CutFinderIMinInfo logInfo, XEventClass a, XEventClass b) {
		if (!D(logInfo, a, b) && !D(logInfo, b, a) && E(logInfo, a, b) && E(logInfo, b, a)) {
			return 1 - 1/(z(logInfo, a, b) + 1);
		}
		return (1.0 / 6.0) * 1/(z(logInfo, a, b) + 1);
	}

	public String toString() {
		return "SAT noise";
	}

}
