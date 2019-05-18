package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;

public class XorTauTauLoop2flowerRevert implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) {
		if (tree.isXor(node) && tree.getNumberOfChildren(node) == 2) {

			//search for a tau
			int tau = -1;
			for (int child : tree.getChildren(node)) {
				if (tree.isTau(child)) {
					tau = child;
					break;
				}
			}

			if (tau >= 0) {
				//search for a tau-loop
				int tauLoop = -1;
				int tauLoopRedo = -1;
				for (int child : tree.getChildren(node)) {
					if (tree.isLoop(child)) {
						int redo = tree.getChild(child, 1);
						int exit = tree.getChild(child, 2);

						if (tree.isTau(redo) && tree.isTau(exit)) {
							tauLoop = child;
							tauLoopRedo = redo;
						}
					}
				}

				if (tauLoop >= 0) {

					if (tau < tauLoop) {
						//before: xor tau loop A tau tau

						//move A
						tree.copy(tauLoop + 1, tauLoop + 2, tauLoopRedo - (tauLoop + 1));

						//set body tau
						tree.setNodeType(tauLoop + 1, NodeType.tau);

						//remove xor tau
						EfficientTreeUtils.removeChild(tree, node, tau);

						//after: xor loop tau A tau
						return true;
					} else {
						//before: xor loop A tau tau tau

						//remove xor tau
						EfficientTreeUtils.removeChild(tree, node, tau);

						//move A
						tree.copy(tauLoop + 1, tauLoop + 2, tauLoopRedo - (tauLoop + 1));

						//set body tau
						tree.setNodeType(tauLoop + 1, NodeType.tau);

						//after: xor loop tau A tau tau 
						return true;
					}
				}
			}
		}
		return false;
	}

}
