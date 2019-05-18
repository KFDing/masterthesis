package org.processmining.plugins.InductiveMiner.graphs;

public interface UndirectedGraph<V> {

	public V[] getVertices();

	public Iterable<Long> getEdges();

	public void addEdge(int nodeIndexA, int nodeIndexB);

	public boolean hasEdge(int nodeIndexA, int nodeIndexB);

	public int getEdgeNodeIndexA(long edgeIndex);
	public V getEdgeNodeA(long edgeIndex);

	public int getEdgeNodeIndexB(long edgeIndex);
	public V getEdgeNodeB(long edgeIndex);
}
