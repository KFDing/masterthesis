package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class LoopLoop implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int loop) {
		if (tree.isLoop(loop)) {
			int oldBody = tree.getChild(loop, 0);
			if (tree.isLoop(oldBody)) {
				int A = tree.getChild(oldBody, 0);
				int B = tree.getChild(oldBody, 1);
				int tau = tree.getChild(oldBody, 2);

				if (tree.isTau(tau)) {

					//before:
					//loop loop A B tau C D

					//after:
					//loop A xor2 B C D

					//remove the exit tau
					EfficientTreeUtils.removeChild(tree, oldBody, tau);
					
					//now:
					//loop loop A B C D

					//move A one position forward (over the nested loop); leave B and further in place
					tree.copy(A, A - 1, B - A);
					
					//now:
					//loop A . B C D

					//set the XOR (notice that B has not moved)
					tree.setNodeType(B - 1, NodeType.xor);
					tree.setNumberOfChildren(B - 1, 2);
					
					//now:
					//loop A xor2 B C D

					return true;
				}
			}
		}
		return false;
	}

}
