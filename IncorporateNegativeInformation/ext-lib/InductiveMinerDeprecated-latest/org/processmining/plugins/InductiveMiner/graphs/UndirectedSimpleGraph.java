package org.processmining.plugins.InductiveMiner.graphs;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * No self-edges, no arc weights. Quadratic implementation. Supports negation.
 * 
 * @author sleemans
 *
 */
public class UndirectedSimpleGraph<V> implements UndirectedGraph<V> {

	private final TObjectIntHashMap<V> v2index; //map V to its vertex index
	private final ArrayList<V> index2v; //map vertex index to V
	private final Class<?> clazz;

	private BitSet edges;

	public UndirectedSimpleGraph(Class<?> clazz, Collection<V> vertices) {
		v2index = new TObjectIntHashMap<V>(10, 0.5f, -1);
		index2v = new ArrayList<>();
		this.clazz = clazz;
		for (V x : vertices) {
			addVertex(x);
		}

		edges = new BitSet(vertices.size() * (vertices.size() - 1) / 2);
	}

	private int addVertex(V x) {
		int newNumber = index2v.size();
		int oldIndex = v2index.putIfAbsent(x, newNumber);
		if (oldIndex == v2index.getNoEntryValue()) {
			index2v.add(x);
			return index2v.size() - 1;
		} else {
			return oldIndex;
		}
	}

	public void addEdge(int a, int b) {
		if (a > b) {
			addEdge(b, a);
			return;
		}
		assert (a < b);
		edges.set(getEdgeNumber(a, b));
	}

	public boolean hasEdge(int a, int b) {
		if (a > b) {
			return hasEdge(b, a);
		}
		assert (a < b);
		return edges.get(getEdgeNumber(a, b));
	}

	public int getEdgeNumber(int a, int b) {
		assert (a < b);
		return b * (b - 1) / 2 + a;
	}

	/**
	 * Invert all edges.
	 */
	public void invert() {
		edges.flip(0, (index2v.size() * (index2v.size()- 1) / 2));
	}

	public Iterable<Long> getEdges() {
		return new Iterable<Long>() {
			public Iterator<Long> iterator() {
				return new Iterator<Long>() {
					int now = -1;

					public Long next() {
						now = edges.nextSetBit(now + 1);
						return (long) now;
					}

					public boolean hasNext() {
						return edges.nextSetBit(now + 1) != -1;
					}
					
					public void remove() {
						
					}
				};
			}
		};
	}
	
	public V getEdgeNodeA(long edgeIndex) {
		return index2v.get(getEdgeNodeIndexA(edgeIndex));
	}

	public int getEdgeNodeIndexA(long edgeIndex) {
		for (int a = 0;; a++) {
			if (a * (a - 1) / 2 > edgeIndex) {
				a = a - 1;
				return a;
			}
		}
	}

	public V getEdgeNodeB(long edgeIndex) {
		return index2v.get(getEdgeNodeIndexB(edgeIndex));
	}
	
	public int getEdgeNodeIndexB(long edgeIndex) {
		for (int a = 0;; a++) {
			if (a * (a - 1) / 2 > edgeIndex) {
				a = a - 1;
				return (int) (edgeIndex - (a * (a - 1) / 2));
			}
		}
	}

	public V[] getVertices() {
		@SuppressWarnings("unchecked")
		V[] result = (V[]) Array.newInstance(clazz, index2v.size());
		return index2v.toArray(result);
	}

}
