package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

/**
 * This reduction rule makes the tree longer. Termination is guaranteed as it is
 * reducing the number of tau's that are directly under a loop.
 * 
 * @author sleemans
 *
 */
public class LoopTau implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isLoop(node)) {
			int body = tree.getChild(node, 0);
			if (tree.isTau(body)) {

				//check whether the redo can produce something else than tau
				int redo = tree.getChild(node, 1);
				if (!EfficientTreeMetrics.canOnlyProduceTau(tree, redo)) {

					//before: loop tau redo exit ...

					//after: xor tau0 loop redo tau1 exit ...

					int e = tree.getChild(node, 2);

					//make a new tree as we're making it longer
					tree.setSize(tree.getMaxNumberOfNodes() + 2);

					//now: loop tau redo exit ... skip skip
					//					 e

					//move the exit and everything after it back
					tree.copy(e, e + 2, tree.getMaxNumberOfNodes() - e - 2);

					//now: loop tau redo skip skip exit ...
					//					 e

					//set tau1
					tree.setNodeType(e + 1, NodeType.tau);

					//now: loop tau redo skip tau1 exit ...
					//				r	 e

					//move redo forward
					int r = tree.getChild(node, 1);
					tree.copy(r, r + 1, e - r);

					//now: loop tau skip redo tau1 exit ...
					//				r	 e

					//set the xor, tau and loop
					tree.setNodeType(node, NodeType.xor);
					tree.setNumberOfChildren(node, 2);

					tree.setNodeType(r, NodeType.loop);
					tree.setNumberOfChildren(r, 3);
					
					//after: xor tau0 loop redo tau1 exit ...
					
					return true;

//					//set the xor
//					newTree[node] = NodeType.xor.code - EfficientTreeImpl.childrenFactor * 2;
//
//					//set tau0
//					newTree[node + 1] = NodeType.tau.code;
//
//					//set the node
//					newTree[node + 2] = NodeType.loop.code - EfficientTreeImpl.childrenFactor * 3;
//
//					//copy the redo part (which becomes the body)
//					System.arraycopy(tree.getTree(), redo, newTree, node + 3, e - (body + 1));
//
//					//set tau1
//					newTree[(e - 1) + 2] = NodeType.tau.code;
//
//					//copy the remaining part of the tree
//					System.arraycopy(tree.getTree(), e, newTree, e + 2, newTree.length - (e + 2));
//
//					tree.replaceTree(newTree);
				}
			}
		}
		return false;
	}

}