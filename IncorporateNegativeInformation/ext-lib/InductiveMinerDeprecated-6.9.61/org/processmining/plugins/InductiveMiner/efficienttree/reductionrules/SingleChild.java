package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;

public class SingleChild implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isOperator(node) && tree.getNumberOfChildren(node) == 1) {
			//remove this node
			tree.copy(node + 1, node, tree.getMaxNumberOfNodes() - node - 1);
			tree.setNodeType(tree.getMaxNumberOfNodes() - 1, NodeType.skip);

			return true;
		}
		return false;
	}
}
