package org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class SimpleDfgBaseCaseFinder implements DfgBaseCaseFinder {

	private static DfgFallThroughFlower flower = new DfgFallThroughFlower();

	public Node findBaseCases(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		if (dfg.getNumberOfEmptyTraces() != 0) {
			//empty traces

			Block newNode = new AbstractBlock.Xor("");
			DfgMiner.addNode(tree, newNode);

			//add tau
			Node tau = new AbstractTask.Automatic("tau");
			DfgMiner.addNode(tree, tau);
			Miner.addChild(newNode, tau, minerState);

			//filter empty traces
			Dfg subDfg = dfg.clone();
			subDfg.setNumberOfEmptyTraces(0);

			//recurse
			Node child = DfgMiner.mineNode(subDfg, tree, minerState);
			Miner.addChild(newNode, child, minerState);

			return newNode;

		} else if (dfg.getNumberOfActivities() == 0) {
			//no activities (should not happen)
			Node node = new AbstractTask.Automatic("tau empty log");
			DfgMiner.addNode(tree, node);
			return node;

		} else if (dfg.getNumberOfActivities() == 1) {
			//single activity

			if (!dfg.getDirectlyFollowsEdges().iterator().hasNext()) {
				//no self-edges present: single activity
				XEventClass activity = dfg.getActivities()[0];
				Node node = new AbstractTask.Manual(activity.toString());
				DfgMiner.addNode(tree, node);
				return node;
			} else {
				//edges present, must be a self-edge

				//let fail to flower loop
				return flower.fallThrough(dfg, tree, minerState);
			}

		}

		return null;
	}

}
