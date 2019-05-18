package org.processmining.plugins.InductiveMiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.hash.TObjectLongHashMap;

public class MultiSet<X> implements Iterable<X> {

	protected TObjectLongHashMap<X> cardinalities;
	protected long size;

	public MultiSet() {
		//use a linked hash map here, as it provides O(1) iteration complexity
		cardinalities = new TObjectLongHashMap<X>();
		size = 0;
	}

	public boolean add(X element) {
		add(element, 1);
		return true;
	}

	public boolean equals(Object a) {
		if (!(a instanceof MultiSet<?>)) {
			return false;
		}

		MultiSet<?> aM = (MultiSet<?>) a;

		for (Object e : this) {
			if (aM.getCardinalityOf(e) != this.getCardinalityOf(e)) {
				return false;
			}
		}

		for (Object e : aM) {
			if (aM.getCardinalityOf(e) != this.getCardinalityOf(e)) {
				return false;
			}
		}

		return true;
	}

	public boolean add(X element, long cardinality) {
		assert (cardinality >= 0);

		if (!cardinalities.containsKey(element)) {
			cardinalities.put(element, cardinality);
		} else {
			Long newCardinality = cardinalities.get(element) + cardinality;
			cardinalities.put(element, newCardinality);
		}
		size += cardinality;
		return true;
	}

	public boolean remove(X element, long cardinality) {
		assert (cardinality >= 0);

		Long oldCardinality = getCardinalityOf(element);
		if (oldCardinality - cardinality > 0) {
			cardinalities.put(element, cardinalities.get(element) - cardinality);
			size -= cardinality;
		} else {
			cardinalities.remove(element);
			size -= oldCardinality;
		}

		return true;
	}

	public boolean remove(X element) {
		long oldCardinality = getCardinalityOf(element);
		cardinalities.remove(element);
		size -= oldCardinality;
		return true;
	}

	/**
	 * Add each element of the collection, each with cardinality 1.
	 * 
	 * @param collection
	 * @return
	 */
	public boolean addAll(Collection<X> collection) {
		for (X element : collection) {
			add(element);
		}
		return true;
	}

	/**
	 * Add each element of the collection, each with cardinality as given.
	 * 
	 * @param collection
	 * @param cardinality
	 * @return
	 */
	public boolean addAll(Collection<X> collection, long cardinality) {
		for (X element : collection) {
			add(element, cardinality);
		}
		return true;
	}

	public boolean addAll(MultiSet<X> collection) {
		for (X element : collection) {
			add(element, collection.getCardinalityOf(element));
		}
		return true;
	}

	public void empty() {
		cardinalities = new TObjectLongHashMap<X>();
		size = 0;
	}

	public long size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int setSize() {
		return cardinalities.keySet().size();
	}

	public Set<X> toSet() {
		return Collections.unmodifiableSet(cardinalities.keySet());
	}

	public boolean contains(Object a) {
		return cardinalities.containsKey(a) && cardinalities.get(a) > 0;
	}

	public long getCardinalityOf(Object e) {
		if (contains(e)) {
			return cardinalities.get(e);
		} else {
			return 0;
		}
	}

	/**
	 * Iterator over the elements of the multiset as if it were a set. Get
	 * cardinalities using getCardinality().
	 */
	public Iterator<X> iterator() {

		Iterator<X> it = new Iterator<X>() {
			private TObjectLongIterator<X> it3 = cardinalities.iterator();

			@Override
			public boolean hasNext() {
				return it3.hasNext();
			}

			@Override
			public X next() {
				it3.advance();
				return it3.key();
			}

			public void remove() {
				it3.remove();
			}
		};
		return it;
	}

	public MultiSet<X> copy() {
		MultiSet<X> result = new MultiSet<X>();
		for (X element : cardinalities.keySet()) {
			result.add(element, cardinalities.get(element));
		}
		return result;
	}

	private class CardinalityComparator implements Comparator<X> {
		public int compare(X arg0, X arg1) {
			return Long.valueOf(getCardinalityOf(arg0)).compareTo(Long.valueOf(getCardinalityOf(arg1)));
		}
	}

	/**
	 * Returns a list of the elements, sorted by their cardinality.
	 * 
	 * @return
	 */
	public List<X> sortByCardinality() {
		List<X> result = new ArrayList<X>(toSet());
		Collections.sort(result, new CardinalityComparator());
		return result;
	}

	/**
	 * Get an element with the highest cardinality of all elements.
	 * 
	 * @return
	 */
	public X getElementWithHighestCardinality() {
		long c = Long.MIN_VALUE;
		X result = null;
		for (X element : cardinalities.keySet()) {
			if (cardinalities.get(element) > c) {
				c = cardinalities.get(element);
				result = element;
			}
		}
		return result;
	}

	public String toString() {
		return cardinalities.toString();
	}

	public MultiSet<X> clone() {
		MultiSet<X> result = new MultiSet<>();
		result.addAll(this);
		return result;
	}
}
