package org.processmining.plugins.InductiveMiner;

public class Septuple<A,B,C,D,E,F,G> {
	private final A a;
	private final B b;
	private final C c;
	private final D d;
	private final E e;
	private final F f;
	private final G g;
	
	public Septuple(A a, B b, C c, D d, E e, F f, G g) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
	}
	
	public static <A,B,C,D,E,F,G> Septuple<A,B,C,D,E,F,G> of(A a, B b, C c, D d, E e, F f, G g){
        return new Septuple<A,B,C,D,E,F,G>(a,b,c,d,e,f,g);
    }
	
	public A getA() { return a; }
	public B getB() { return b; }
	public C getC() { return c; }
	public D getD() { return d; }
	public E getE() { return e; }
	public F getF() { return f; }
	public G getG() { return g; }
	
	@Override
	public int hashCode() { return a.hashCode() ^ b.hashCode() ^ c.hashCode() ^ d.hashCode() ^ e.hashCode(); }
	
	@SuppressWarnings("rawtypes")
	@Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Septuple)) return false;
	    Septuple pairo = (Septuple) o;
	    return this.a.equals(pairo.getA()) &&
	           this.b.equals(pairo.getB()) &&
	           this.c.equals(pairo.getC()) &&
	           this.d.equals(pairo.getD()) &&
	           this.e.equals(pairo.getE()) &&
	           this.f.equals(pairo.getF()) &&
	           this.g.equals(pairo.getG());
	  }
}
