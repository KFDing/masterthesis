package org.processmining.plugins.InductiveMiner.graphs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

public class GraphImplLinearEdge<V> implements Graph<V> {

	private final TObjectIntHashMap<V> v2index; //map V to its vertex index
	private final ArrayList<V> index2v; //map vertex index to V

	private final TIntArrayList sources;
	private final TIntArrayList targets;
	private final TLongArrayList weights;

	private final Class<?> clazz;

	public GraphImplLinearEdge(Class<?> clazz) {
		v2index = new TObjectIntHashMap<V>(10, 0.5f, -1);
		index2v = new ArrayList<>();

		sources = new TIntArrayList();
		targets = new TIntArrayList();
		weights = new TLongArrayList();
		this.clazz = clazz;
	}

	public int addVertex(V x) {
		int newNumber = index2v.size();
		int oldIndex = v2index.putIfAbsent(x, newNumber);
		if (oldIndex == v2index.getNoEntryValue()) {
			index2v.add(x);
			return index2v.size() - 1;
		} else {
			return oldIndex;
		}
	}

	public void addVertices(Collection<V> xs) {
		for (V x : xs) {
			addVertex(x);
		}
	}

	public void addVertices(V[] xs) {
		for (V x : xs) {
			addVertex(x);
		}
	}

