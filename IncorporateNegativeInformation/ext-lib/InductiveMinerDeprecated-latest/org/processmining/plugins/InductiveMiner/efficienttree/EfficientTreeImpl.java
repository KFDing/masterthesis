package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.Arrays;
import java.util.Iterator;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class EfficientTreeImpl implements EfficientTree {

	public static final int childrenFactor = 10;

	private int[] tree;
	private TObjectIntMap<String> activity2int;
	private String[] int2activity;

	/**
	 * Construct a new efficient tree using the given inputs. These inputs will
	 * not be copied and should not be altered outside the EfficientTree context
	 * after creating the tree.
	 * 
	 * @param tree
	 * @param activity2int
	 *            The mapping from activities (strings) to integers. The map
	 *            should be created such that the emptiness value is not 0 (as
	 *            that is a valid activity). Preferably, use
	 *            getEmptyActivity2int() to obtain such a map.
	 * @param int2activity
	 *            The mapping from integers to the activities (strings). Should
	 *            be consistent with activity2int.
	 */
	public EfficientTreeImpl(int[] tree, TObjectIntMap<String> activity2int, String[] int2activity) {
		this.tree = tree;
		this.activity2int = activity2int;
		this.int2activity = int2activity;
	}

	/**
	 * 
	 * @return the internal representation of the process tree. Do not edit the
	 *         returned object.
	 */
	public int[] getTree() {
		return tree;
	}

	@Override
	public TObjectIntMap<String> getActivity2int() {
		return activity2int;
	}

	@Override
	public String[] getInt2activity() {
		return int2activity;
	}

	/**
	 * Add a child to the tree, as a child of parent, at the given position. The
	 * caller has to ensure there's enough space in the array of the tree.
	 * 
	 * @param parent
	 * @param asChildNr
	 */
	public void addChild(int parent, int asChildNr, int operatorOrActivity) {
		assert (tree[parent] < 0);

		//find the tree index where the new child is to go
		int insertAt = parent + 1;
		for (int j = 0; j < asChildNr; j++) {
			insertAt = traverse(insertAt);
		}

		//make space in the array
		copy(insertAt, insertAt + 1, tree.length - insertAt - 1);
		//System.arraycopy(tree, insertAt, tree, insertAt + 1, tree.length - insertAt - 1);

		//insert the new node
		tree[insertAt] = operatorOrActivity;

		//increase the children of the parent
		tree[parent] -= childrenFactor;
		if (tree[parent] > 0) {
			//int underflow happened
			throw new RuntimeException("child cannot be added");
		}
	}

	@Override
	public int traverse(int node) {
		if (tree[node] >= 0) {
			return node + 1;
		} else if (tree[node] == NodeType.tau.code) {
			return node + 1;
		} else {
			int numberOfChildren = -tree[node] / EfficientTreeImpl.childrenFactor;
			node++;
			for (int j = 0; j < numberOfChildren; j++) {
				node = traverse(node);
			}
			return node;
		}
	}

	@Override
	public int getActivity(int node) {
		return tree[node];
	}

	@Override
	public String getActivityName(int node) {
		return int2activity[tree[node]];
	}

	@Override
	public boolean isOperator(int node) {
		return tree[node] < 0 && tree[node] != NodeType.skip.code && tree[node] != NodeType.tau.code;
	}

	@Override
	public int getNumberOfChildren(int node) {
		if (!isOperator(node)) {
			return 0;
		}
		return tree[node] / -childrenFactor;
	}

	@Override
	public int getChild(int parent, int numberOfChild) {
		int i = parent + 1;
		for (int j = 0; j < numberOfChild; j++) {
			i = traverse(i);
		}
		return i;
	}

	@Override
	public boolean isTau(int node) {
		return tree[node] == NodeType.tau.code;
	}

	@Override
	public boolean isActivity(int node) {
		return tree[node] >= 0;
	}

	@Override
	public boolean isSequence(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.sequence.code;
	}

	@Override
	public boolean isXor(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.xor.code;
	}

	@Override
	public boolean isConcurrent(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.concurrent.code;
	}

	@Override
	public boolean isInterleaved(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.interleaved.code;
	}

	@Override
	public boolean isLoop(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.loop.code;
	}

	@Override
	public boolean isOr(int node) {
		return tree[node] < 0 && tree[node] % childrenFactor == NodeType.or.code;
	}

	@Override
	public int getRoot() {
		return 0;
	}

	@Override
	public Iterable<Integer> getChildren(final int node) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = -1;
					int next = node + 1;
					int count = 0;

					public void remove() {
						throw new RuntimeException("not implemented");
					}

					public Integer next() {
						count++;
						now = next;
						next = traverse(now);
						return now;
					}

					public boolean hasNext() {
						return count < getNumberOfChildren(node);
					}
				};
			}
		};
	}

	/**
	 * Copy the tree into a tight array
	 * 
	 */
	public EfficientTreeImpl shortenTree() {
		int length = traverse(0);
		int[] newTree = new int[length];
		System.arraycopy(tree, 0, newTree, 0, length);
		return new EfficientTreeImpl(tree, activity2int, int2activity);
	}

	@Override
	public boolean isSkip(int node) {
		return tree[node] == NodeType.skip.code;
	}

	@Deprecated
	public int[] getChildTree(int node) {
		int next = traverse(node);
		int[] result = new int[next - node];
		System.arraycopy(tree, node, result, 0, next - node);
		return result;
	}

	public static TObjectIntMap<String> getEmptyActivity2int() {
		return new TObjectIntHashMap<String>(8, 0.5f, -1);
	}

	/**
	 * Replace the tree structure.
	 * 
	 * @param tree
	 */
	public void replaceTree(int[] tree) {
		this.tree = tree;
	}

	/**
	 * Return a string representation of this tree. This string representation
	 * is not unique for equivalent trees, e.g. the trees xor(a, b) and xor(b,
	 * a) are equivalent, but do not have the same string representation.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		try {
			toString(0, result);
		} catch (UnknownTreeNodeException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public void toString(int node, StringBuilder result) throws UnknownTreeNodeException {
		if (isActivity(node)) {
			result.append(getActivityName(node));
		} else if (isTau(node)) {
			result.append("tau");
		} else if (isOperator(node)) {
			if (isXor(node)) {
				result.append("xor(");
			} else if (isSequence(node)) {
				result.append("seq(");
			} else if (isConcurrent(node)) {
				result.append("and(");
			} else if (isInterleaved(node)) {
				result.append("int(");
			} else if (isLoop(node)) {
				result.append("loop(");
			} else if (isOr(node)) {
				result.append("or(");
			} else {
				throw new UnknownTreeNodeException();
			}
			for (int i = 0; i < getNumberOfChildren(node); i++) {
				int child = getChild(node, i);
				toString(child, result);
				if (i < getNumberOfChildren(node) - 1) {
					result.append(",");
				}
			}
			result.append(")");
		} else {
			throw new UnknownTreeNodeException();
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity2int == null) ? 0 : activity2int.hashCode());
		result = prime * result + Arrays.hashCode(int2activity);
		result = prime * result + Arrays.hashCode(tree);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EfficientTreeImpl other = (EfficientTreeImpl) obj;
		if (activity2int == null) {
			if (other.activity2int != null)
				return false;
		} else if (!activity2int.equals(other.activity2int))
			return false;
		if (!Arrays.equals(int2activity, other.int2activity))
			return false;
		if (!Arrays.equals(tree, other.tree))
			return false;
		return true;
	}

	@Override
	public EfficientTree clone() {
		//return new EfficientTreeImpl(tree.clone(), new TObjectIntHashMap<String>(activity2int), int2activity.clone());

		EfficientTreeImpl result = null;
		try {
			result = (EfficientTreeImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		result.tree = tree.clone();
		result.activity2int = new TObjectIntHashMap<String>(activity2int);
		result.int2activity = int2activity.clone();
		return result;
	}

	@Override
	public NodeType getNodeType(int node) {
		if (isSkip(node)) {
			return NodeType.skip;
		} else if (isTau(node)) {
			return NodeType.tau;
		} else if (isActivity(node)) {
			return NodeType.activity;
		} else if (isConcurrent(node)) {
			return NodeType.concurrent;
		} else if (isSequence(node)) {
			return NodeType.sequence;
		} else if (isXor(node)) {
			return NodeType.xor;
		} else if (isLoop(node)) {
			return NodeType.loop;
		} else if (isOr(node)) {
			return NodeType.or;
		} else if (isInterleaved(node)) {
			return NodeType.interleaved;
		} else {
			throw new RuntimeException("not implemented");
		}
	}

	@Override
	public int getMaxNumberOfNodes() {
		return tree.length;
	}

	@Override
	public void copy(int srcPos, int destPos, int length) {
		System.arraycopy(tree, srcPos, tree, destPos, length);
	}

	@Override
	public void setNodeType(int node, NodeType operator) {
		if (operator == NodeType.skip || operator == NodeType.tau || operator == NodeType.activity) {
			tree[node] = operator.code;
		} else {
			tree[node] = operator.code - (childrenFactor * getNumberOfChildren(node));
		}
	}

	@Override
	public void setNodeActivity(int node, int activity) {
		tree[node] = activity;
	}

	@Override
	public void setNumberOfChildren(int node, int numberOfChildren) {
		tree[node] = tree[node] % childrenFactor - (childrenFactor * numberOfChildren);
	}

	@Override
	public void setSize(int size) {
		int oldSize = tree.length;
		int[] newTree = new int[size];
		System.arraycopy(tree, 0, newTree, 0, Math.min(oldSize, size));
		for (int i = oldSize; i < size; i++) {
			newTree[i] = NodeType.skip.code;
		}
		tree = newTree;
	}

	@Override
	public void swap(int startA, int startB, int lengthB) {
		int[] temp = new int[startB - startA];
		System.arraycopy(tree, startA, temp, 0, startB - startA);
		System.arraycopy(tree, startB, tree, startA, lengthB);
		System.arraycopy(temp, 0, tree, startA + lengthB, startB - startA);
	}

	@Override
	public void reorderNodes(Integer[] nodes, int end) {
		//create a temp array to hold the blocks
		int min = tree.length;
		for (int i = 0; i < nodes.length; i++) {
			min = Math.min(min, nodes[i]);
		}
		int length = end - min;
		int[] temp = new int[length];

		//copy the nodes to the temp array
		int currentlyAtInTemp = 0;
		for (int node : nodes) {
			/*
			 * find where this node stops (we could do it with traverse, but
			 * this is probably more efficient and become robust against "nodes"
			 * that span multiple nodes
			 * 
			 */
			int nodeEnd = end;
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] > node && nodes[i] < nodeEnd) {
					nodeEnd = nodes[i];
				}
			}

			System.arraycopy(tree, node, temp, currentlyAtInTemp, nodeEnd - node);

			currentlyAtInTemp += nodeEnd - node;
		}

		System.arraycopy(temp, 0, tree, min, temp.length);
	}
}
