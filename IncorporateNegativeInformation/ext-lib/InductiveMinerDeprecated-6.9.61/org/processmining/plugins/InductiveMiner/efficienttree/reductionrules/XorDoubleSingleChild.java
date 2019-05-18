package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class XorDoubleSingleChild implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isXor(node)) {
			boolean[] activitiesSeen = new boolean[tree.getInt2activity().length];

			for (int child : tree.getChildren(node)) {
				if (tree.isActivity(child)) {
					int activity = tree.getActivity(child);
					if (!activitiesSeen[activity]) {
						activitiesSeen[activity] = true;
					} else {
						//remove this activity
						EfficientTreeUtils.removeChild(tree, node, child);
						return true;
					}
				}
			}
		}

		return false;
	}

}
