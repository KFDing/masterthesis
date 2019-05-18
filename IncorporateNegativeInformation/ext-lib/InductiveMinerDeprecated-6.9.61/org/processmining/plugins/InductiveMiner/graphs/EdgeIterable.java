package org.processmining.plugins.InductiveMiner.graphs;

import java.util.Iterator;

public abstract class EdgeIterable implements Iterable<Long> {

	public Iterator<Long> iterator() {
		return new Iterator<Long>() {

			public boolean hasNext() {
				return EdgeIterable.this.hasNext();
			}

			public Long next() {
				return EdgeIterable.this.next();
			}

			public void remove() {
				EdgeIterable.this.remove();
			}
		};
	}
	
	protected abstract void remove();
	protected abstract boolean hasNext();
	protected abstract long next();
}
