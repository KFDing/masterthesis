package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

/**
 * Assumes that all nodes are normalised, i.e. their indices are [0..n-1]
 * 
 * @author sander
 *
 */
public interface NormalisedIntGraph extends Cloneable {

	/**
	 * Adds an edge. If the weight becomes 0, the edge is removed.
	 * 
	 * @param normalisedSource
	 * @param normalisedTarget
	 * @param weight
	 */
	public void addEdge(int normalisedSource, int normalisedTarget, long weight);

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
	public boolean containsEdge(int normalisedSource, int normalisedTarget);

	/**
	 * Returns the vertex the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeSourceIndex(long edgeIndex);

	/**
	 * Returns the index of the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public int getEdgeTargetIndex(long edgeIndex);

	/**
	 * Returns the weight of an edge between source and target.
	 * 
	 * @param normalisedSource
	 * @param normalisedTarget
	 * @return
	 */
	public long getEdgeWeight(int normalisedSource, int normalisedTarget);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target. Notice that the edge weight might be 0.
	 * 
	 * @param normalisedNode
	 * @return
	 */
	public Iterable<Long> getIncomingEdgesOf(int normalisedNode);

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source. Notice that the edge weight might be 0.
	 * 
	 * @param normalisedNode
	 * @return
	 */
	public Iterable<Long> getOutgoingEdgesOf(int normalisedNode);

	/**
	 * Return an iterable of edgeIndex containing all edges of which v is a
	 * source or a target. Notice that the edge weight might be 0.
	 * 
	 * @param normalisedNode
	 * @return
	 */
	public Iterable<Long> getEdgesOf(int normalisedNode);

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

	public NormalisedIntGraph clone();
}
