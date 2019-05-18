package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIM implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().setSize() == 1 && logInfo.getDfg().getNumberOfEmptyTraces() == 0
				&& logInfo.getNumberOfActivityInstances() == logInfo.getNumberOfTraces()) {
			//single activity

			Miner.debug(" base case: IM single activity", minerState);

			XEventClass activity = logInfo.getActivities().iterator().next();
			Node node = new AbstractTask.Manual(activity.toString());
			Miner.addNode(tree, node);

			return node;
		} else if (logInfo.getActivities().setSize() == 1 && logInfo.getDfg().getNumberOfEmptyTraces() == 0) {
			//single activity in semi-flower model

			Miner.debug(" base case: IM single activity semi-flower model", minerState);

			XEventClass activity = logInfo.getActivities().iterator().next();
			Block loopNode = new AbstractBlock.XorLoop("");
			Miner.addNode(tree, loopNode);

			//body: activity
			Node body = new AbstractTask.Manual(activity.toString());
			Miner.addNode(tree, body);
			loopNode.addChild(body);

			//redo: tau
			Node redo = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, redo);
			loopNode.addChild(redo);

			//exit: tau
			Node exit = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, exit);
			loopNode.addChild(exit);

			return loopNode;
		} else if (logInfo.getActivities().setSize() == 0) {
			//empty log

			Miner.debug(" base case: IM empty log", minerState);

			Node node = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, node);

			return node;
		} else if (logInfo.getDfg().getNumberOfEmptyTraces() != 0) {
			Miner.debug(" base case: IM xor(tau, ..)", minerState);

			Block newNode = new AbstractBlock.Xor("");
			Miner.addNode(tree, newNode);

			//add tau
			Node tau = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, tau);
			newNode.addChild(tau);

			//filter empty traces
			IMLog sublog = BaseCaseFinderIMiEmptyTrace.removeEpsilonTraces(log, minerState);

			//recurse
			Node child = Miner.mineNode(sublog, tree, minerState);
			newNode.addChild(child);

			return newNode;
		}

		return null;
	}

}
