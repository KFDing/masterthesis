package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor.Exhaustive.Result;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderEKS implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {

		UpToKSuccessorMatrix kSuccessor = UpToKSuccessor.fromLog(log, logInfo);
		Exhaustive exhaustive = new Exhaustive(log, logInfo, kSuccessor, minerState);
		Result r = exhaustive.tryAll();

		return r.cut;
	}

}
