package org.processmining.plugins.InductiveMiner.mining.cuts.IMlc;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class CutFinderIMlcInterleaved implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut cut = findCutBasic(logInfo.getDfg(), logInfo.getDfg().getDirectlyFollowsGraph(),
				logInfo.getDfg().getConcurrencyGraph());
		if (cut == null) {
			return null;
		}

		return findSpecialCase(logInfo.getDfg(), cut.getPartition(), logInfo.getDfg().getDirectlyFollowsGraph());
	}

	/**
	 * Finds the special case int(A, B) where A is not interleaved itself (B
	 * might be).
	 * 
	 * @param dfg
	 * @param partition
	 * @param directlyFollowsGraph
	 * @return
	 */
	public static Cut findSpecialCase(Dfg dfg, Collection<Set<XEventClass>> partition,
			Graph<XEventClass> directlyFollowsGraph) {
		//count the number of start activities
		for (Set<XEventClass> sigma : partition) {

			//check if this sigma has startActivities = outgoing-dfg-edges
			long countStartActivities = 0;
			for (XEventClass a : dfg.getStartActivities()) {
				if (sigma.contains(a)) {
					countStartActivities += dfg.getStartActivityCardinality(a);
				}
			}

			//count the outgoing-dfg-edges
			long countOutgoingDfgEdges = 0;
			for (XEventClass a : sigma) {
				for (long edge : directlyFollowsGraph.getOutgoingEdgesOf(a)) {
					if (!sigma.contains(directlyFollowsGraph.getEdgeTarget(edge))) {
						//this is an outgoing edge
						countOutgoingDfgEdges += directlyFollowsGraph.getEdgeWeight(edge);
					}
				}
			}

			if (countStartActivities == countOutgoingDfgEdges) {
				//we conclude that this sigma is a non-interleaving child of a binary interleaving operator
				Collection<Set<XEventClass>> newPartition = new THashSet<>();
				newPartition.add(sigma);
				Set<XEventClass> newSigma = new THashSet<>();
				for (Set<XEventClass> sigma2 : partition) {
					if (!sigma.equals(sigma2)) {
						newSigma.addAll(sigma2);
					}
				}
				newPartition.add(newSigma);
				//System.out.println(" interleaved: special case");
				return new Cut(Operator.maybeInterleaved, newPartition);
			}
		}
		return new Cut(Operator.maybeInterleaved, partition);
	}

	public static Cut findCutBasic(Dfg dfg, Graph<XEventClass> directGraph, Graph<XEventClass> concurrencyGraph) {

		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

		//put each activity in a component.
		Components<XEventClass> components = new Components<XEventClass>(graph.getVertices());

		/*
		 * By semantics of the interleaved operator, a non-start activity cannot
		 * have connections from other subtrees. Thus, walk over all such
		 * activities and merge components.
		 */
		for (int activityIndex : graph.getVertexIndices()) {
			if (!dfg.isStartActivity(activityIndex)) {
				for (long edgeIndex : graph.getIncomingEdgesOf(activityIndex)) {
					int source = graph.getEdgeSourceIndex(edgeIndex);
					components.mergeComponentsOf(source, activityIndex);
				}
			}
			if (!dfg.isEndActivity(activityIndex)) {
				for (long edgeIndex : graph.getOutgoingEdgesOf(activityIndex)) {
					int target = graph.getEdgeTargetIndex(edgeIndex);
					components.mergeComponentsOf(activityIndex, target);
				}
			}
		}

		/*
		 * All start activities need to be doubly connected from all end
		 * activities from other components. Thus, walk through the start
		 * activities and end activities and merge violating pairs. The reverse
		 * direction is implied.
		 */
		for (int startActivity : dfg.getStartActivityIndices()) {
			for (int endActivity : dfg.getEndActivityIndices()) {
				if (startActivity != endActivity) {
					if (!graph.containsEdge(endActivity, startActivity)) {
						components.mergeComponentsOf(startActivity, endActivity);
					}
				}
			}
		}

		if (components.getNumberOfComponents() < 2) {
			return null;
		}

		return new Cut(Operator.maybeInterleaved, components.getComponents());
	}

	public static void mergeClusters(TObjectIntMap<XEventClass> clusters, int c1, int c2) {
		for (XEventClass e3 : clusters.keySet()) {
			if (clusters.get(e3) == c2) {
				clusters.put(e3, c1);
			}
		}
	}

	public static void mergeClusters(TObjectIntMap<XEventClass> clusters, XEventClass e1, XEventClass e2) {
		int target = clusters.get(e1);
		if (clusters.containsKey(e2)) {
			int oldCluster = clusters.get(e2);
			for (XEventClass e3 : clusters.keySet()) {
				if (clusters.get(e3) == oldCluster) {
					clusters.put(e3, target);
				}
			}
		} else {
			clusters.put(e2, target);
		}
	}

	public static Collection<Set<XEventClass>> getPartition(TObjectIntMap<XEventClass> clusters) {
		TIntObjectHashMap<Set<XEventClass>> map = new TIntObjectHashMap<>();

		for (XEventClass a : clusters.keySet()) {
			int cluster = clusters.get(a);
			map.putIfAbsent(cluster, new THashSet<XEventClass>());
			map.get(cluster).add(a);
		}

		return map.valueCollection();
	}
}
