package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class OrXorTau implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isOr(node)) {

			//one child is an xor
			for (int xorChild : tree.getChildren(node)) {
				if (tree.isXor(xorChild)) {

					//one child is a tau
					for (int tauChild : tree.getChildren(xorChild)) {
						if (tree.isTau(tauChild)) {

							//before: ... or  ... xor ... tau ...
							//            node

							//remove the tau
							EfficientTreeUtils.removeChild(tree, xorChild, tauChild);

							//now: ... or  ... xor ... ... skip
							//         node

							//increase the size of the tree
							tree.setSize(tree.getMaxNumberOfNodes() + 1);

							//now: ... or  ... xor ... ... skip skip
							//         node

							//move everything forward by two places
							tree.copy(node, node + 2, tree.getMaxNumberOfNodes() - node - 2);

							//now: ... skip skip or  ... xor ... ...
							//         node

							//set xor and tau

							tree.setNodeType(node, NodeType.xor);
							tree.setNumberOfChildren(node, 2);

							tree.setNodeType(node + 1, NodeType.tau);

							//now: ... xor tau or  ... xor ... ...
							//         node

							return true;

							//							
							//							//set the xor
							//							newTree[node] = NodeType.xor.code - EfficientTreeImpl.childrenFactor * 2;
							//							
							//							//set the tau
							//							newTree[node + 1] = NodeType.tau.code;
							//							
							//							//copy the remaining part
							//							System.arraycopy(tree.getTree(), node, newTree, node + 2, tree.getTree().length - node);
							//							
							//							tree.replaceTree(newTree);
						}
					}

				}
			}
		}
		return false;
	}

}
