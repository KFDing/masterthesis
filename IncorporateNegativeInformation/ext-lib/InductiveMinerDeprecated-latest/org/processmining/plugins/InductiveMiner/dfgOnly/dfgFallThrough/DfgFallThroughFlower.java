package org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

public class DfgFallThroughFlower implements DfgFallThrough {

	public Node fallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		DfgMiner.debug(" fall through: flower model", minerState);

		Block loopNode = new AbstractBlock.XorLoop("");
		DfgMiner.addNode(tree, loopNode);

		//body: tau
		Node body = new AbstractTask.Automatic("tau");
		DfgMiner.addNode(tree, body);
		Miner.addChild(loopNode, body, minerState);

		//redo: xor/activity
		Block xorNode;
		if (dfg.getDirectlyFollowsGraph().getNumberOfVertices() == 1) {
			xorNode = loopNode;
		} else {
			xorNode = new AbstractBlock.Xor("");
			DfgMiner.addNode(tree, xorNode);
			Miner.addChild(loopNode, xorNode, minerState);
		}

		for (XEventClass activity : dfg.getDirectlyFollowsGraph().getVertices()) {
			Node child = new AbstractTask.Manual(activity.toString());
			DfgMiner.addNode(tree, child);
			Miner.addChild(xorNode, child, minerState);
		}

		Node tau2 = new AbstractTask.Automatic("tau");
		DfgMiner.addNode(tree, tau2);
		Miner.addChild(loopNode, tau2, minerState);

		return loopNode;
	}

}
