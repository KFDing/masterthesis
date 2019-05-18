package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import java.util.Arrays;
import java.util.Set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

public class NormalisedIntStronglyConnectedComponents {

	private boolean[] marked; // marked[v] = has v been visited?
	private int[] id; // id[v] = id of strong component containing v
	private int[] low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private TIntStack stack;

	/**
	 * Get the strongly connected components within G.
	 * 
	 * @param G
	 * @return
	 */
	public static Set<TIntSet> compute(NormalisedIntGraph G, int numberOfVertices) {
		NormalisedIntStronglyConnectedComponents cc = new NormalisedIntStronglyConnectedComponents(G, numberOfVertices);
		return cc.getResult(G, numberOfVertices);
	}

	private Set<TIntSet> getResult(NormalisedIntGraph G, int numberOfVertices) {
		// compute list of vertices in each strong component
		TIntSet[] components = new TIntSet[count];
		for (int i = 0; i < count; i++) {
			components[i] = new TIntHashSet(10, 0.5f, Integer.MIN_VALUE);
		}
		for (int v = 0; v < numberOfVertices; v++) {
			int component = id[v];
			components[component].add(v);
		}
		return new THashSet<TIntSet>(Arrays.asList(components));
	}

	private NormalisedIntStronglyConnectedComponents(NormalisedIntGraph G, int numberOfVertices) {
		marked = new boolean[numberOfVertices];
		stack = new TIntArrayStack();
		id = new int[numberOfVertices];
		low = new int[numberOfVertices];
		for (int v = 0; v < numberOfVertices; v++) {
			if (!marked[v]) {
				dfs(G, v, numberOfVertices);
			}
		}
	}

	private void dfs(NormalisedIntGraph G, int v, int numberOfVertices) {
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (long edge : G.getOutgoingEdgesOf(v)) {
			int w = G.getEdgeTargetIndex(edge);
			if (!marked[w]) {
				dfs(G, w, numberOfVertices);
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
			low[w] = numberOfVertices;
		} while (w != v);
		count++;
	}
}
