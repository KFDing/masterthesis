package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.processmining.plugins.InductiveMiner.graphs.Graph;

/**
 * Notice that this approach does not work on cyclic graphs.
 * @author sleemans
 *
 */
public class CutFinderIMSequenceReachability {

	private TIntObjectMap<TIntSet> reachableTo;
	private TIntObjectMap<TIntSet> reachableFrom;
	private Graph<?> condensedGraph;

	public CutFinderIMSequenceReachability(Graph<?> graph) {
		reachableTo = new TIntObjectHashMap<>();
		reachableFrom = new TIntObjectHashMap<>();
		this.condensedGraph = graph;
	}

	public TIntSet getReachableFromTo(int node) {
		TIntSet r = new TIntHashSet(findReachableTo(node));
		r.addAll(findReachableFrom(node));
		return r;
	}

	public TIntSet getReachableFrom(int node) {
		return findReachableFrom(node);
	}

	private TIntSet findReachableTo(int from) {
		if (!reachableTo.containsKey(from)) {
			TIntSet reached = new TIntHashSet();

			reachableTo.put(from, reached);

			for (long edge : condensedGraph.getOutgoingEdgesOf(from)) {
				int target = condensedGraph.getEdgeTargetIndex(edge);
				reached.add(target);

				//recurse
				reached.addAll(findReachableTo(target));
			}
		}
		return reachableTo.get(from);
	}

	private TIntSet findReachableFrom(int to) {
		if (!reachableFrom.containsKey(to)) {
			TIntSet reached = new TIntHashSet();

			reachableFrom.put(to, reached);

			for (long edge : condensedGraph.getIncomingEdgesOf(to)) {
				int source = condensedGraph.getEdgeSourceIndex(edge);
				reached.add(source);

				//recurse
				reached.addAll(findReachableFrom(source));
			}
		}
		return reachableFrom.get(to);
	}
}
