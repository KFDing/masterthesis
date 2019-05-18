package org.processmining.plugins.inductiveminer2.helperclasses.graphs;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections15.IteratorUtils;
import org.processmining.plugins.InductiveMiner.graphs.EdgeIterable;

import gnu.trove.list.array.TIntArrayList;

public class IntGraphImplQuadratic implements IntGraph {
	private TIntArrayList internal2external;
	//private TIntIntMap external2internal;
	private int[] external2internalArray;
	private long[][] edges; //matrix of weights of edges

	public IntGraphImplQuadratic() {
		this(1);
	}

	public IntGraphImplQuadratic(int initialSize) {
		internal2external = new TIntArrayList();
		//external2internal = new TIntIntHashMap(10, 0.5f, -1, -1);
		external2internalArray = new int[] { -1 };
		edges = new long[initialSize][initialSize];
	}

	private int toExternal(int internal) {
		return internal2external.get(internal);
	}

	private int toInternal(int external) {
		//return external2internal.get(external);
		if (external >= external2internalArray.length) {
			return -1;
		}
		return external2internalArray[external];
	}

	@Override
	public void addEdge(int source, int target, long weight) {
		int normalisedSource = toInternal(source);
		int normalisedTarget = toInternal(target);
		edges[normalisedSource][normalisedTarget] += weight;
	}

	/**
	 * Gives an iterable that iterates over all edges that have a weight greater
	 * than 0; The edges that are returned are indices.
	 * 
	 * @return
	 */
	@Override
	public Iterable<Long> getEdges() {
		return new Iterable<Long>() {
			public Iterator<Long> iterator() {
				return new EdgeIterator();
			}
		};
	}

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	@Override
	public boolean containsEdge(int source, int target) {
		int normalisedSource = toInternal(source);
		int normalisedTarget = toInternal(target);
		if (normalisedSource < 0 || normalisedSource > internal2external.size() - 1 || normalisedTarget < 0
				|| normalisedTarget > internal2external.size() - 1) {
			return false;
		}
		return edges[normalisedSource][normalisedTarget] > 0;
	}

	@Override
	public int getEdgeSource(long edgeIndex) {
		return toExternal(getEdgeSourceIndex(edgeIndex));
	}

	@Override
	public int getEdgeSourceIndex(long edgeIndex) {
		return (int) (edgeIndex / internal2external.size());
	}

	@Override
	public int getEdgeTarget(long edgeIndex) {
		return toExternal(getEdgeTargetIndex(edgeIndex));
	}

	@Override
	public int getEdgeTargetIndex(long edgeIndex) {
		return (int) (edgeIndex % internal2external.size());
	}

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	@Override
	public long getEdgeWeight(long edgeIndex) {
		int normalisedSource = getEdgeSourceIndex(edgeIndex);
		int normalisedTarget = getEdgeTargetIndex(edgeIndex);
		if (normalisedSource < 0 || normalisedSource > internal2external.size() - 1 || normalisedTarget < 0
				|| normalisedTarget > internal2external.size() - 1) {
			return 0;
		}
		return edges[normalisedSource][normalisedTarget];
	}

	@Override
	public long getEdgeWeight(int source, int target) {
		int normalisedSource = toInternal(source);
		int normalisedTarget = toInternal(target);
		if (normalisedSource < 0 || normalisedSource > internal2external.size() - 1 || normalisedTarget < 0
				|| normalisedTarget > internal2external.size() - 1) {
			return 0;
		}
		return edges[normalisedSource][normalisedTarget];
	}

	@Override
	public EdgeIterable getIncomingEdgesOf(int node) {
		int normalisedNode = toInternal(node);
		return getIncomingEdgesOfIndex(normalisedNode);
	}

	@Override
	public EdgeIterable getIncomingEdgesOfIndex(int index) {
		if (index < 0 || index > internal2external.size() - 1) {
			return new EdgeIterableEmpty();
		}
		return new EdgeIterableIncoming(index);
	}

	@Override
	public EdgeIterable getOutgoingEdgesOf(int node) {
		int normalisedNode = toInternal(node);
		return getOutgoingEdgesOfIndex(normalisedNode);
	}

	@Override
	public EdgeIterable getOutgoingEdgesOfIndex(int index) {
		if (index < 0 || index > internal2external.size() - 1) {
			return new EdgeIterableEmpty();
		}
		return new EdgeIterableOutgoing(index);
	}

	@Override
	public Iterable<Long> getEdgesOf(int node) {
		final int normalisedNode = toInternal(node);
		if (normalisedNode < 0 || normalisedNode > internal2external.size() - 1) {
			return new EdgeIterableEmpty();
		}
		return new Iterable<Long>() {

			public Iterator<Long> iterator() {

				//first count every edge, count a self-edge only in the row-run
				int count = 0;
				for (int column = 0; column < internal2external.size(); column++) {
					if (column != normalisedNode && edges[normalisedNode][column] > 0) {
						count++;
					}
				}
				for (int row = 0; row < internal2external.size(); row++) {
					if (edges[row][normalisedNode] > 0) {
						count++;
					}
				}

				long[] result = new long[count];
				count = 0;
				for (int column = 0; column < internal2external.size(); column++) {
					if (column != normalisedNode && edges[normalisedNode][column] > 0) {
						result[count] = normalisedNode * internal2external.size() + column;
						count++;
					}
				}
				for (int row = 0; row < internal2external.size(); row++) {
					if (edges[row][normalisedNode] > 0) {
						result[count] = row * internal2external.size() + normalisedNode;
						count++;
					}
				}
				return IteratorUtils.arrayIterator(result);
			}
		};
	}

