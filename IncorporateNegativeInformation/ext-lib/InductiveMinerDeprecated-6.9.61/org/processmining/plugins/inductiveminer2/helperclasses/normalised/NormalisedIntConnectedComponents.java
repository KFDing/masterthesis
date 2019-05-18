package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import java.util.List;

import gnu.trove.set.TIntSet;

/**
 * New implementation of connected components. Asymptotically slower, but does
 * not use sets, so might be (actually, is) faster in practice.
 * 
 * Assumes that all nodes are normalised, i.e. their indices are [0..n-1]
 * 
 * @author sleemans
 *
 */
public class NormalisedIntConnectedComponents {

	/**
	 * Returns the connected components of G.
	 * 
	 * @param graph
	 * @param numberOfNodes
	 * @return
	 */
	public static List<TIntSet> compute(NormalisedIntGraph graph, int numberOfNodes) {
		NormalisedIntComponents components = new NormalisedIntComponents(numberOfNodes);

		for (long edgeIndex : graph.getEdges()) {
			int source = graph.getEdgeSourceIndex(edgeIndex);
			int target = graph.getEdgeTargetIndex(edgeIndex);

			components.mergeComponentsOf(source, target);
		}

		return components.getComponents();
	}

}
