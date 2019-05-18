package org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class DfgFallThroughStrictTauLoop implements DfgFallThrough {

	public Node fallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		if (dfg.getActivities().length > 1 && dfg.hasEndActivities() && dfg.hasStartActivities()) {

			TIntSet startActivities = new TIntHashSet(10, 0.5f, -1);
			TIntSet endActivities = new TIntHashSet(10, 0.5f, -1);
			startActivities.addAll(dfg.getStartActivityIndices());
			endActivities.addAll(dfg.getEndActivityIndices());

			Dfg result = new DfgImpl();

			//add nodes, start and end activities
			for (XEventClass activity : dfg.getActivities()) {
				result.addActivity(activity);

				if (dfg.isStartActivity(activity)) {
					result.addStartActivity(activity, dfg.getStartActivityCardinality(activity));
				}
				if (dfg.isEndActivity(activity)) {
					result.addEndActivity(activity, dfg.getEndActivityCardinality(activity));
				}
			}

			//find out whether the dfg has connections end -> start
			boolean removedEdge = false;
			for (long edge : dfg.getDirectlyFollowsEdges()) {
				XEventClass source = dfg.getDirectlyFollowsEdgeSource(edge);
				XEventClass target = dfg.getDirectlyFollowsEdgeTarget(edge);
				long cardinality = dfg.getDirectlyFollowsEdgeCardinality(edge);

				if (endActivities.contains(dfg.getDirectlyFollowsEdgeSourceIndex(edge))
						&& startActivities.contains(dfg.getDirectlyFollowsEdgeTargetIndex(edge))) {
					//this edge goes from an end activity to a start activity
					//add it as end activities
					result.addEndActivity(source, cardinality);
					result.addStartActivity(target, cardinality);
					removedEdge = true;
				} else {
					//this edge is fine, just copy
					result.addDirectlyFollowsEdge(source, target, cardinality);
				}
			}

			if (!removedEdge) {
				return null;
			}

			//construct the tau loop construct
			DfgMiner.debug(" fall through: tau loop strict", minerState);
			//making a tau loop split makes sense
			Block loop = new XorLoop("");
			Miner.addNode(tree, loop);

			{
				Node body = DfgMiner.mineNode(result, tree, minerState);
				Miner.addChild(loop, body, minerState);
			}

			{
				Node redo = new Automatic("tau");
				Miner.addNode(tree, redo);
				Miner.addChild(loop, redo, minerState);
			}

			{
				Node exit = new Automatic("tau");
				Miner.addNode(tree, exit);
				Miner.addChild(loop, exit, minerState);
			}

			return loop;
		}
		return null;
	}

}
