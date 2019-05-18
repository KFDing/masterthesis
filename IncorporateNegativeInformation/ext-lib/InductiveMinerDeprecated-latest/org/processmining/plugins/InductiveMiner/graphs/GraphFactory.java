package org.processmining.plugins.InductiveMiner.graphs;

public class GraphFactory {

	public static <V> Graph<V> create(Class<?> clazz, int initialSize) {
		return new GraphImplLinearEdge<V>(clazz);
	}

	public static <V> Graph<V> createTimeOptimised(Class<?> clazz, int initialSize) {
		return new GraphImplQuadratic<V>(clazz, initialSize);
	}
	
	public static <V> Graph<V> createRandomEdgeAdding(Class<?> clazz, int initialSize) {
		return new GraphImplLinearEdgeImportOptimised<V>(clazz);
	}
}
