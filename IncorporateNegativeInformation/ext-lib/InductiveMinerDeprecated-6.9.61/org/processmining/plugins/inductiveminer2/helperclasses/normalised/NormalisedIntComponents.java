package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.primitives.Ints;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Assumes that the nodes are numbered [0..n].
 * 
 * @author sander
 *
 */
public class NormalisedIntComponents {

	private final int[] node2component;
	private int numberOfComponents;
	private int numberOfNodes;

	public NormalisedIntComponents(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
		node2component = new int[numberOfNodes];
		numberOfComponents = numberOfNodes;
		for (int i = 0; i < node2component.length; i++) {
			node2component[i] = i;
		}
	}

	public NormalisedIntComponents(Collection<? extends TIntCollection> partition) {
		numberOfNodes = 0;
		for (TIntCollection part : partition) {
			numberOfNodes += part.size();
		}
		numberOfComponents = partition.size();
		node2component = new int[numberOfNodes];

		int componentNumber = 0;
		for (TIntCollection component : partition) {
			for (TIntIterator it = component.iterator(); it.hasNext();) {
				node2component[it.next()] = componentNumber;
			}
			componentNumber++;
		}
	}

	/**
	 * Merge the components of the two nodes. If they are in the same component,
	 * runs in O(1). If they are not, runs in O(n) (n = number of nodes).
	 * 
	 * @param normalisedA
	 * @param normalisedB
	 */
	public void mergeComponentsOf(int normalisedA, int normalisedB) {
		int source = node2component[normalisedA];
		int target = node2component[normalisedB];

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
			for (int i = 0; i < node2component.length; i++) {
				if (node2component[i] == componentA) {
					node2component[i] = componentB;
					found = true;
				}
			}
			if (found) {
				numberOfComponents--;
			}
		}
	}

	public boolean areInSameComponent(int normalisedA, int normalisedB) {
		return node2component[normalisedA] == node2component[normalisedB];
	}

	public int getComponentOf(int node) {
		return node2component[node];
	}

	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public List<TIntSet> getComponents() {
		final List<TIntSet> result = new ArrayList<>(numberOfComponents);

		//find the highest component number
		int highestComponentIndex = Integer.MIN_VALUE;
		for (int node = 0; node < node2component.length; node++) {
			highestComponentIndex = Math.max(highestComponentIndex, node2component[node]);
		}

		//prepare the result
		for (int i = 0; i <= highestComponentIndex; i++) {
			result.add(new TIntHashSet(10, 0.5f, Integer.MIN_VALUE));
		}

		//put each node in its component
		for (int node = 0; node < numberOfNodes; node++) {
			int component = node2component[node];
			result.get(component).add(node);
		}

		//remove the empty components
		for (Iterator<TIntSet> it = result.iterator(); it.hasNext();) {
			if (it.next().isEmpty()) {
				it.remove();
			}
		}

		return result;
	}

	public Iterable<Integer> getNodeIndicesOfComponent(final int componentIndex) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = -1;

					public Integer next() {
						for (int i = now + 1; i < node2component.length; i++) {
							if (node2component[i] == componentIndex) {
								now = i;
								return now;
							}
						}
						return null;
					}

					public boolean hasNext() {
						for (int i = now + 1; i < node2component.length; i++) {
							if (node2component[i] == componentIndex) {
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
		int max = Ints.max(node2component);
		int[] old2new = new int[max + 1];
		Arrays.fill(old2new, -1);
		for (int old : node2component) {
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
		for (int i = 0; i < node2component.length; i++) {
			node2component[i] = old2new[node2component[i]];
		}

		return old2new;
	}
}