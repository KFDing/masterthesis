package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.set.hash.THashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Arrays;
import java.util.Set;

public class StronglyConnectedComponents<X> {

	private boolean[] marked; // marked[v] = has v been visited?
	private int[] id; // id[v] = id of strong component containing v
	private int[] low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private TIntStack stack;
	
	/**
	 * Get the strongly connected components within G.
	 * @param G
	 * @return
	 */
	public static <Y> Set<Set<Y>> compute(Graph<Y> G) {
		StronglyConnectedComponents<Y> cc = new StronglyConnectedComponents<>(G);
		return cc.getResult(G);
	}
	
	private Set<Set<X>> getResult(Graph<X> G) {
		// compute list of vertices in each strong component
        @SuppressWarnings("unchecked")
		Set<X>[] components = new Set[count];
        for (int i = 0 ; i < count ; i++) {
        	components[i] = new THashSet<>();
        }
        for (int v = 0; v < G.getNumberOfVertices(); v++) {
        	int component = id[v];
        	components[component].add(G.getVertexOfIndex(v));
        }
        return new THashSet<Set<X>>(Arrays.asList(components));
	}

	private StronglyConnectedComponents(Graph<X> G) {
		marked = new boolean[G.getNumberOfVertices()];
		stack = new TIntArrayStack();
		id = new int[G.getNumberOfVertices()];
		low = new int[G.getNumberOfVertices()];
		for (int v = 0; v < G.getNumberOfVertices(); v++) {
			if (!marked[v]) {
				dfs(G, v);
			}
		}
	}

	private void dfs(Graph<X> G, int v) {
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (long edge : G.getOutgoingEdgesOf(v)) {
			int w = G.getEdgeTargetIndex(edge);
			if (!marked[w]) {
				dfs(G, w);
			}
			if (low[w] < min) {
				min = low[w];
			}
		}
		if (min < low[v]) {
			low[v] = min;
			return;
		}
		int w;
		do {
			w = stack.pop();
			id[w] = count;
			low[w] = G.getNumberOfVertices();
		} while (w != v);
		count++;
	}
}
