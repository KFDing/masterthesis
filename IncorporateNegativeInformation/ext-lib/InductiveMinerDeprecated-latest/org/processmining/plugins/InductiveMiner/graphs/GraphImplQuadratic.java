package org.processmining.plugins.InductiveMiner.graphs;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.IteratorUtils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class GraphImplQuadratic<V> implements Graph<V> {
	private int vertices; //number of vertices
	private long[][] edges; //matrix of weights of edges
	private TObjectIntMap<V> v2index; //map V to its vertex index
	private V[] index2x; //map vertex index to V
	private final Class<?> clazz;

	public GraphImplQuadratic(Class<?> clazz) {
		this(clazz, 1);
	}

	@SuppressWarnings("unchecked")
	public GraphImplQuadratic(Class<?> clazz, int initialSize) {
		vertices = 0;
		edges = new long[initialSize][initialSize];
		v2index = new TObjectIntHashMap<V>(10, 0.5f, -1);

		index2x = (V[]) Array.newInstance(clazz, 0);
		this.clazz = clazz;
	}

	public int addVertex(V x) {
		int newNumber = vertices;
		int oldIndex = v2index.putIfAbsent(x, newNumber);
		if (oldIndex == v2index.getNoEntryValue()) {
			vertices++;
			increaseSizeTo(vertices);
			index2x[newNumber] = x;
			return vertices - 1;
		} else {
			return oldIndex;
		}
	}

	public void addVertices(Collection<V> xs) {
		increaseSizeTo(vertices + xs.size());
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addVertices(V[] xs) {
		increaseSizeTo(vertices + xs.length);
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addEdge(int source, int target, long weight) {
		edges[source][target] += weight;
	}

	public void addEdge(V source, V target, long weight) {
		addVertex(source);
		addVertex(target);
		edges[v2index.get(source)][v2index.get(target)] += weight;
	}

	public void removeEdge(long edge) {
		edges[getEdgeSourceIndex(edge)][getEdgeTargetIndex(edge)] = 0;
	}

	public V getVertexOfIndex(int index) {
		return index2x[index];
	}

	public int getIndexOfVertex(V v) {
		return v2index.get(v);
	}

	public V[] getVertices() {
		return index2x;
	}

	public int[] getVertexIndices() {
		int[] result = new int[vertices];
		for (int i = 0; i < vertices; i++) {
			result[i] = i;
		}
		return result;
	}

	public int getNumberOfVertices() {
		return vertices;
	}

	/**
	 * Gives an iterable that iterates over all edges that have a weight greater
	 * than 0; The edges that returns are indices.
	 * 
	 * @return
	 */
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
	public boolean containsEdge(V source, V target) {
		return edges[v2index.get(source)][v2index.get(target)] > 0;
	}

	/**
	 * Returns whether the graph contains an edge between source and target.
	 * 
	 * @return
	 */
	public boolean containsEdge(int source, int target) {
		return edges[source][target] > 0;
	}

	/**
	 * Returns the vertex the edgeIndex comes from.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeSource(long edgeIndex) {
		return index2x[getEdgeSourceIndex(edgeIndex)];
	}

	public int getEdgeSourceIndex(long edgeIndex) {
		return (int) (edgeIndex / vertices);
	}

	/**
	 * Returns the vertex the edgeIndex points to.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public V getEdgeTarget(long edgeIndex) {
		return index2x[getEdgeTargetIndex(edgeIndex)];
	}

	public int getEdgeTargetIndex(long edgeIndex) {
		return (int) (edgeIndex % vertices);
	}

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param edgeIndex
	 * @return
	 */
	public long getEdgeWeight(long edgeIndex) {
		return edges[getEdgeSourceIndex(edgeIndex)][getEdgeTargetIndex(edgeIndex)];
	}

	public long getEdgeWeight(int source, int target) {
		return edges[source][target];
	}

	/**
	 * Returns the weight of an edge.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public long getEdgeWeight(V source, V target) {
		int sourceIndex = v2index.get(source);
		int targetIndex = v2index.get(target);
		if (sourceIndex == v2index.getNoEntryValue() || targetIndex == v2index.getNoEntryValue()) {
			return 0;
		}
		return edges[sourceIndex][targetIndex];
	}

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * target.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getIncomingEdgesOf(V v) {
		return new EdgeIterableIncoming(v2index.get(v));
	}

	public Iterable<Long> getIncomingEdgesOf(int v) {
		return new EdgeIterableIncoming(v2index.get(v));
	}

	/**
	 * Returns an array of edge index, containing all edges of which v is the
	 * source.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getOutgoingEdgesOf(V v) {
		return new EdgeIterableOutgoing(v2index.get(v));
	}

	public Iterable<Long> getOutgoingEdgesOf(int v) {
		return new EdgeIterableOutgoing(v);
	}

	/**
	 * Return an array of edgeIndex containing all edges of which v is a source
	 * or a target.
	 * 
	 * @param v
	 * @return
	 */
	public Iterable<Long> getEdgesOf(V v) {
		return getEdgesOf(v2index.get(v));
	}

	public Iterable<Long> getEdgesOf(final int indexOfV) {
		return new Iterable<Long>() {

			public Iterator<Long> iterator() {

				//first count every edge, count a self-edge only in the row-run
				int count = 0;
				for (int column = 0; column < vertices; column++) {
					if (column != indexOfV && edges[indexOfV][column] > 0) {
						count++;
					}
				}
				for (int row = 0; row < vertices; row++) {
					if (edges[row][indexOfV] > 0) {
						count++;
					}
				}

				long[] result = new long[count];
				count = 0;
				for (int column = 0; column < vertices; column++) {
					if (column != indexOfV && edges[indexOfV][column] > 0) {
						result[count] = indexOfV * vertices + column;
						count++;
					}
				}
				for (int row = 0; row < vertices; row++) {
					if (edges[row][indexOfV] > 0) {
						result[count] = row * vertices + indexOfV;
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
		index2x = Arrays.copyOf(index2x, size);
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

		protected long next() {
			current = next;
			next++;
			findNext();
			return current * vertices + column;
		}

		private void findNext() {
			while (next < vertices && edges[next][column] == 0) {
				next++;
			}
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

	public Graph<V> clone() {
		GraphImplQuadratic<V> result = new GraphImplQuadratic<V>(clazz, edges.length);
		result.vertices = vertices;
		for (int i = 0; i < edges.length; i++) {
			System.arraycopy(edges[i], 0, result.edges[i], 0, edges[i].length);
		}
		result.v2index.putAll(v2index);
		System.arraycopy(index2x, 0, result.index2x, 0, index2x.length);
		return result;
	}

	public void addVertex(int vertexIndex) {
		throw new RuntimeException("not available");
	}
}
