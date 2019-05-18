package org.processmining.plugins.inductiveminer2.helperclasses.graphs;

import java.util.List;

import gnu.trove.set.TIntSet;

/**
 * New implementation of connected components. Asymptotically slower, but does
 * not use sets, so might be (actually, is) faster in practice.
 * 
 * @author sleemans
 *
 */
public class IntConnectedComponents {

	/**
	 * Returns the connected components of G.
	 * 
	 * @param graph
	 * @return
	 */
	public static List<TIntSet> compute(IntGraph graph) {
		IntComponents components = new IntComponents(graph.getNodes());

		for (long edgeIndex : graph.getEdges()) {
			int source = graph.getEdgeSource(edgeIndex);
			int target = graph.getEdgeTarget(edgeIndex);

			components.mergeComponentsOf(source, target);
		}

		return components.getComponents();
	}

}
