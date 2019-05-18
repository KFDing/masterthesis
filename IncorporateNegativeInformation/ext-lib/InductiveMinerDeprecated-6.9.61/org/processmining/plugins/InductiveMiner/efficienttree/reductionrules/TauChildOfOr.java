package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class TauChildOfOr implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isOr(node)) {
			if (tree.getNumberOfChildren(node) > 1) {
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {

						//before: ... or  ... tau ... ...
						//            node

						//remove tau
						EfficientTreeUtils.removeChild(tree, node, child);

						//now: ... or  ... ... ... skip
						//         node

						//increase size
						tree.setSize(tree.getMaxNumberOfNodes() + 1);

						//now: ... or  ... ... ... skip skip
						//         node

						//move everything forward
						tree.copy(node, node + 2, tree.getMaxNumberOfNodes() - node - 2);

						//now: ... skip skip or  ... ... ...
						//         node

						//insert xor tau
						tree.setNodeType(node, NodeType.xor);
						tree.setNumberOfChildren(node, 2);

						tree.setNodeType(node + 1, NodeType.tau);

						return true;

						//						//put node in xor tau
						//						int[] newTree = new int[tree.getTree().length + 2];
						//
						//						//copy the part up to the node
						//						System.arraycopy(tree.getTree(), 0, newTree, 0, node);
						//
						//						//set the xor
						//						newTree[node] = NodeType.xor.code - EfficientTreeImpl.childrenFactor * 2;
						//
						//						//set the tau
						//						newTree[node + 1] = NodeType.tau.code;
						//
						//						//copy the remaining part
						//						System.arraycopy(tree.getTree(), node, newTree, node + 2, tree.getTree().length - node);
						//
						//						tree.replaceTree(newTree);
						//
						//						return true;
					}
				}
			}
		}
		return false;
	}

}
