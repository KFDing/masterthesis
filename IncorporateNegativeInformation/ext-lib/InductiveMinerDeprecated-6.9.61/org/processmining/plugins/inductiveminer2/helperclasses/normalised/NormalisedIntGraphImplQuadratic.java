package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections15.IteratorUtils;
import org.processmining.plugins.InductiveMiner.graphs.EdgeIterable;

public class NormalisedIntGraphImplQuadratic implements NormalisedIntGraph {
	private int vertices; //number of vertices
	private long[][] edges; //matrix of weights of edges

	public NormalisedIntGraphImplQuadratic() {
		this(1);
	}

	public NormalisedIntGraphImplQuadratic(int initialSize) {
		vertices = initialSize;
		edges = new long[initialSize][initialSize];
	}

	@Override
	public void addEdge(int normalisedSource, int normalisedTarget, long weight) {
		increaseSizeTo(normalisedSource);
		increaseSizeTo(normalisedTarget);
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
	public boolean containsEdge(int normalisedSource, int normalisedTarget) {
		if (normalisedSource > vertices - 1 || normalisedTarget > vertices - 1) {
			return false;
		}
		return edges[normalisedSource][normalisedTarget] > 0;
	}

	@Override
	public int getEdgeSourceIndex(long edgeIndex) {
		return (int) (edgeIndex / vertices);
	}

	@Override
	public int getEdgeTargetIndex(long edgeIndex) {
		return (int) (edgeIndex % vertices);
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
		if (normalisedSource > vertices - 1 || normalisedTarget > vertices - 1) {
			return 0;
		}
		return edges[normalisedSource][normalisedTarget];
	}

	@Override
	public long getEdgeWeight(int normalisedSource, int normalisedTarget) {
		if (normalisedSource > vertices - 1 || normalisedTarget > vertices - 1) {
			return 0;
		}
		return edges[normalisedSource][normalisedTarget];
	}

	@Override
	public EdgeIterable getIncomingEdgesOf(int normalisedNode) {
		if (normalisedNode > vertices - 1) {
			return new EdgeIterableEmpty();
		}
		return new EdgeIterableIncoming(normalisedNode);
	}

	@Override
	public EdgeIterable getOutgoingEdgesOf(int normalisedNode) {
		if (normalisedNode > vertices - 1) {
			return new EdgeIterableEmpty();
		}
		return new EdgeIterableOutgoing(normalisedNode);
	}

	@Override
	public Iterable<Long> getEdgesOf(final int normalisedNode) {
		if (normalisedNode > vertices - 1) {
			return new EdgeIterableEmpty();
		}
		return new Iterable<Long>() {

			public Iterator<Long> iterator() {

				//first count every edge, count a self-edge only in the row-run
				int count = 0;
				for (int column = 0; column < vertices; column++) {
					if (column != normalisedNode && edges[normalisedNode][column] > 0) {
						count++;
					}
				}
				for (int row = 0; row < vertices; row++) {
					if (edges[row][normalisedNode] > 0) {
						count++;
					}
				}

				long[] result = new long[count];
				count = 0;
				for (int column = 0; column < vertices; column++) {
					if (column != normalisedNode && edges[normalisedNode][column] > 0) {
						result[count] = normalisedNode * vertices + column;
						count++;
					}
				}
				for (int row = 0; row < vertices; row++) {
					if (edges[row][normalisedNode] > 0) {
						result[count] = row * vertices + normalisedNode;
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
			for (int i = 0; i < vertices; i++) {
				for (int j = 0; j < vertices; j++) {
					newEdges[i][j] = edges[i][j];
				}
			}
			edges = newEdges;
		}
		if (vertices < size + 1) {
			vertices = size + 1;
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
			while (next < vertices && edges[row][next] == 0) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return row * vertices + current;
		}

		protected boolean hasNext() {
			return next < vertices;
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
			while (next < vertices && edges[next][column] == 0) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return current * vertices + column;
		}

		protected boolean hasNext() {
			return next < vertices;
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
			while (currentIndex < vertices * vertices && edges[currentIndex / vertices][currentIndex % vertices] <= 0) {
				currentIndex++;
			}
			//and to the next
			nextIndex = currentIndex;
			while (nextIndex < vertices * vertices && edges[nextIndex / vertices][nextIndex % vertices] <= 0) {
				nextIndex++;
			}
		}

		public void remove() {
			edges[getEdgeSourceIndex(currentIndex)][getEdgeTargetIndex(currentIndex)] = 0;
		}

		public Long next() {
			currentIndex = nextIndex;
			nextIndex++;
			while (nextIndex < vertices * vertices && edges[nextIndex / vertices][nextIndex % vertices] <= 0) {
				nextIndex++;
			}
			return (long) currentIndex;
		}

		public boolean hasNext() {
			return nextIndex < vertices * vertices;
		}
	}

	@Override
	public NormalisedIntGraphImplQuadratic clone() {
		NormalisedIntGraphImplQuadratic result = new NormalisedIntGraphImplQuadratic(vertices);
		result.vertices = vertices;
		result.edges = new long[edges.length][];
		for (int i = 0; i < edges.length; i++) {
			result.edges[i] = new long[edges[i].length];
			System.arraycopy(edges[i], 0, result.edges[i], 0, edges[i].length);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < edges.length; i++) {
			result.append(Arrays.toString(edges[i]));
			result.append("\n");
		}
		return result.toString();
	}
}
