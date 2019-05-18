package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve;

import java.lang.reflect.InvocationTargetException;

import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.AtomicResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.SATResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single.SATSolveSingle;

public abstract class SATSolve {

	protected final AtomicResult bestTillNow;
	protected final CutFinderIMinInfo info;
	protected final MinerStateBase minerState;

	public SATSolve(CutFinderIMinInfo info, AtomicResult bestTillNow, MinerStateBase minerState) {
		this.info = info;
		this.bestTillNow = bestTillNow;
		this.minerState = minerState;
	}

	public abstract void solve();

	public void solveDefault(final Class<? extends SATSolveSingle> c, boolean commutative) {
		double maxCut;
		if (commutative) {
			maxCut = 0.5 + info.getActivities().size() / 2;
		} else {
			maxCut = info.getActivities().size();
		}
		for (int i = 1; i < maxCut; i++) {
			final int j = i;
			info.getJobList().addJob(new Runnable() {
				public void run() {
					if (!minerState.isCancelled()) {
						SATSolveSingle solver = null;
						try {
							solver = c.getConstructor(CutFinderIMinInfo.class).newInstance(info);
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						}
						SATResult result = solver.solveSingle(j, bestTillNow.get().getProbability());
						if (result != null && result.getProbability() >= bestTillNow.get().getProbability()) {
							if (bestTillNow.maximumAndGet(result)) {
								debug("new maximum " + result);
							}
						}
					}
				}
			});
		}
	}

	protected void debug(String x) {
		if (info.isDebug()) {
			System.out.println(x);
		}
	}
}
