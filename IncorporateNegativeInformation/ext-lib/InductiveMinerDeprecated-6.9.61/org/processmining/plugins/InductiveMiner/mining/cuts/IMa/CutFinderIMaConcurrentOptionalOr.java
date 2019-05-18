package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrentWithMinimumSelfDistance;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMaConcurrentOptionalOr implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut cut = CutFinderIMConcurrentWithMinimumSelfDistance.findCutImpl(log, logInfo, minerState);

		if (cut == null || !cut.isValid()) {
			return null;
		}

		/**
		 * We assume that the log does not contain empty traces, i.e. in each
		 * trace at least one part is executed. This is guaranteed by the empty
		 * log base case.
		 */

		Components<XEventClass> components = new Components<>(cut.getPartition());
		ConcurrentOptionalOrLog cooLog = new ConcurrentOptionalOrLog(log, components);

		while (components.getNumberOfComponents() > 1) {
			ConcurrentOptionalOrLogInfo cooLogInfo = new ConcurrentOptionalOrLogInfo(cooLog);
			debug(minerState, " presence abstraction " + cooLog);

			Pair<Integer, Integer> biImplication = cooLogInfo.findBiImplication();
			if (biImplication != null) {
				//found a bi-implication

				debug(minerState, "  and " + components.getComponents().get(biImplication.getA()) + ", "
						+ components.getComponents().get(biImplication.getB()));

				if (components.getNumberOfComponents() == 2) {
					return new Cut(Operator.concurrent, components.getComponents());
				}

				cooLog = ConcurrentOptionalOrLogMerger.merge(components, cooLog, biImplication.getA(),
						biImplication.getB());
			} else {
				Pair<Integer, Integer> or = cooLogInfo.findOr(cooLog.getTraces());
				if (or != null) {
					//found an or-relation

					debug(minerState, "  or " + components.getComponents().get(or.getA()) + ", "
							+ components.getComponents().get(or.getB()));

					if (components.getNumberOfComponents() == 2) {
						return new Cut(Operator.or, components.getComponents());
					}
					cooLog = ConcurrentOptionalOrLogMerger.merge(components, cooLog, or.getA(), or.getB());
				} else {
					Pair<Integer, Integer> optionalAnd = cooLogInfo.findOptionalAnd(cooLog.getTraces());
					if (optionalAnd != null) {
						//found an and with a single optional child

						debug(minerState, "  and-one-optional " + components.getComponents().get(optionalAnd.getA())
								+ ", " + components.getComponents().get(optionalAnd.getB()));

						if (components.getNumberOfComponents() == 2) {
							return new Cut(Operator.concurrent, components.getComponents());
						}
						cooLog = ConcurrentOptionalOrLogMerger.merge(components, cooLog, optionalAnd.getA(),
								optionalAnd.getB());
					} else {
						//found nothing

						/*
						 * As a fall-through, return a concurrent cut of the
						 * components up till now. This is allowed, as by a base
						 * case, no empty traces can be present.
						 */
						return new Cut(Operator.concurrent, components.getComponents());
					}
				}
			}
		}
		return null;
	}

	private static void debug(MinerState minerState, String message) {
		if (minerState.parameters.isDebug()) {
			System.out.println(message);
		}
	}
}
