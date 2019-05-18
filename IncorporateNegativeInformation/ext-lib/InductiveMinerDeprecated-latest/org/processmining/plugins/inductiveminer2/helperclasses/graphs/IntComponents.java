package org.processmining.plugins.inductiveminer2.helperclasses.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.primitives.Ints;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class IntComponents {

	private final int[] components;
	private int numberOfComponents;
	private final TIntIntHashMap node2index;

	public IntComponents(int[] nodes) {
		components = new int[nodes.length];
		numberOfComponents = nodes.length;
		for (int i = 0; i < components.length; i++) {
			components[i] = i;
		}

		node2index = new TIntIntHashMap(10, 0.5f, -1, -1);
		{
			int i = 0;
			for (int node : nodes) {
				node2index.put(node, i);
				i++;
			}
		}
	}

	public IntComponents(Collection<? extends TIntCollection> partition) {
		numberOfComponents = partition.size();
		node2index = new TIntIntHashMap(10, 0.5f, -1, -1);
		int nodeNumber = 0;
		for (TIntCollection component : partition) {
			for (TIntIterator it = component.iterator(); it.hasNext();) {
				int node = it.next();
				node2index.put(node, nodeNumber);
				nodeNumber++;
			}
		}
		components = new int[nodeNumber];

		int componentNumber = 0;
		nodeNumber = 0;
		for (TIntCollection component : partition) {
			for (TIntIterator it = component.iterator(); it.hasNext();) {
				it.next();
				components[nodeNumber] = componentNumber;
				nodeNumber++;
			}
			componentNumber++;
		}
	}

	/**
	 * Merge the components of the two nodes. If they are in the same component,
	 * runs in O(1). If they are not, runs in O(n) (n = number of nodes).
	 * 
	 * @param nodeA
	 * @param nodeB
	 */
	public void mergeComponentsOf(int nodeA, int nodeB) {
		int source = components[node2index.get(nodeA)];
		int target = components[node2index.get(nodeB)];

		mergeComponents(source, target);
	}

	/**
	 * Merge two components. The second component is kept.
	 * 
	 * @param componentA
	 * @param componentB
	 */
	public void mergeComponents(int componentA, int componentB) {
		if (componentA != componentB) {
			boolean found = false;
			for (int i = 0; i < components.length; i++) {
				if (components[i] == componentA) {
					components[i] = componentB;
					found = true;
				}
			}
			if (found) {
				numberOfComponents--;
			}
		}
	}

	public boolean areInSameComponent(int nodeA, int nodeB) {
		return components[node2index.get(nodeA)] == components[node2index.get(nodeB)];
	}

	public int getComponentOf(int node) {
		return components[node2index.get(node)];
	}

	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public List<TIntSet> getComponents() {
		final List<TIntSet> result = new ArrayList<>(numberOfComponents);

		//prepare a hashmap of components
		final TIntIntHashMap component2componentIndex = new TIntIntHashMap();
		int highestComponentIndex = 0;
		for (int node = 0; node < components.length; node++) {
			int component = components[node];
			if (!component2componentIndex.contains(component)) {
				component2componentIndex.put(component, highestComponentIndex);
				highestComponentIndex++;
				result.add(new TIntHashSet(10, 0.5f, -1));
			}
		}

		//put each node in its component
		node2index.forEachEntry(new TIntIntProcedure() {
			public boolean execute(int node, int nodeIndex) {
				int component = components[nodeIndex];
				int componentIndex = component2componentIndex.get(component);
				result.get(componentIndex).add(node);
				return true;
			}
		});

		return result;
	}

	public Iterable<Integer> getNodeIndicesOfComponent(final int componentIndex) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = -1;

					public Integer next() {
						for (int i = now + 1; i < components.length; i++) {
							if (components[i] == componentIndex) {
								now = i;
								return now;
							}
						}
						return null;
					}

					public boolean hasNext() {
						for (int i = now + 1; i < components.length; i++) {
							if (components[i] == componentIndex) {
								return true;
							}
						}
						return false;
					}

					public void remove() {

					}
				};
			}
		};

	}

	/**
	 * Put the components in increasing order 0...n
	 * 
	 * @return The mapping from old component to new component.
	 */
	public int[] normalise() {
		int max = Ints.max(components);
		int[] old2new = new int[max + 1];
		Arrays.fill(old2new, -1);
		for (int old : components) {
			old2new[old] = old;
		}

		int newv = 0;
		for (int i = 0; i < old2new.length; i++) {
			if (old2new[i] != -1) {
				old2new[i] = newv;
				newv++;
			}
		}

		//replace
		for (int i = 0; i < components.length; i++) {
			components[i] = old2new[components[i]];
		}

		return old2new;
	}
}