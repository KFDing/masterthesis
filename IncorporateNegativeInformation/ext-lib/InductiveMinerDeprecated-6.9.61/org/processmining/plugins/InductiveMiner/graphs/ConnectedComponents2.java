package org.processmining.plugins.InductiveMiner.graphs;

import java.util.List;
import java.util.Set;

/**
 * New implementation of connected components. Asymptotically slower, but does
 * not use sets, so might be (actually, is) faster in practice.
 * 
 * @author sleemans
 *
 */
public class ConnectedComponents2 {

	/**
	 * Returns the connected components of G.
	 * 
	 * @param graph
	 * @return
	 */
	public static <Y> List<Set<Y>> compute(Graph<Y> graph) {
		Components<Y> components = new Components<Y>(graph.getVertices());

		for (long edgeIndex : graph.getEdges()) {
			int source = graph.getEdgeSourceIndex(edgeIndex);
			int target = graph.getEdgeTargetIndex(edgeIndex);

			components.mergeComponentsOf(source, target);
		}

		return components.getComponents();
	}

}
