package org.processmining.plugins.InductiveMiner.mining.cuts.IMc;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicResult {
	private AtomicReference<SATResult> value;

	// Constructors
	public AtomicResult() {
		this(new SATResult(null, null, 0, null));
	}
	
	public AtomicResult(double minimum) {
		this(new SATResult(null, null, minimum, null));
	}

	public AtomicResult(SATResult result) {
		value = new AtomicReference<SATResult>(result);
	}

	// Atomic methods
	public SATResult get() {
		return value.get();
	}

	public void set(SATResult r) {
		value.set(new SATResult(r));
	}

	public void lazySet(SATResult newVal) {
		set(newVal);
	}

	public boolean compareAndSet(SATResult expect, SATResult update) {
		SATResult origVal, newVal;

		newVal = new SATResult(update);
		while (true) {
			origVal = get();

			if (Double.compare(origVal.getProbability(), expect.getProbability()) == 0) {
				if (value.compareAndSet(origVal, newVal))
					return true;
			} else {
				return false;
			}
		}
	}

	public boolean weakCompareAndSet(SATResult expect, SATResult update) {
		return compareAndSet(expect, update);
	}
	
	public boolean maximumAndGet(SATResult tryVal) {
		while (true) {
			SATResult origVal = get();
			if (origVal.getProbability() > tryVal.getProbability()) {
				return false;
			}
			if (compareAndSet(origVal, tryVal)) {
				return true;
			}
		}
	}
}
