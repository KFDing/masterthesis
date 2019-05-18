package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;

import gnu.trove.map.TObjectIntMap;

public class InlineTree {

	/**
	 * 
	 * @param label
	 * @return a leaf with the given label
	 */
	public static EfficientTree leaf(String label) {
		//construct the tree
		int[] tree = new int[1];
		tree[0] = 0;

		//construct the activity -> int map
		TObjectIntMap<String> activity2int = EfficientTreeImpl.getEmptyActivity2int();
		activity2int.put(label, 0);

		//construct the int -> activity map
		String[] int2activity = new String[] { label };

		return EfficientTreeFactory.create(tree, activity2int, int2activity);
	}

	public static EfficientTree tau() {
		//construct the tree
		int[] tree = new int[1];
		tree[0] = NodeType.tau.code;

		//construct the activity -> int map
		TObjectIntMap<String> activity2int = EfficientTreeImpl.getEmptyActivity2int();

		//construct the int -> activity map
		String[] int2activity = new String[0];

		return EfficientTreeFactory.create(tree, activity2int, int2activity);
	}

	/**
	 * Construct a new tree by putting the given children in xor. The children
	 * will be copied.
	 * 
	 * @param child1
	 * @param children
	 */
	public static EfficientTree xor(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.xor, child1, children);
	}

	/**
	 * Construct a new tree by putting the given children in xor. The children
	 * will be copied.
	 * 
	 * @param children
	 */
	public static EfficientTree xor(Collection<EfficientTree> children) {
		return combineTrees(NodeType.xor, children);
	}

	/**
	 * Construct a new tree by putting the given children in sequence. The
	 * children will be copied.
	 * 
	 * @param child1
	 * @param children
	 */
	public static EfficientTree sequence(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.sequence, child1, children);
	}

	public static EfficientTree seq(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.sequence, child1, children);
	}

	/**
	 * Construct a new tree by putting the given children in sequence. The
	 * children will be copied.
	 * 
	 * @param children
	 */
	public static EfficientTree sequence(List<EfficientTree> children) {
		return combineTrees(NodeType.sequence, children);
	}

	/**
	 * Construct a new tree by putting the given children concurrent. The
	 * children will be copied.
	 * 
	 * @param child1
	 * @param children
	 */
	public static EfficientTree concurrent(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.concurrent, child1, children);
	}

	public static EfficientTree and(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.concurrent, child1, children);
	}

	/**
	 * Construct a new tree by putting the given children concurrent. The
	 * children will be copied.
	 * 
	 * @param children
	 */
	public static EfficientTree concurrent(Collection<EfficientTree> children) {
		return combineTrees(NodeType.concurrent, children);
	}

	/**
	 * Construct a new tree by putting the given children in loop. The children
	 * will be copied.
	 * 
	 * @param body
	 * @param redo
	 * @param exit
	 * @return
	 */
	public static EfficientTree loop(EfficientTree body, EfficientTree redo, EfficientTree exit) {
		EfficientTree[] children = new EfficientTree[2];
		children[0] = redo;
		children[1] = exit;
		return combineTrees(NodeType.loop, body, children);
	}

	/**
	 * Construct a new tree by putting the given children in loop. The children
	 * will be copied. Three children must be given.
	 * 
	 * @param children
	 */
	public static EfficientTree loop(List<EfficientTree> children) {
		assert (children.size() == 3);
		return combineTrees(NodeType.loop, children);
	}

	/**
	 * Construct a new tree by putting the given children interleaved. The
	 * children will be copied.
	 * 
	 * @param child1
	 * @param children
	 */
	public static EfficientTree interleaved(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.interleaved, child1, children);
	}

	public static EfficientTree inte(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.interleaved, child1, children);
	}

	/**
	 * Construct a new tree by putting the given children interleaved. The
	 * children will be copied.
	 * 
	 * @param children
	 */
	public static EfficientTree interleaved(List<EfficientTree> children) {
		return combineTrees(NodeType.interleaved, children);
	}

	/**
	 * Construct a new tree by putting the given children in an inclusive or.
	 * The children will be copied.
	 * 
	 * @param child1
	 * @param children
	 */
	public static EfficientTree or(EfficientTree child1, EfficientTree... children) {
		return combineTrees(NodeType.or, child1, children);
	}

	/**
	 * Construct a new tree by putting the given children in an inclusive or.
	 * The children will be copied.
	 * 
	 * @param children
	 */
	public static EfficientTree or(List<EfficientTree> children) {
		return combineTrees(NodeType.or, children);
	}

	private static EfficientTree combineTrees(NodeType operator, Collection<EfficientTree> children) {
		assert (children.size() >= 1);
		Iterator<EfficientTree> it = children.iterator();
		EfficientTree first = it.next();
		EfficientTree[] childrenArray = new EfficientTree[children.size() - 1];
		for (int i = 0; i < children.size() - 1; i++) {
			childrenArray[i] = it.next();
		}
		return combineTrees(operator, first, childrenArray);
	}

	private static EfficientTree combineTrees(NodeType operator, EfficientTree child1, EfficientTree... children) {
		if ((child1 instanceof EfficientTreeImpl)) {
			for (EfficientTree child : children) {
				if (!(child instanceof EfficientTreeImpl)) {
					return combineTreesSlow(operator, child1, children);
				}
			}

			return combineTreesFast(operator, (EfficientTreeImpl) child1, children);
		}

		return combineTreesSlow(operator, child1, children);
	}

	private static EfficientTree combineTreesSlow(NodeType operator, EfficientTree child1, EfficientTree... children) {
		int size = child1.traverse(0);
		for (EfficientTree child : children) {
			size += child.traverse(0);
		}
		int[] newTree = new int[size + 1];

		//set the root operator
		newTree[0] = operator.code - EfficientTreeImpl.childrenFactor * (children.length + 1);

		//set-up the activity data structures
		TObjectIntMap<String> newActivity2int = EfficientTreeImpl.getEmptyActivity2int();
		String[] newInt2activity;

		//copy the first child
		int nextChildIndex = 1;
		{
			int size1 = child1.traverse(0);
			for (int node = 0; node < size1; node++) {
				newTree[nextChildIndex] = child1.getNodeType(node).code
						- EfficientTreeImpl.childrenFactor * child1.getNumberOfChildren(node);
				nextChildIndex++;
			}
			newActivity2int.putAll(child1.getActivity2int());
			newInt2activity = child1.getInt2activity().clone();
		}

		//copy the other children
		for (EfficientTree child : children) {
			int sizeChild = child.traverse(0);

			//copy the tree structure
			int size1 = child.traverse(0);
			for (int node = 0; node < size1; node++) {
				newTree[nextChildIndex + node] = child.getNodeType(node).code
						- EfficientTreeImpl.childrenFactor * child.getNumberOfChildren(node);
			}

			//make an activity mapping
			int[] activity2new = new int[child.getInt2activity().length];
			for (int i = 0; i < child.getInt2activity().length; i++) {
				String activity = child.getInt2activity()[i];
				if (newActivity2int.containsKey(activity)) {
					activity2new[i] = newActivity2int.get(activity);
				} else {
					activity2new[i] = newInt2activity.length;
					newActivity2int.put(activity, newInt2activity.length);
					newInt2activity = Arrays.copyOf(newInt2activity, newInt2activity.length + 1);
					newInt2activity[newInt2activity.length - 1] = activity;
				}
			}

			//walk over the copied array and replace activity integer codes
			for (int i = nextChildIndex; i < nextChildIndex + sizeChild; i++) {
				if (newTree[i] >= 0) {
					newTree[i] = activity2new[newTree[i]];
				}
			}

			nextChildIndex += sizeChild;
		}

		return EfficientTreeFactory.create(newTree, newActivity2int, newInt2activity);
	}

	/**
	 * Combine trees if all are of a known type.
	 * 
	 * @param operator
	 * @param child1
	 * @param children
	 * @return
	 */
	private static EfficientTree combineTreesFast(NodeType operator, EfficientTreeImpl child1,
			EfficientTree... children) {
		//initialise the tree array
		int[] newTree;
		{
			int size = child1.traverse(0);
			for (EfficientTree child : children) {
				size += child.traverse(0);
			}
			newTree = new int[size + 1];
		}

		/*
		 * Before: [child1] [child2] [child..] After: operator child1 child2
		 * child..
		 */

		//set the root operator
		newTree[0] = operator.code - EfficientTreeImpl.childrenFactor * (children.length + 1);

		//set-up the activity data structures
		TObjectIntMap<String> newActivity2int = EfficientTreeImpl.getEmptyActivity2int();
		String[] newInt2activity;

		//copy the first child
		int nextChildIndex = 1;
		{
			int size1 = child1.traverse(0);
			System.arraycopy(child1.getTree(), 0, newTree, nextChildIndex, size1);
			nextChildIndex += size1;
			newActivity2int.putAll(child1.getActivity2int());
			newInt2activity = child1.getInt2activity().clone();
		}

		//copy the other children
		for (EfficientTree child2 : children) {
			EfficientTreeImpl child = (EfficientTreeImpl) child2;
			int size = child.traverse(0);

			//copy the tree structure
			System.arraycopy(child.getTree(), 0, newTree, nextChildIndex, size);

			//make an activity mapping
			int[] activity2new = new int[child.getInt2activity().length];
			for (int i = 0; i < child.getInt2activity().length; i++) {
				String activity = child.getInt2activity()[i];
				if (newActivity2int.containsKey(activity)) {
					activity2new[i] = newActivity2int.get(activity);
				} else {
					activity2new[i] = newInt2activity.length;
					newActivity2int.put(activity, newInt2activity.length);
					newInt2activity = Arrays.copyOf(newInt2activity, newInt2activity.length + 1);
					newInt2activity[newInt2activity.length - 1] = activity;
				}
			}

			//walk over the copied array and replace activity integer codes
			for (int i = nextChildIndex; i < nextChildIndex + size; i++) {
				if (newTree[i] >= 0) {
					newTree[i] = activity2new[newTree[i]];
				}
			}

			nextChildIndex += size;
		}

		return EfficientTreeFactory.create(newTree, newActivity2int, newInt2activity);
	}

}
