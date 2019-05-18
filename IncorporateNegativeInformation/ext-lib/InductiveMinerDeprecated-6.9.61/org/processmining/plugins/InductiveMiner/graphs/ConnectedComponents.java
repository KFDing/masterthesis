package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.set.hash.THashSet;

import java.util.Arrays;
import java.util.Set;

public class ConnectedComponents<V> {
	private boolean[] marked; // marked[v] = has vertex v been marked?
	private int[] id; // id[v] = id of connected component containing v
	private int[] size; // size[id] = number of vertices in given component
	private int count;

	/**
	 * Returns the connected components of G.
	 * 
	 * @param G
	 * @return
	 */
	public static <Y> Set<Set<Y>> compute(Graph<Y> G) {
		ConnectedComponents<Y> cc = new ConnectedComponents<>(G);
		return cc.getResult(G);
	}

	private Set<Set<V>> getResult(Graph<V> G) {
		// compute list of vertices in each strong component
		@SuppressWarnings("unchecked")
		Set<V>[] components = new Set[count];
		for (int i = 0; i < count; i++) {
			components[i] = new THashSet<>();
		}
		for (int v = 0; v < G.getNumberOfVertices(); v++) {
			int component = id[v];
			components[component].add(G.getVertexOfIndex(v));
		}

		return new THashSet<Set<V>>(Arrays.asList(components));
	}

	private ConnectedComponents(Graph<V> G) {
		marked = new boolean[G.getNumberOfVertices()];
		id = new int[G.getNumberOfVertices()];
		size = new int[G.getNumberOfVertices()];
		for (int v = 0; v < G.getNumberOfVertices(); v++) {
			if (!marked[v]) {
				dfs(G, v);
				count++;
			}
		}
	}

	// depth-first search
	private void dfs(Graph<V> G, int v) {
		marked[v] = true;
		id[v] = count;
		size[count]++;
		for (long edgeIndex : G.getEdgesOf(v)) {

			if (G.getEdgeWeight(edgeIndex) >= 0) {
				int w;
				if (G.getEdgeSourceIndex(edgeIndex) == v) {
					w = G.getEdgeTargetIndex(edgeIndex);
				} else {
					w = G.getEdgeSourceIndex(edgeIndex);
				}

				if (!marked[w]) {
					dfs(G, w);
				}
			}
		}
	}
}
