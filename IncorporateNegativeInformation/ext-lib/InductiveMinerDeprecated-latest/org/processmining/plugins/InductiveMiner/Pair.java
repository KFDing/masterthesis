package org.processmining.plugins.InductiveMiner;


public class Pair<L, R> {

	private final L left;
	private final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public static <L,R> Pair<L,R> of(L left, R right){
        return new Pair<L,R>(left, right);
    }

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public String toString() {
		return "[" + left + ", " + right + "]";
	}

	@Override
	public int hashCode() {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return right.hashCode();
		} else if (right == null) {
			return left.hashCode();
		}
		return left.hashCode() ^ right.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Pair))
			return false;
		Pair<?, ?> pairo = (Pair<?, ?>) o;
		if (left == null) {
			return pairo.getLeft() == null && right.equals(pairo.getRight());
		} else if (right == null) {
			return pairo.getRight() == null && left.equals(pairo.getLeft());
		}
		return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
	}

	public L getA() {
		return getLeft();
	}
	
	public R getB() {
		return getRight();
	}

}