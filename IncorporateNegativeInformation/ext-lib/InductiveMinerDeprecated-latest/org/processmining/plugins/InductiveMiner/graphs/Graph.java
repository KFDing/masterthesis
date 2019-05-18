package org.processmining.plugins.InductiveMiner.graphs;

import java.util.Collection;

import org.processmining.plugins.inductiveminer2.helperclasses.normalised.NormalisedIntGraph;

public interface Graph<V> extends NormalisedIntGraph {

	/**
	 * Add a vertex to the graph. Has no effect if the vertex is already in the
	 * graph. Returns the index of the inserted vertex.
	 * 
	 * @param x
	 */
	public int addVertex(V x);

	public void addVertices(Collection<V> xs);

	public void addVertices(V[] xs);

	/**
	 * Adds an edge. If the weight becomes 0, the edge is removed. Use the
	 * integer variant if possible.
	 * 
	 * @param source
	 * @param target
	 * @param weight
	 */
	public void addEdge(V source, V target, long weight);

	public V getVertexOfIndex(int index);

	public V[] getVertices();

	public int[] getVertexIndices();

	public int getNumberOfVertices();

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(V source, V target);

	/**
	 * Returns the vertex the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeSource(long edgeIndex);

	/**
	 * Returns the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeTarget(long edgeIndex);

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public long getEdgeWeight(V source, V target);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target. Notice that the edge weight might be 0.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getIncomingEdgesOf(V v);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getOutgoingEdgesOf(V v);

	/**
	 * Return an iterable of edgeIndex containing all edges of which v is a
	 * source or a target. Notice that the edge weight might be 0.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getEdgesOf(V v);

	/**
	 * 
	 * @param v
	 * @return the index of the given vertex
	 */
	public int getIndexOfVertex(V v);

	/**
	 * 
	 * @return A copy of the graph that is not connected to this graph. Should
	 *         not clone the vertices themselves.
	 */
	public Graph<V> clone();
}
