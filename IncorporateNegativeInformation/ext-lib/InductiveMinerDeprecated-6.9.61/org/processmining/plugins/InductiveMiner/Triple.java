package org.processmining.plugins.InductiveMiner;

public class Triple<A, B, C> {
	private final A a;
	private final B b;
	private final C c;

	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public static <A,B,C> Triple<A,B,C> of(A a, B b, C c){
        return new Triple<A,B,C>(a,b,c);
    }

	public A getA() {
		return a;
	}

	public B getB() {
		return b;
	}

	public C getC() {
		return c;
	}

	@Override
	public int hashCode() {
		return a.hashCode() ^ b.hashCode() ^ c.hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Triple))
			return false;
		Triple pairo = (Triple) o;
		return this.a.equals(pairo.getA()) && this.b.equals(pairo.getB()) && this.c.equals(pairo.getC());
	}

	@Override
	public String toString() {
		return "[" + a.toString() + ", " + b.toString() + ", " + c.toString() + "]";
	}
}
