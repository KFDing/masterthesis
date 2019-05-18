package org.processmining.plugins.InductiveMiner.efficienttree.reductionrules;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeMetrics;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReductionRule;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

@Deprecated
public class LoopTauATau2flower implements EfficientTreeReductionRule {

	public boolean apply(EfficientTree tree, int loop) throws UnknownTreeNodeException {

		//look for loop( tau1, A, tau3)
		if (tree.isLoop(loop)) {
			int tau2 = tree.getChild(loop, 0);
			int redo = tree.getChild(loop, 1);
			int tau4 = tree.getChild(loop, 2);

			if (tree.isTau(tau2) && tree.isTau(tau4)) {

				/*
				 * perform behaviour analysis: if this subprocess can produce
				 * the single activity (for every occurring activity), we might
				 * as well replace it with a flower model
				 */

				//loop through all nodes for activities
				int countActivities = 0;
				for (int i = 0; i < tau4; i++) {
					if (tree.isActivity(i)) {
						countActivities++;
						int activity = tree.getActivity(i);

						//check whether the redo can produce this activity as a single activity
						if (!EfficientTreeMetrics.canProduceSingleActivity(tree, redo, activity)) {
							return false;
						}
					}
				}

				//make sure we are terminating: seq(a) does not require rewriting
				if ((tau4 - tau2) <= countActivities + 3) {
					return false;
				}

				//make sure we are terminating: xor(a, b, c....) does not require rewriting
				if (tree.isXor(redo)) {
					int length = tree.traverse(redo) - redo;
					if (length == countActivities + 1) {
						return false;
					}
				}

				//gather the activities
				int[] activities = new int[countActivities];
				int j = 0;
				for (int i = 0; i < tau4; i++) {
					if (tree.isActivity(i)) {
						activities[j] = tree.getActivity(i);
						j++;
					}
				}

				//before: loop(tau2, (....), tau3)

				//remove every node that will be useless in the future
				for (int child = tau4 - 1; child > tau2 + countActivities + 1; child--) {
					tree.setNodeType(child, NodeType.tau);
					EfficientTreeUtils.removeChild(tree, redo, child);
				}

				//set the xor
				tree.setNodeType(redo, NodeType.xor);
				tree.setNumberOfChildren(redo, countActivities);

				//set the activities
				for (int i = 0; i < countActivities; i++) {
					tree.setNodeActivity(redo + i + 1, activities[i]);
				}

				//after: loop(tau2, xor( ... ), tau4)

				return true;
			}
		}

		return false;
	}
}
