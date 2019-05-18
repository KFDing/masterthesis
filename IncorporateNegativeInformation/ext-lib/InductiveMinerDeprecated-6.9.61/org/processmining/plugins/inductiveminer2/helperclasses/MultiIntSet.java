package org.processmining.plugins.inductiveminer2.helperclasses;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import gnu.trove.TCollections;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.procedure.TIntLongProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;

public class MultiIntSet implements Iterable<Integer> {

	protected TIntLongHashMap cardinalities;
	protected long size;

	public MultiIntSet() {
		cardinalities = new TIntLongHashMap(10, 0.5f, Integer.MIN_VALUE, 0);
		size = 0;
	}

	public void clear() {
		size = 0;
		cardinalities.clear();
	}

	public boolean add(int element) {
		return add(element, 1);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cardinalities == null) ? 0 : cardinalities.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiIntSet other = (MultiIntSet) obj;
		if (cardinalities == null) {
			if (other.cardinalities != null)
				return false;
		} else if (!cardinalities.equals(other.cardinalities))
			return false;
		return true;
	}

	public boolean add(int element, long cardinality) {
		assert (cardinality >= 0);

		if (!cardinalities.containsKey(element)) {
			cardinalities.put(element, cardinality);
		} else {
			long newCardinality = cardinalities.get(element) + cardinality;
			cardinalities.put(element, newCardinality);
		}
		size += cardinality;
		return true;
	}

	public boolean remove(int element, long cardinality) {
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

	public boolean remove(int element) {
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
	public boolean addAll(Collection<Integer> collection) {
		for (int element : collection) {
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
	public boolean addAll(Collection<Integer> collection, long cardinality) {
		for (int element : collection) {
			add(element, cardinality);
		}
		return true;
	}

	public boolean addAll(TIntCollection collection) {
		collection.forEach(new TIntProcedure() {
			public boolean execute(int element) {
				add(element, 1);
				return true;
			}
		});
		return true;
	}

	public boolean addAll(MultiIntSet collection) {
		for (int element : collection) {
			add(element, collection.getCardinalityOf(element));
		}
		return true;
	}

	public void empty() {
		cardinalities = new TIntLongHashMap(10, 0.5f, Integer.MIN_VALUE, 0);
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

	public TIntSet toSet() {
		return TCollections.unmodifiableSet(cardinalities.keySet());
	}

	public boolean contains(int a) {
		return cardinalities.containsKey(a) && cardinalities.get(a) > 0;
	}

	public long getCardinalityOf(int e) {
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
	public Iterator<Integer> iterator() {

		Iterator<Integer> it = new Iterator<Integer>() {
			private TIntLongIterator it3 = cardinalities.iterator();

			@Override
			public boolean hasNext() {
				return it3.hasNext();
			}

			@Override
			public Integer next() {
				it3.advance();
				return it3.key();
			}

			public void remove() {
				it3.remove();
			}
		};
		return it;
	}

	public MultiIntSet copy() {
		final MultiIntSet result = new MultiIntSet();
		cardinalities.forEachEntry(new TIntLongProcedure() {
			public boolean execute(int a, long b) {
				result.add(a, b);
				return true;
			}
		});
		return result;
	}

	/**
	 * Get an element with the highest cardinality of all elements.
	 * 
	 * @return
	 */
	public int getElementWithHighestCardinality() {
		final AtomicLong c = new AtomicLong(Long.MIN_VALUE);
		final AtomicInteger result = new AtomicInteger(Integer.MIN_VALUE);
		cardinalities.forEachEntry(new TIntLongProcedure() {
			public boolean execute(int key, long value) {
				if (value > c.get()) {
					c.set(value);
					result.set(key);
				}
				return true;
			}
		});
		return result.get();
	}

	public String toString() {
		return cardinalities.toString();
	}

	public MultiIntSet clone() {
		MultiIntSet result = new MultiIntSet();
		result.addAll(this);
		return result;
	}

	/**
	 * Returns a list of the elements, sorted by their cardinality.
	 * 
	 * @return
	 */
	public int[] sortByCardinality() {
		final int[] elements = cardinalities.keys();
		List<Integer> wrapper = new AbstractList<Integer>() {

			@Override
			public Integer get(int index) {
				return elements[index];
			}

			@Override
			public int size() {
				return elements.length;
			}

			@Override
			public Integer set(int index, Integer element) {
				int v = elements[index];
				elements[index] = element;
				return v;
			}
		};

		Collections.sort(wrapper, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return Long.valueOf(getCardinalityOf(o1)).compareTo(Long.valueOf(getCardinalityOf(o2)));
			}
		});
		return elements;
	}
}
