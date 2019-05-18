package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;

import gnu.trove.set.hash.THashSet;

/**
 * Try to exclude a single activity, given that it occurs precisely once per
 * trace.
 * 
 * @author sleemans
 *
 */
public class FallThroughActivityOncePerTraceConcurrent implements FallThrough {

	private final boolean strict;

	/**
	 * 
	 * @param strict
	 *            Denotes whether this case is applied strictly, i.e. true =
	 *            only apply if each trace contains precisely one activity;
	 *            false = apply it also if it's close enough.
	 */
	public FallThroughActivityOncePerTraceConcurrent(boolean strict) {
		this.strict = strict;
	}

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		if (logInfo.getActivities().toSet().size() > 1) {

			Collection<XEventClass> activities = logInfo.getActivities().sortByCardinality();
			for (XEventClass activity : activities) {

				/*
				 * An arbitrary parallel cut is always possible. However, to
				 * save precision we only want to split here if this activity
				 * occurs precisely once in each trace.
				 */

				long cardinality = logInfo.getActivities().getCardinalityOf(activity);
				long epsilon = logInfo.getDfg().getNumberOfEmptyTraces();
				boolean x = epsilon == 0 && cardinality == logInfo.getNumberOfTraces();

				double noise = minerState.parameters.getNoiseThreshold();
				double avg = cardinality / logInfo.getNumberOfTraces();
				double reverseNoise = noise == 1 ? Double.MAX_VALUE : 1 / (1 - noise);
				boolean y = epsilon < logInfo.getNumberOfTraces() * noise && avg > 1 - noise && avg < reverseNoise;

				if (x || (!strict && y)) {

					Miner.debug(" fall through: leave out one-per-trace activity", minerState);

					//create cut
					Set<XEventClass> sigma0 = new THashSet<>();
					sigma0.add(activity);
					Set<XEventClass> sigma1 = new THashSet<>(activities);
					sigma1.remove(activity);
					List<Set<XEventClass>> partition = new ArrayList<Set<XEventClass>>();
					partition.add(sigma0);
					partition.add(sigma1);
					Cut cut = new Cut(Operator.concurrent, partition);

					//split log
					LogSplitResult logSplitResult = minerState.parameters.getLogSplitter().split(log, logInfo, cut,
							minerState);
					if (minerState.isCancelled()) {
						return null;
					}
					IMLog log1 = logSplitResult.sublogs.get(0);
					IMLog log2 = logSplitResult.sublogs.get(1);

					//construct node
					Block newNode = new AbstractBlock.And("");
					Miner.addNode(tree, newNode);

					//recurse
					Node child1 = Miner.mineNode(log1, tree, minerState);
					Miner.addChild(newNode, child1, minerState);

					Node child2 = Miner.mineNode(log2, tree, minerState);
					Miner.addChild(newNode, child2, minerState);

					return newNode;
				}
			}
		}
		return null;
	}
}
