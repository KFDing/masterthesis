package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve;

import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.SATResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single.SATSolveSingle;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single.SATSolveSingleLoop;

public class SATSolveLoop extends SATSolve {

	public SATSolveLoop(CutFinderIMinInfo info, AtomicResult result, MinerStateBase minerState) {
		super(info, result, minerState);
	}

	public void solve() {
		//debug("start SAT search for loop cut likelier than " + bestTillNow.get().getProbability());

		for (int i = 1; i < info.getActivities().size(); i++) {
			final int j = i;
			info.getJobList().addJob(new Runnable() {
				public void run() {
					SATSolveSingle solver = new SATSolveSingleLoop(info);
					SATResult result = solver.solveSingle(j, bestTillNow.get().getProbability());
					if (result != null && result.getProbability() >= bestTillNow.get().getProbability()) {
						if (bestTillNow.maximumAndGet(result)) {
							debug("new maximum " + result);
						}
					}
				}
			});
		}
	}

}
