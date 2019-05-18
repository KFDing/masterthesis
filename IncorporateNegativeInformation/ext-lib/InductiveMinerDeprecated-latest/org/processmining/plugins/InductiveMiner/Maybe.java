package org.processmining.plugins.InductiveMiner;

public class Maybe<X> {
	protected X x;

	public Maybe(X x) {
		this.x = x;
	}
	
	public static <X> Maybe<X> of(X x){
        return new Maybe<X>(x);
    }

	public X get() {
		return x;
	}
	
	@Override
	public String toString() {
		if (x == null) {
			return "null";
		} else {
			return x.toString();
		}
	}

	@Override
	public int hashCode() {
		if (x != null) {
			return x.hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Maybe<?>) {
			Maybe<?> o2 = (Maybe<?>) o;
			if (get() == null && o2.get() == null) {
				return true;
			} else if (get() == null || o2.get() == null) {
				return false;
			}
			return this.get().equals(o2.get());
		}
		return false;
	}
}
