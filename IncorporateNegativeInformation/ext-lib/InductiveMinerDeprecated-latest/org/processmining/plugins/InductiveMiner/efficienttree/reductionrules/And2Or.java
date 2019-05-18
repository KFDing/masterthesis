package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class And2Or implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isConcurrent(node)) {
			TIntList childrenThatCanProduceTau = new TIntArrayList();
			for (int child : tree.getChildren(node)) {
				if (EfficientTreeMetrics.canProduceTau(tree, child)) {
					childrenThatCanProduceTau.add(child);
				}
			}
			if (childrenThatCanProduceTau.size() < 2) {
				return false;
			}

			//there are more than two children that can produce tau

			//before: and ... Q2 ... Q4 ... skip skip skip skip skip
			//        node

			//find the number of skips at the end
			int numberOfSkips = 0;
			for (int i = tree.getMaxNumberOfNodes() - 1; i >= 0; i--) {
				if (!tree.isSkip(i)) {
					break;
				}
				numberOfSkips++;
			}

			//increase the size of the tree
			tree.setSize((tree.getMaxNumberOfNodes() + 3) - numberOfSkips);

			//now: and ... Q2 ... Q4 ... skip skip skip
			//     node

			//bring all the Q's forward
			{
				int at = node + 1;
				for (int child : childrenThatCanProduceTau.toArray()) {
					int endB = tree.traverse(child);
					if (at != child) {
						tree.swap(at, child, endB - child);
					}
					at = at + (endB - child);
				}
			}

			//now: and Q2 Q4 ... ... ... skip skip skip
			//     node

			//move all children backward and insert xor tau or
			{
				tree.copy(node + 1, node + 4, tree.getMaxNumberOfNodes() - node - 4);

				//set number of children of the and
				tree.setNumberOfChildren(node,
						(tree.getNumberOfChildren(node) - (childrenThatCanProduceTau.size() - 1)));

				tree.setNodeType(node + 1, NodeType.xor);
				tree.setNumberOfChildren(node + 1, 2);

				tree.setNodeType(node + 2, NodeType.tau);

				tree.setNodeType(node + 3, NodeType.or);
				tree.setNumberOfChildren(node + 3, childrenThatCanProduceTau.size());
			}

			//now: and xor tau or Q2 Q4 ... ... ...
			//     node

			return true;

			//			//copy the part up to the node
			//			System.arraycopy(tree.getTree(), 0, newTree, 0, node);
			//
			//			//set the number of children of and
			//			newTree[node] = NodeType.concurrent.code - EfficientTreeImpl.childrenFactor
			//					* (tree.getNumberOfChildren(node) - (childrenThatCanProduceTau.size() - 1));
			//
			//			int pos = node + 1;
			//			for (int child : tree.getChildren(node)) {
			//				int childLength = tree.traverse(child) - child;
			//				if (!childrenThatCanProduceTau.contains(child)) {
			//					//this child should stay in place, however it should be shifted to the left (pos)
			//					System.arraycopy(tree.getTree(), child, newTree, pos, childLength);
			//					pos += childLength;
			//				}
			//			}
			//
			//			//set the xor
			//			newTree[pos] = NodeType.xor.code - EfficientTreeImpl.childrenFactor * 2;
			//
			//			//set the tau
			//			newTree[pos + 1] = NodeType.tau.code;
			//
			//			//set the or
			//			newTree[pos + 2] = NodeType.or.code - EfficientTreeImpl.childrenFactor * childrenThatCanProduceTau.size();
			//
			//			pos += 3;
			//
			//			for (int child : childrenThatCanProduceTau.toArray()) {
			//				int childLength = tree.traverse(child) - child;
			//				System.arraycopy(tree.getTree(), child, newTree, pos, childLength);
			//				pos += childLength;
			//			}
			//
			//			//copy the part after the node
			//			int afterNode = tree.traverse(node);
			//			System.arraycopy(tree.getTree(), afterNode, newTree, pos, newTree.length - pos);
			//
			//			tree.replaceTree(newTree);

		}
		return false;
	}
}
