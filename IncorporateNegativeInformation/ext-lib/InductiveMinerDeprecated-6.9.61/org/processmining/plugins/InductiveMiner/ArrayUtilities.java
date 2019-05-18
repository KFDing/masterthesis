package org.processmining.plugins.InductiveMiner;

import java.util.Arrays;
import java.util.Set;

import gnu.trove.set.hash.THashSet;

public class ArrayUtilities {

	/**
	 * Returns a set containing the elements of the array
	 * 
	 * @param a
	 * @return
	 */
	public static <X> Set<X> toSet(X[] a) {
		return new THashSet<X>(Arrays.asList(a));
	}

	/**
	 * Returns a new array containing the elements of a followed by the elements
	 * of b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <X> X[] concatenate(X[] a, X[] b) {
		X[] result = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	/**
	 * Returns a new array containing the elements of a followed by the elements
	 * of b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int[] concatenate(int[] a, int[] b) {
		int[] result = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	/**
	 * Returns whether a contains b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <X> boolean contains(X[] a, X b) {
		for (X e : a) {
			if (e.equals(b)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a new array containing all elements in a but not in b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <X extends Comparable<X>> X[] difference(X[] a, X[] b) {

		X[] sortedFirst = Arrays.copyOf(a, a.length); // O(n)
		X[] sortedSecond = Arrays.copyOf(b, b.length); // O(m)
		Arrays.sort(sortedFirst); // O(n log n)
		Arrays.sort(sortedSecond); // O(m log m)

		int firstIndex = 0;
		int secondIndex = 0;

		@SuppressWarnings("unchecked")
		X[] diff = (X[]) new Object[a.length];
		int diffI = 0;

		while (firstIndex < sortedFirst.length && secondIndex < sortedSecond.length) { // O(n + m)
			int compare = (int) Math.signum(sortedFirst[firstIndex].compareTo(sortedSecond[secondIndex]));

			switch (compare) {
				case -1 :
					diff[diffI] = sortedFirst[firstIndex];
					diffI++;
					firstIndex++;
					break;
				case 1 :
					diff[diffI] = sortedSecond[secondIndex];
					diffI++;
					secondIndex++;
					break;
				default :
					firstIndex++;
					secondIndex++;
			}
		}

		//copy the array
		return Arrays.copyOf(diff, diffI);
	}

	/**
	 * Returns the first array within a in which b occurs, or null if b does not
	 * occur anywhere.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <X> X[] findArrayWith(X[][] a, X b) {
		for (X[] SCC : a) {
			for (X c : SCC) {
				if (c.equals(b)) {
					return SCC;
				}
			}
		}
		return null;
	}
}
