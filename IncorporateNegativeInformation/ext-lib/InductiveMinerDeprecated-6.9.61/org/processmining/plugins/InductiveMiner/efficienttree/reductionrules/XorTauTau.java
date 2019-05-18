package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class XorTauTau implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isXor(node)) {
			//count the number of taus
			boolean tauSeen = false;

			//search for children that can produce epsilon
			for (int child : tree.getChildren(node)) {
				if (!tauSeen && !tree.isTau(child) && EfficientTreeMetrics.canProduceTau(tree, child)) {
					tauSeen = true;
					break;
				}
			}

			for (int child : tree.getChildren(node)) {
				if (tree.isTau(child)) {
					if (tauSeen) {
						//this is the second tau; remove it
						EfficientTreeUtils.removeChild(tree, node, child);
						return true;
					}
					tauSeen = true;
				}
			}
		}
		return false;
	}

}