	private int compare(int source1, int target1, int source2, int target2) {
		if (source1 < source2) {
			return 1;
		} else if (source1 > source2) {
			return -1;
		} else if (target1 < target2) {
			return 1;
		} else if (target1 > target2) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Similar to Arrays.binarySearch, but works on both sources and targets
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	private int binarySearch(int source, int target) {
		int low = 0;
		int high = sources.size() - 1;
		int mid;
		int midVal;

		while (low <= high) {
			mid = (low + high) >>> 1;
			midVal = compare(source, target, sources.get(mid), targets.get(mid));

			if (midVal < 0)
				low = mid + 1;
			else if (midVal > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public void addEdge(int source, int target, long weight) {
		assert (source >= 0);
		assert (target >= 0);

		//idea: keep the sources and targets sorted; first on source then on target
		int from = binarySearch(source, target);
		if (from >= 0) {
			weights.set(from, weights.get(from) + weight);

			if (weights.get(from) == 0) {
				removeEdge(from);
			}
		} else {
			sources.insert(~from, source);
			targets.insert(~from, target);
			weights.insert(~from, weight);
		}
	}

	public void removeEdge(long edge) {
		assert (edge <= Integer.MAX_VALUE);
		sources.remove((int) edge, 1);
		targets.remove((int) edge, 1);
		weights.remove((int) edge, 1);
	}

	public void addEdge(V source, V target, long weight) {
		addEdge(addVertex(source), addVertex(target), weight);
	}

	public V getVertexOfIndex(int index) {
		return index2v.get(index);
	}

	public V[] getVertices() {
		@SuppressWarnings("unchecked")
		V[] result = (V[]) Array.newInstance(clazz, index2v.size());
		return index2v.toArray(result);
	}

	public int[] getVertexIndices() {
		int[] result = new int[index2v.size()];
		for (int i = 0; i < index2v.size(); i++) {
			result[i] = i;
		}
		return result;
	}

	public int getNumberOfVertices() {
		return index2v.size();
	}

	public Iterable<Long> getEdges() {
		return new EdgeIterable() {
			int next = 0;
			int current = 0;

			public boolean hasNext() {
				return next != sources.size();
			}

			public long next() {
				current = next;
				next++;
				return current;
			}

			protected void remove() {
				targets.removeAt(current);
				sources.removeAt(current);
				weights.removeAt(current);
				current--;
				next--;
			}
		};
	}

	public boolean containsEdge(V source, V target) {
		return containsEdge(v2index.get(source), v2index.get(target));
	}

	public boolean containsEdge(int source, int target) {
		return binarySearch(source, target) >= 0;
	}

	public V getEdgeSource(long edgeIndex) {
		return index2v.get(sources.get((int) edgeIndex));
	}

	public int getEdgeSourceIndex(long edgeIndex) {
		return sources.get((int) edgeIndex);
	}

	public V getEdgeTarget(long edgeIndex) {
		return index2v.get(targets.get((int) edgeIndex));
	}

	public int getEdgeTargetIndex(long edgeIndex) {
		return targets.get((int) edgeIndex);
	}

	public long getEdgeWeight(long edgeIndex) {
		return weights.get((int) edgeIndex);
	}

	public long getEdgeWeight(int source, int target) {
		int from = binarySearch(source, target);
		if (from >= 0) {
			return weights.get(from);
		} else {
			return 0;
		}
	}

	public long getEdgeWeight(V source, V target) {
		return getEdgeWeight(v2index.get(source), v2index.get(target));
	}

	public Iterable<Long> getIncomingEdgesOf(V v) {
		return new EdgeIterableIncoming(v2index.get(v));
	}

	public Iterable<Long> getIncomingEdgesOf(int v) {
		return new EdgeIterableIncoming(v);
	}

	public Iterable<Long> getOutgoingEdgesOf(V v) {
		return new EdgeIterableOutgoing(v2index.get(v));
	}

	public Iterable<Long> getOutgoingEdgesOf(int v) {
		return new EdgeIterableOutgoing(v);
	}

	public Iterable<Long> getEdgesOf(V v) {
		return getEdgesOf(v2index.get(v));
	}

	public Iterable<Long> getEdgesOf(int indexOfV) {
		return new EdgeIterableBoth(indexOfV);
	}

	public long getWeightOfHeaviestEdge() {
		long max = 0;
		for (int e = 0; e < weights.size(); e++) {
			max = Math.max(max, weights.get(e));
		}
		return max;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < sources.size(); i++) {
			result.append(index2v.get(sources.get(i)) + "->" + index2v.get(targets.get(i)) + "x" + weights.get(i));
			result.append(", ");
		}
		return result.toString();
	}

	@Override
	public Graph<V> clone() {
		GraphImplLinearEdge<V> result = new GraphImplLinearEdge<>(clazz);
		result.v2index.putAll(v2index);
		result.index2v.addAll(index2v);
		result.sources.addAll(sources);
		result.targets.addAll(targets);
		result.weights.addAll(weights);
		return result;
	}

	public int getIndexOfVertex(V v) {
		return v2index.get(v);
	}

	private final class EdgeIterableIncoming extends EdgeIterable {
		private final int target;
		int next = 0;
		int current = -1;

		private EdgeIterableIncoming(int target) {
			this.target = target;
			next = 0;
			findNext();
		}

		protected boolean hasNext() {
			return next < targets.size();
		}

		private void findNext() {
			while (next < targets.size() && targets.get(next) != target) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return current;
		}

		protected void remove() {
			targets.removeAt(current);
			sources.removeAt(current);
			weights.removeAt(current);
			current--;
			next--;
			findNext();
		}
	}

	private final class EdgeIterableOutgoing extends EdgeIterable {
		private final int source;
		int next = 0;
		int current = 0;

		private EdgeIterableOutgoing(int source) {
			this.source = source;
			int from = sources.binarySearch(source);
			if (from < 0) {
				return;
			}
			while (from >= 0 && sources.get(from) == source) {
				from--;
			}
			next = from + 1;
			current = -1;
		}

		protected boolean hasNext() {
			return next < sources.size() && sources.get(next) == source;
		}

		protected long next() {
			current = next;
			next++;
			return current;
		}

		protected void remove() {
			sources.removeAt(current);
			targets.removeAt(current);
			weights.removeAt(current);
			next--;
			current--;
		}
	}

	private final class EdgeIterableBoth extends EdgeIterable {
		private final int vertexIndex;
		int next = 0;
		int current = 0;

		private EdgeIterableBoth(int vertexIndex) {
			this.vertexIndex = vertexIndex;
			next = 0;
			findNext();
		}

		protected boolean hasNext() {
			return next < targets.size();
		}

		private void findNext() {
			while (next < targets.size() && targets.get(next) != vertexIndex && sources.get(next) != vertexIndex) {
				next++;
			}
		}

		protected long next() {
			current = next;
			next++;
			findNext();
			return current;
		}

		protected void remove() {
			targets.removeAt(current);
			sources.removeAt(current);
			weights.removeAt(current);
			current--;
			next--;
			findNext();
		}
	}

	public void addVertex(int vertexIndex) {
		throw new RuntimeException("not available");
	}
}
