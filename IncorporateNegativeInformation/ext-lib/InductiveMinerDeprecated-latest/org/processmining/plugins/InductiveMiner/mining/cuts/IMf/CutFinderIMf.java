package org.processmining.plugins.InductiveMiner.mining.cuts.IMf;

import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMf implements CutFinder {

	private static CutFinder cutFinderIM = new CutFinderIM();

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		//filter logInfo
		IMLogInfo logInfoFiltered = filterNoise(logInfo, minerState.parameters.getNoiseThreshold());

		//call IM cut detection
		Cut cut = cutFinderIM.findCut(null, logInfoFiltered, minerState);

		return cut;
	}

	/*
	 * filter noise
	 */

	public static IMLogInfo filterNoise(IMLogInfo logInfo, float threshold) {
		return new IMLogInfo(filterNoise(logInfo.getDfg(), threshold), logInfo.getActivities().copy(),
				logInfo.getMinimumSelfDistancesBetween(), logInfo.getMinimumSelfDistances(),
				logInfo.getNumberOfEvents(), logInfo.getNumberOfActivityInstances(), logInfo.getNumberOfTraces());
	}

	public static Dfg filterNoise(Dfg dfg, float threshold) {
		Dfg newDfg = dfg.clone();

		filterStartActivities(newDfg, threshold);
		filterEndActivities(newDfg, threshold);
		filterDirectlyFollowsGraph(newDfg, threshold);
		filterConcurrencyGraph(newDfg, threshold);
		return newDfg;
	}

	/**
	 * Filter a graph. Only keep the edges that occur often enough, compared
	 * with other outgoing edges of the source. 0 <= threshold <= 1.
	 * 
	 * @param graph
	 * @param threshold
	 * @return
	 */
	private static void filterDirectlyFollowsGraph(Dfg dfg, float threshold) {
		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

		for (int activity : dfg.getActivityIndices()) {
			//find the maximum outgoing weight of this node
			long maxWeightOut = dfg.getEndActivityCardinality(activity);
			for (long edge : graph.getOutgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) graph.getEdgeWeight(edge));
			}

			//remove all edges that are not strong enough
			Iterator<Long> it = graph.getOutgoingEdgesOf(activity).iterator();
			while (it.hasNext()) {
				long edge = it.next();
				if (graph.getEdgeWeight(edge) < maxWeightOut * threshold) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Filter a graph. Only keep the edges that occur often enough, compared
	 * with other outgoing edges of the source. 0 <= threshold <= 1.
	 * 
	 * @param graph
	 * @param threshold
	 * @return
	 */
	private static void filterConcurrencyGraph(Dfg dfg, float threshold) {
		Graph<XEventClass> graph = dfg.getConcurrencyGraph();

		for (int activity : dfg.getActivityIndices()) {
			//find the maximum outgoing weight of this node
			long maxWeightOut = dfg.getEndActivityCardinality(activity);
			for (long edge : graph.getOutgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) graph.getEdgeWeight(edge));
			}

			Iterator<Long> it = graph.getOutgoingEdgesOf(activity).iterator();
			while (it.hasNext()) {
				long edge = it.next();
				if (graph.getEdgeWeight(edge) < maxWeightOut * threshold) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Filter start activities. Only keep those occurring more times than
	 * threshold * the most occurring activity. 0 <= threshold <= 1.
	 * 
	 * @param activities
	 * @param threshold
	 * @return
	 */
	private static void filterStartActivities(Dfg dfg, float threshold) {
		long max = dfg.getMostOccurringStartActivityCardinality();
		for (int activity : dfg.getStartActivityIndices()) {
			if (dfg.getStartActivityCardinality(activity) < threshold * max) {
				dfg.removeStartActivity(activity);
			}
		}
	}

	/**
	 * Filter start activities. Only keep those occurring more times than
	 * threshold * the most occurring activity. 0 <= threshold <= 1.
	 * 
	 * @param activities
	 * @param threshold
	 * @return
	 */
	private static void filterEndActivities(Dfg dfg, float threshold) {
		long max = dfg.getMostOccurringEndActivityCardinality();
		for (int activity : dfg.getEndActivityIndices()) {
			if (dfg.getEndActivityCardinality(activity) < threshold * max) {
				dfg.removeEndActivity(activity);
			}
		}
	}
}