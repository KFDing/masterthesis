package org.processmining.plugins.InductiveMiner.graphs;

/**
 * "Solve" the clique problem, but polynomially on arbitrary graphs. This is
 * NP-hard, but we only need to guarantee that 1) each returned clique is a true
 * clique, and 2) a clique that is not connected to any other node is reported
 * as a clique.
 * 
 * @author sleemans
 *
 */
public class Cliques {
	public static <V> Components<V> compute(UndirectedGraph<V> graph) {
		//initially, each node is it's own clique
		Components<V> components = new Components<V>(graph.getVertices());

		for (long edge : graph.getEdges()) {
			V source = graph.getEdgeNodeA(edge);
			V target = graph.getEdgeNodeB(edge);

			//see if source and target can be merged
			if (!components.areInSameComponent(source, target)) {
				if (canBeMerged(graph, components, components.getComponentOf(source), components.getComponentOf(target))) {
					components.mergeComponentsOf(source, target);
				}
			}
		}

		return components;
	}

	private static <V> boolean canBeMerged(UndirectedGraph<V> graph, Components<V> components, int clique1, int clique2) {
		for (int node1 : components.getNodeIndicesOfComponent(clique1)) {
			for (int node2 : components.getNodeIndicesOfComponent(clique2)) {
				if (!graph.hasEdge(node1, node2)) {
					return false;
				}
			}
		}
		return true;
	}
}
