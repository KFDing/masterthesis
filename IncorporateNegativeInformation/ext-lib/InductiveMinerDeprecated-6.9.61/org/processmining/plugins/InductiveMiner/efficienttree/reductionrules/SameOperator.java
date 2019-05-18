package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class SameOperator implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isXor(node) || tree.isSequence(node) || tree.isConcurrent(node) || tree.isOr(node)) {
			NodeType operator = tree.getNodeType(node);

			for (int child : tree.getChildren(node)) {
				if (tree.isOperator(child) && tree.getNodeType(child) == operator) {
					//before: op( op2( A, B ), ...)
					//after:  op( A, B, ...)

					int numberOfChildren2 = tree.getNumberOfChildren(child);

					//remove op2
					tree.setNodeType(child, NodeType.tau);
					EfficientTreeUtils.removeChild(tree, node, child);

					//correct the number of children of node
					tree.setNumberOfChildren(node, tree.getNumberOfChildren(node) + numberOfChildren2);
					return true;
				}
			}
		}

		return false;
	}

}
