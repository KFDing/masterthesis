package org.processmining.plugins.InductiveMiner;

public class Quintuple<A,B,C,D,E> {
	private final A a;
	private final B b;
	private final C c;
	private final D d;
	private final E e;
	
	public Quintuple(A a, B b, C c, D d, E e) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
	}
	
	public static <A,B,C,D,E> Quintuple<A,B,C,D,E> of(A a, B b, C c, D d, E e){
        return new Quintuple<A,B,C,D,E>(a,b,c,d,e);
    }
	
	public A getA() { return a; }
	public B getB() { return b; }
	public C getC() { return c; }
	public D getD() { return d; }
	public E getE() { return e; }
	
	@Override
	public int hashCode() { return a.hashCode() ^ b.hashCode() ^ c.hashCode() ^ d.hashCode() ^ e.hashCode(); }
	
	@SuppressWarnings("rawtypes")
	@Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Quintuple)) return false;
	    Quintuple pairo = (Quintuple) o;
	    return this.a.equals(pairo.getA()) &&
	           this.b.equals(pairo.getB()) &&
	           this.c.equals(pairo.getC()) &&
	           this.d.equals(pairo.getD()) &&
	           this.e.equals(pairo.getE());
	  }
}
