package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiEmptyLog implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		if (logInfo.getNumberOfActivityInstances() == 0) {
			//empty log, return tau

			Miner.debug(" base case: IMi empty log", minerState);

			Node node = new AbstractTask.Automatic("tau");
			Miner.addNode(tree, node);

			return node;
		}
		return null;
	}

}
