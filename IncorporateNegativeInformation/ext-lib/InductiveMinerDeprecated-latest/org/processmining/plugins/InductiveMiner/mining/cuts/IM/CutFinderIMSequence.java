package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.ArrayUtilities;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.graphs.Reachability;
import org.processmining.plugins.InductiveMiner.graphs.StronglyConnectedComponents;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;

public class CutFinderIMSequence implements CutFinder, DfgCutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getDfg());
	}

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		return findCut(dfg);
	}

	public static Cut findCut(Dfg dfg) {
		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

		//compute the strongly connected components of the directly follows graph
		Set<Set<XEventClass>> SCCs = StronglyConnectedComponents.compute(graph);

		//condense the strongly connected components
		Graph<Set<XEventClass>> condensedGraph1 = GraphFactory.create(Set.class, SCCs.size());
		{

			//15-3-2016: optimisation to look up strongly connected components faster
			TIntIntMap node2sccIndex = new TIntIntHashMap();
			{
				int i = 0;
				for (Set<XEventClass> scc : SCCs) {
					for (XEventClass e : scc) {
						node2sccIndex.put(graph.getIndexOfVertex(e), i);
					}
					i++;
				}
			}

			//add vertices (= components)
			for (Set<XEventClass> SCC : SCCs) {
				condensedGraph1.addVertex(SCC);
			}
			//add edges
			for (long edge : graph.getEdges()) {
				if (graph.getEdgeWeight(edge) >= 0) {
					//find the connected components belonging to these nodes
					int u = graph.getEdgeSourceIndex(edge);
					int SCCu = node2sccIndex.get(u);
					int v = graph.getEdgeTargetIndex(edge);
					int SCCv = node2sccIndex.get(v);

					//add an edge if it is not internal
					if (SCCv != SCCu) {
						condensedGraph1.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
					}
				}
			}
		}

		//debug("  nodes in condensed graph 1 " + condensedGraph1.getVertices());

		//condense the pairwise unreachable nodes
		Collection<Set<Set<XEventClass>>> xorCondensedNodes;
		{
			Components<Set<XEventClass>> components = new Components<Set<XEventClass>>(condensedGraph1.getVertices());
			CutFinderIMSequenceReachability scr1 = new CutFinderIMSequenceReachability(condensedGraph1);

			for (int node : condensedGraph1.getVertexIndices()) {
				TIntSet reachableFromTo = scr1.getReachableFromTo(node);

				//debug("nodes pairwise reachable from/to " + node.toString() + ": " + reachableFromTo.toString());

				for (int node2 : condensedGraph1.getVertexIndices()) {
					if (node != node2 && !reachableFromTo.contains(node2)) {
						components.mergeComponentsOf(node, node2);
					}
				}

			}

			//find the connected components to find the condensed xor nodes
			xorCondensedNodes = components.getComponents();
		}

		//debug("sccs voor xormerge " + xorCondensedNodes.toString());

		//make a new condensed graph
		final Graph<Set<XEventClass>> condensedGraph2 = GraphFactory.create(Set.class, xorCondensedNodes.size());
		for (Set<Set<XEventClass>> node : xorCondensedNodes) {

			//we need to flatten this s to get a new list of nodes
			condensedGraph2.addVertex(Sets.flatten(node));
		}

		//debug("sccs na xormerge " + condensedGraph2.getVertices().toString());

		//add the edges
		Set<Set<XEventClass>> set = ArrayUtilities.toSet(condensedGraph2.getVertices());
		for (long edge : condensedGraph1.getEdges()) {
			//find the condensed node belonging to this activity
			Set<XEventClass> u = condensedGraph1.getEdgeSource(edge);
			Set<XEventClass> SCCu = Sets.findComponentWith(set, u.iterator().next());
			Set<XEventClass> v = condensedGraph1.getEdgeTarget(edge);
			Set<XEventClass> SCCv = Sets.findComponentWith(set, v.iterator().next());

			//add an edge if it is not internal
			if (SCCv != SCCu) {
				condensedGraph2.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
				//debug ("nodes in condensed graph 2 " + Sets.implode(condensedGraph2.vertexSet(), ", "));
			}
		}

		//now we have a condensed graph. we need to return a sorted list of condensed nodes.
		final CutFinderIMSequenceReachability scr2 = new CutFinderIMSequenceReachability(condensedGraph2);
		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		result.addAll(Arrays.asList(condensedGraph2.getVertices()));
		Collections.sort(result, new Comparator<Set<XEventClass>>() {

			public int compare(Set<XEventClass> arg0, Set<XEventClass> arg1) {
				if (scr2.getReachableFrom(condensedGraph2.getIndexOfVertex(arg0)).contains(
						condensedGraph2.getIndexOfVertex(arg1))) {
					return 1;
				} else {
					return -1;
				}
			}

		});

		if (result.size() <= 1) {
			return null;
		}

		/**
		 * Optimisation 4-8-2015: do not greedily use the maximal cut, but
		 * choose the one that minimises the introduction of taus.
		 * 
		 * This solves the case {<a, b, c>, <c>}, where choosing the cut {a,
		 * b}{c} increases precision over choosing the cut {a}{b}{c}.
		 * 
		 * Correction 11-7-2016: identify optional sub sequences and merge them.
		 */
		Cut newCut = new Cut(Operator.sequence, CutFinderIMSequenceStrict.merge(dfg, result));
		if (newCut.isValid()) {
			return newCut;
		} else {
			return new Cut(Operator.sequence, result);
		}
	}

	public static Cut findCut2(Dfg dfg) {
		Components<XEventClass> components = new Components<XEventClass>(dfg.getActivities());

		final Reachability reachability = new Reachability(dfg);
		for (int i : dfg.getActivityIndices()) {
			for (int j : dfg.getActivityIndices()) {
				if (!components.areInSameComponent(i, j)) {
					if (reachability.isReachable(i, j) == reachability.isReachable(j, i)) {
						/*
						 * Either the nodes are not reachable from each other (=
						 * xor), or they are reachable from one another
						 * (=loop/and/int). Hence, merge.
						 */
						components.mergeComponentsOf(i, j);
					}
				}
			}
		}

		if (components.getNumberOfComponents() < 2) {
			return null;
		}

		/*
		 * Sort the components by their structure, i.e. if a component is
		 * reachable from another component, it appears afterwards in the list.
		 */
		List<Set<XEventClass>> result = components.getComponents();
		Collections.sort(result, new Comparator<Set<XEventClass>>() {
			public int compare(Set<XEventClass> arg0, Set<XEventClass> arg1) {
				XEventClass pivot0 = arg0.iterator().next();
				XEventClass pivot1 = arg1.iterator().next();
				if (!reachability.isReachable(pivot0, pivot1)) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		/**
		 * Optimisation 4-8-2015: do not greedily use the maximal cut, but
		 * choose the one that minimises the introduction of taus.
		 * 
		 * This solves the case {<a, b, c>, <c>}, where choosing the cut {a,
		 * b}{c} increases precision over choosing the cut {a}{b}{c}.
		 */
		{
			//make a mapping node -> subCut
			//initialise counting of taus
			TObjectIntMap<XEventClass> node2subCut = new TObjectIntHashMap<>();
			long[] skippingTaus = new long[result.size() - 1];
			for (int subCut = 0; subCut < result.size(); subCut++) {
				for (XEventClass e : result.get(subCut)) {
					node2subCut.put(e, subCut);
				}
			}

			//count the number of taus that will be introduced by each edge
			for (long edge : dfg.getDirectlyFollowsEdges()) {
				XEventClass source = dfg.getDirectlyFollowsEdgeSource(edge);
				XEventClass target = dfg.getDirectlyFollowsEdgeTarget(edge);
				long cardinality = dfg.getDirectlyFollowsEdgeCardinality(edge);
				for (int c = node2subCut.get(source) + 1; c < node2subCut.get(target) - 1; c++) {
					skippingTaus[c] += cardinality;
				}
			}

			//count the number of taus that will be introduced by each start activity
			for (XEventClass e : dfg.getStartActivities()) {
				for (int c = 0; c < node2subCut.get(e) - 1; c++) {
					skippingTaus[c] += dfg.getStartActivityCardinality(e);
				}
			}

			//count the number of taus that will be introduced by each end activity
			for (XEventClass e : dfg.getEndActivities()) {
				for (int c = node2subCut.get(e) + 1; c < result.size() - 1; c++) {
					skippingTaus[c] += dfg.getEndActivityCardinality(e);
				}
			}

			//find the sub cut that introduces the least taus
			int subCutWithMinimumTaus = -1;
			{
				long minimumTaus = Long.MAX_VALUE;
				for (int i = 0; i < skippingTaus.length; i++) {
					if (skippingTaus[i] < minimumTaus) {
						subCutWithMinimumTaus = i;
						minimumTaus = skippingTaus[i];
					}
				}
			}

			//make a new cut
			Set<XEventClass> result1 = new THashSet<>();
			Set<XEventClass> result2 = new THashSet<>();
			for (int i = 0; i <= subCutWithMinimumTaus; i++) {
				result1.addAll(result.get(i));
			}
			for (int i = subCutWithMinimumTaus + 1; i < result.size(); i++) {
				result2.addAll(result.get(i));
			}
			result.clear();
			result.add(result1);
			result.add(result2);
		}

		return new Cut(Operator.sequence, result);
	}
}
