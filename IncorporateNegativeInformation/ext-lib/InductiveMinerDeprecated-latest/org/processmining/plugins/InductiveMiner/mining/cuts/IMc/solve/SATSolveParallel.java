package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve;

import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single.SATSolveSingleParallel;

public class SATSolveParallel extends SATSolve {

	public SATSolveParallel(CutFinderIMinInfo info, AtomicResult result, MinerStateBase minerState) {
		super(info, result, minerState);
	}
	
	public void solve() {
		//debug("start SAT search for parallel cut likelier than " + bestTillNow.get().getProbability());
		solveDefault(SATSolveSingleParallel.class, true);
	}
}