	/**
	 * Returns the weight of the edge with the highest weight.
	 * 
	 * @return
	 */
	@Override
	public long getWeightOfHeaviestEdge() {
		long max = Long.MIN_VALUE;
		for (long[] v : edges) {
			for (long w : v) {
				if (w > max) {
					max = w;
				}
			}
		}
		return max;
	}

	private void increaseSizeTo(int size) {
		//see if the matrix can still accomodate this vertex
		if (size >= edges.length) {
			int newLength = edges.length * 2;
			while (size >= newLength) {
				newLength = newLength * 2;
			}
			long[][] newEdges = new long[newLength][newLength];

			//copy old values
			for (int i = 0; i < edges.length; i++) {
				System.arraycopy(edges[i], 0, newEdges[i], 0, edges[i].length);
			}
			edges = newEdges;
		}
	}

	private final class EdgeIterableEmpty extends EdgeIterable {

		protected boolean hasNext() {
			return false;
		}

		protected long next() {
			return 0;
		}

		protected void remove() {

		}

	}

	private final class EdgeIterableOutgoing extends EdgeIterable {
		private final int row;
		int next;
		int current;

		private EdgeIterableOutgoing(int row) {
			this.row = row;
			next = 0;
			findNext();
		}

		private void findNext() {
			while (next < internal2external.size() && edges[row][next] == 0) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return row * internal2external.size() + current;
		}

		protected boolean hasNext() {
			return next < internal2external.size();
		}

		protected void remove() {
			edges[row][current] = 0;
		}
	}

	private final class EdgeIterableIncoming extends EdgeIterable {
		private final int column;
		int next;
		int current;

		private EdgeIterableIncoming(int column) {
			this.column = column;
			next = 0;
			findNext();
		}

		private void findNext() {
			while (next < internal2external.size() && edges[next][column] == 0) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return current * internal2external.size() + column;
		}

		protected boolean hasNext() {
			return next < internal2external.size();
		}

		protected void remove() {
			edges[current][column] = 0;
		}
	}

	public class EdgeIterator implements Iterator<Long> {
		int currentIndex = 0;
		int nextIndex = 0;

		public EdgeIterator() {
			//walk to the first non-zero edge
			while (currentIndex < internal2external.size() * internal2external.size()
					&& edges[currentIndex / internal2external.size()][currentIndex % internal2external.size()] <= 0) {
				currentIndex++;
			}
			//and to the next
			nextIndex = currentIndex;
			while (nextIndex < internal2external.size() * internal2external.size()
					&& edges[nextIndex / internal2external.size()][nextIndex % internal2external.size()] <= 0) {
				nextIndex++;
			}
		}

		public void remove() {
			edges[getEdgeSourceIndex(currentIndex)][getEdgeTargetIndex(currentIndex)] = 0;
		}

		public Long next() {
			currentIndex = nextIndex;
			nextIndex++;
			while (nextIndex < internal2external.size() * internal2external.size()
					&& edges[nextIndex / internal2external.size()][nextIndex % internal2external.size()] <= 0) {
				nextIndex++;
			}
			return (long) currentIndex;
		}

		public boolean hasNext() {
			return nextIndex < internal2external.size() * internal2external.size();
		}
	}

	@Override
	public IntGraphImplQuadratic clone() {
		IntGraphImplQuadratic result = new IntGraphImplQuadratic(0);
		result.internal2external = new TIntArrayList(internal2external);
		result.external2internalArray = Arrays.copyOf(external2internalArray, external2internalArray.length);
		//result.external2internal = new TIntIntHashMap(external2internal);
		result.edges = new long[edges.length][];
		for (int i = 0; i < edges.length; i++) {
			result.edges[i] = new long[edges[i].length];
			System.arraycopy(edges[i], 0, result.edges[i], 0, edges[i].length);
		}
		return result;
	}

	public void addNode(int node) {

		//make sure the external2internal map has enough space
		if (external2internalArray.length <= node) {
			int newSize = external2internalArray.length;
			while (newSize <= node) {
				newSize *= 2;
			}
			int[] newArray = new int[newSize];
			Arrays.fill(newArray, -1);
			System.arraycopy(external2internalArray, 0, newArray, 0, external2internalArray.length);
			external2internalArray = newArray;
		}

		if (external2internalArray[node] == -1) {
			external2internalArray[node] = internal2external.size();
			internal2external.add(node);
			increaseSizeTo(internal2external.size());
		}
	}

	public int[] getNodes() {
		//return external2internal.keys();
		return internal2external.toArray();
	}

	public int getNumberOfNodes() {
		//return external2internal.size();
		return internal2external.size();
	}

	public int getNodeOfIndex(int index) {
		return internal2external.get(index);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("digraph G {");
		for (long edge : getEdges()) {
			result.append(getEdgeSource(edge) + " -> " + getEdgeTarget(edge) + " [label=\"" + edge + "\"];");
		}
		result.append("}");
		return result.toString();
	}
}
