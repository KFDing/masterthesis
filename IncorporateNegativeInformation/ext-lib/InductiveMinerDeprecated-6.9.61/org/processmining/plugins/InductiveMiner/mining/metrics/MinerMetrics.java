package org.processmining.plugins.InductiveMiner.mining.metrics;

import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

public class MinerMetrics {

	public static long getShortestTrace(Node node) {
		if (node instanceof Manual) {
			return 1;
		} else if (node instanceof Automatic) {
			return 0;
		} else if (node instanceof Block) {
			Block block = (Block) node;
			if (block instanceof Xor || block instanceof Def || block instanceof Or) {
				long result = Long.MAX_VALUE;
				for (Node child: block.getChildren()) {
					result = Math.min(result, getShortestTrace(child));
				}
				return result;
			} else if (block instanceof And || block instanceof Seq) {
				int result = 0;
				for (Node child: block.getChildren()) {
					result += getShortestTrace(child);
				}
				return result;
			} else if (block instanceof AbstractBlock.DefLoop || block instanceof AbstractBlock.XorLoop) {
				return getShortestTrace(block.getChildren().get(0)) + getShortestTrace(block.getChildren().get(2));
			}
		}
		assert(false);
		return 0;
	}
}
