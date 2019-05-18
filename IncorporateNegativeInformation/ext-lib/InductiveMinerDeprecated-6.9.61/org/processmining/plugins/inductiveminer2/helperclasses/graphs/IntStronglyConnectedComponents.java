package org.processmining.plugins.inductiveminer2.helperclasses.graphs;

import java.util.Arrays;
import java.util.Set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

public class IntStronglyConnectedComponents {

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
	public static Set<TIntSet> compute(IntGraph G) {
		IntStronglyConnectedComponents cc = new IntStronglyConnectedComponents(G);
		return cc.getResult(G);
	}
	
	private Set<TIntSet> getResult(IntGraph G) {
		// compute list of vertices in each strong component
		TIntSet[] components = new TIntSet[count];
        for (int i = 0 ; i < count ; i++) {
        	components[i] = new TIntHashSet(10, 0.5f, -1);
        }
        for (int v = 0; v < G.getNumberOfNodes(); v++) {
        	int component = id[v];
        	components[component].add(G.getNodeOfIndex(v));
        }
        return new THashSet<>(Arrays.asList(components));
	}

	private IntStronglyConnectedComponents(IntGraph G) {
		marked = new boolean[G.getNumberOfNodes()];
		stack = new TIntArrayStack();
		id = new int[G.getNumberOfNodes()];
		low = new int[G.getNumberOfNodes()];
		for (int v = 0; v < G.getNumberOfNodes(); v++) {
			if (!marked[v]) {
				dfs(G, v);
			}
		}
	}

	private void dfs(IntGraph G, int v) {
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (long edge : G.getOutgoingEdgesOfIndex(v)) {
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
			low[w] = G.getNumberOfNodes();
		} while (w != v);
		count++;
	}
}
