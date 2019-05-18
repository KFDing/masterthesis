package org.processmining.plugins.inductiveminer2.helperclasses.graphs;

import org.processmining.plugins.InductiveMiner.graphs.EdgeIterable;

public interface IntGraph extends Cloneable {

	/**
	 * Adds a node to the graph. Has no effect if the node was already present.
	 * 
	 * @param node
	 */
	public void addNode(int node);

	/**
	 * 
	 * @return an array of the vertices in the graph.
	 */
	public int[] getNodes();

	/**
	 * Adds an edge. If the weight becomes 0, the edge is removed.
	 * 
	 * @param Source
	 * @param Target
	 * @param weight
	 */
	public void addEdge(int Source, int Target, long weight);

	/**
	 * Gives an iterable that iterates over all edges; The edges that are
	 * returned are indices. Notice that the edge weight might NOT be 0.
	 * 
	 * @return
	 */
	public Iterable<Long> getEdges();

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(int source, int target);

	/**
	 * Returns the node the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeSource(long edgeIndex);

	/**
	 * Returns the node the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeTarget(long edgeIndex);

	/**
	 * Returns the index of the node the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeTargetIndex(long edgeIndex);

	/**
	 * Returns the weight of an edge between source and target.
	 * 
	 * @param Source
	 * @param Target
	 * @return
	 */
	public long getEdgeWeight(int Source, int Target);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target. Notice that the edge weight might be 0.
	 * 
	 * @param Node
	 * @return
	 */
	public Iterable<Long> getIncomingEdgesOf(int Node);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source. Notice that the edge weight might be 0.
	 * 
	 * @param Node
	 * @return
	 */
	public Iterable<Long> getOutgoingEdgesOf(int Node);

	/**
	 * Return an iterable of edgeIndex containing all edges of which v is a
	 * source or a target. Notice that the edge weight might be 0.
	 * 
	 * @param node
	 * @return
	 */
	public Iterable<Long> getEdgesOf(int node);

	/**
	 * Returns the weight of the edge with the highest weight.
	 * 
	 * @return
	 */
	public long getWeightOfHeaviestEdge();

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public long getEdgeWeight(long edgeIndex);

	public IntGraph clone();

	public int getNumberOfNodes();

	/**
	 * The graph keeps an index of nodes. With this function, you can look up
	 * these indices.
	 * 
	 * @param index
	 * @return
	 */
	public int getNodeOfIndex(int index);

	public int getEdgeSourceIndex(long edgeIndex);

	public EdgeIterable getOutgoingEdgesOfIndex(int v);
	
	public EdgeIterable getIncomingEdgesOfIndex(int v);
}
