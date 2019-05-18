package org.processmining.plugins.InductiveMiner;

public class Quadruple<A,B,C,D> {
		private final A a;
		private final B b;
		private final C c;
		private final D d;
		
		public Quadruple(A a, B b, C c, D d) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}
		
		public static <A,B,C,D> Quadruple<A,B,C,D> of(A a, B b, C c, D d){
	        return new Quadruple<A,B,C,D>(a,b,c,d);
	    }
		
		public A getA() { return a; }
		public B getB() { return b; }
		public C getC() { return c; }
		public D getD() { return d; }
		
		@Override
		public int hashCode() { return a.hashCode() ^ b.hashCode() ^ c.hashCode() ^ d.hashCode(); }
		
		@SuppressWarnings("rawtypes")
		@Override
		  public boolean equals(Object o) {
		    if (o == null) return false;
		    if (!(o instanceof Quadruple)) return false;
		    Quadruple pairo = (Quadruple) o;
		    return this.a.equals(pairo.getA()) &&
		           this.b.equals(pairo.getB()) &&
		           this.c.equals(pairo.getC()) &&
		           this.d.equals(pairo.getD());
		  }
	}

