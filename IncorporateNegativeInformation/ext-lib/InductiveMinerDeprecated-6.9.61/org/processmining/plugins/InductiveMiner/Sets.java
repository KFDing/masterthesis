package org.processmining.plugins.InductiveMiner;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import gnu.trove.TIntCollection;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class Sets {
	public static <X> Set<X> extend(Set<X> base, X node) {
		Set<X> result = new THashSet<X>(base);
		result.add(node);
		return result;
	}

	public static <X> Set<X> union(Set<X> a, Set<X> b) {
		Set<X> result = new THashSet<X>(a);
		result.addAll(b);
		return result;
	}

	public static <X> Collection<X> difference(Collection<X> a, Set<X> b) {
		Collection<X> result = new THashSet<X>(a);
		result.removeAll(b);
		return result;
	}

	public static <X> Set<X> intersection(Set<X> a, Set<X> b) {
		Set<X> result = new THashSet<X>(a);
		result.retainAll(b);
		return result;
	}

	public static TIntSet intersection(TIntCollection a, TIntCollection b) {
		TIntSet result = new TIntHashSet(a);
		result.retainAll(b);
		return result;
	}

	public static TIntSet intersection(TIntCollection a, int[] b) {
		TIntSet result = new TIntHashSet(a);
		result.retainAll(b);
		return result;
	}

	public static <X> Set<X> flatten(Set<Set<X>> set) {
		Set<X> result = new THashSet<X>();
		for (Set<X> node : set) {
			result.addAll(node);
		}
		return result;
	}

	public static TIntSet flattenInt(Set<TIntSet> set) {
		TIntSet result = new TIntHashSet(10, 0.5f, Integer.MIN_VALUE);
		for (TIntSet node : set) {
			result.addAll(node);
		}
		return result;
	}

	public static <X> Set<X> complement(Set<X> set, Set<X> universe) {
		Set<X> result = new THashSet<X>(universe);
		result.removeAll(set);
		return result;
	}

	public static TIntSet complement(TIntSet set, int[] universe) {
		TIntSet result = new TIntHashSet(10, 0.5f, Integer.MIN_VALUE);
		result.addAll(universe);
		result.removeAll(set);
		return result;
	}

	public static <X> X[] toArray(Class<?> clazz, Collection<X> a) {
		@SuppressWarnings("unchecked")
		X[] result = (X[]) Array.newInstance(clazz, a.size());
		int i = 0;
		Iterator<X> it = a.iterator();
		while (it.hasNext()) {
			result[i] = it.next();
			i++;
		}
		return result;
	}

	public static <X> Set<X> findComponentWith(Collection<Set<X>> Components, X c) {
		Set<X> result = null;
		for (Set<X> SCC : Components) {
			if (SCC.contains(c)) {
				result = SCC;
			}
		}
		return result;
	}

	public static TIntSet findComponentWith(Collection<TIntSet> Components, int c) {
		TIntSet result = null;
		for (TIntSet SCC : Components) {
			if (SCC.contains(c)) {
				result = SCC;
			}
		}
		return result;
	}

	public static <X> String implode(Collection<X> input, String glueString) {
		String output = "";
		boolean first = true;
		if (input.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (X e : input) {
				if (first) {
					first = false;
				} else {
					sb.append(glueString);
				}
				sb.append(e.toString());
			}
			output = sb.toString();
		}
		return output;
	}
}
