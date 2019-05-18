package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.Iterator;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;

public class EfficientTreeUtils {

	/**
	 * 
	 * @return whether the tree is consistent.
	 */
	public static boolean isConsistent(EfficientTree tree) {
		int treeLength = tree.traverse(0);

		if (treeLength != tree.getMaxNumberOfNodes() && tree.getNodeType(treeLength) != NodeType.skip) {
			return false;
		}

		if (tree.getNodeType(treeLength - 1) == NodeType.skip) {
			return false;
		}

		for (int node = 0; node < treeLength; node++) {
			if (tree.isLoop(node) && tree.getNumberOfChildren(node) != 3) {
				System.out.println("tree inconsistent at " + node);
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the parent of node. Do not call if node is the root. Notice that
	 * this is an expensive operation; avoid if possible.
	 * 
	 * @param node
	 * @return The parent of node.
	 */
	public static int getParent(EfficientTree tree, int node) {
		assert (node != tree.getRoot());

		int potentialParent = node - 1;
		while (tree.traverse(potentialParent) <= node) {
			potentialParent--;
		}
		return potentialParent;
	}

	/**
	 * Remove a child of a node. Only call when the child has no children
	 * itself. Note that activity names may be left behind outside of the tree.
	 * 
	 * @param parent
	 * @param child
	 */
	public static void removeChild(EfficientTree tree, int parent, int child) {
		assert (tree.isActivity(child) || tree.isTau(child) || tree.getNumberOfChildren(child) == 0);

		//move everything beyond the child
		tree.copy(child + 1, child, tree.getMaxNumberOfNodes() - child - 1);
		tree.setNodeType(tree.getMaxNumberOfNodes() - 1, NodeType.skip);

		//update the children counter of the parent
		tree.setNumberOfChildren(parent, tree.getNumberOfChildren(parent) - 1);
	}

	/**
	 * Replace a node and all of its children by a single tau.
	 * 
	 * @param tree
	 * @param node
	 */
	public static void replaceNodeWithTau(EfficientTree tree, int node) {
		if (tree.isTau(node)) {
			return;
		}
		int nextNode = tree.traverse(node);
		int length = nextNode - node;
		if (nextNode != tree.getMaxNumberOfNodes()) {
			tree.copy(nextNode - 1, node, tree.getMaxNumberOfNodes() - (nextNode - 1));
		}

		for (int i = tree.getMaxNumberOfNodes() - (length - 1); i < tree.getMaxNumberOfNodes(); i++) {
			tree.setNodeType(i, NodeType.skip);
		}

		tree.setNodeType(node, NodeType.tau);
	}

	/**
	 * 
	 * @param parent
	 * @param child
	 * @return Whether the child is a direct or indirect child of parent.
	 */
	public static boolean isParentOf(EfficientTree tree, int parent, int child) {
		if (parent > child) {
			return false;
		}
		return tree.traverse(parent) > child;
	}

	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The child of parent that contains grandChild. If grandChild is
	 *         not a child of parent, will return -1.
	 */
	public static int getChildWith(EfficientTree tree, int parent, int grandChild) {
		for (int child : tree.getChildren(parent)) {
			if (isParentOf(tree, child, grandChild)) {
				return child;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param parent
	 * @param grandChild
	 * @return The number of the child within parent that contains grandChild.
	 *         If grandChild is not a child of parent, will return -1.
	 */
	public static int getChildNumberWith(EfficientTree tree, int parent, int grandChild) {
		int childNumber = 0;
		for (int child : tree.getChildren(parent)) {
			if (isParentOf(tree, child, grandChild)) {
				return childNumber;
			}
			childNumber++;
		}
		return -1;
	}

	/**
	 * 
	 * @param tree
	 * @param nodeA
	 * @param nodeB
	 * @return The node that is a parent of both nodeA and nodeB, or is nodeA or
	 *         nodeB itself.
	 */
	public static int getLowestCommonParent(EfficientTree tree, int nodeA, int nodeB) {
		if (nodeA > nodeB) {
			return getLowestCommonParent(tree, nodeB, nodeA);
		}
		if (nodeA == nodeB) {
			return nodeA;
		}

		int lowestCommonParent = nodeA;
		while (!isParentOf(tree, lowestCommonParent, nodeB)) {
			lowestCommonParent = EfficientTreeUtils.getParent(tree, lowestCommonParent);
		}
		return lowestCommonParent;
	}

	public static Iterable<Integer> getAllNodes(EfficientTree tree) {
		return getAllNodes(tree, tree.getRoot());
	}

	public static Iterable<Integer> getAllNodes(final EfficientTree tree, final int child) {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int now = child - 1;

					public int findNext() {
						int next = now + 1;
						while (next < tree.getMaxNumberOfNodes() && tree.isSkip(next)) {
							next++;
						}
						if (next == tree.getMaxNumberOfNodes()) {
							return -1;
						}
						return next;
					}

					public Integer next() {
						now = findNext();
						return now;
					}

					public boolean hasNext() {
						return findNext() != -1;
					}

					public void remove() {

					}
				};
			}
		};
	}
}
