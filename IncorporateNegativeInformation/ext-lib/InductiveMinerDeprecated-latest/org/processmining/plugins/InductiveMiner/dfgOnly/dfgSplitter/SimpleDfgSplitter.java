package org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;

import gnu.trove.map.hash.TObjectIntHashMap;

public class SimpleDfgSplitter implements DfgSplitter {

	public DfgSplitResult split(Dfg dfg, Cut cut, DfgMinerState minerState) {
		List<Dfg> subDfgs = new ArrayList<>();

		TObjectIntHashMap<XEventClass> node2sigma = new TObjectIntHashMap<>();
		int sigmaN = 0;
		for (Set<XEventClass> sigma : cut.getPartition()) {
			for (XEventClass a : sigma) {
				node2sigma.put(a, sigmaN);
			}
			sigmaN++;
		}

		sigmaN = 0;
		for (Set<XEventClass> sigma : cut.getPartition()) {
			Dfg subDfg = new DfgImpl();
			subDfgs.add(subDfg);

			//walk through the nodes
			for (XEventClass activity : sigma) {
				subDfg.getDirectlyFollowsGraph().addVertex(activity);

				if (dfg.isStartActivity(activity)) {
					subDfg.addStartActivity(activity, dfg.getStartActivityCardinality(activity));
				}
				if (dfg.isEndActivity(activity)) {
					subDfg.addEndActivity(activity, dfg.getEndActivityCardinality(activity));
				}
			}

			//walk through the edges
			{
				//directly follows graph
				for (long edge : dfg.getDirectlyFollowsGraph().getEdges()) {
					int cardinality = (int) dfg.getDirectlyFollowsGraph().getEdgeWeight(edge);
					XEventClass source = dfg.getDirectlyFollowsGraph().getEdgeSource(edge);
					XEventClass target = dfg.getDirectlyFollowsGraph().getEdgeTarget(edge);

					if (sigma.contains(source) && sigma.contains(target)) {
						//internal edge in sigma
						subDfg.getDirectlyFollowsGraph().addEdge(source, target, cardinality);
					} else if (sigma.contains(source) && !sigma.contains(target)) {
						//edge going out of sigma
						if (cut.getOperator() == Operator.sequence || cut.getOperator() == Operator.loop) {
							//source is an end activity
							subDfg.addEndActivity(source, cardinality);
						}
					} else if (!sigma.contains(source) && sigma.contains(target)) {
						//edge going into sigma
						if (cut.getOperator() == Operator.sequence || cut.getOperator() == Operator.loop) {
							//target is a start activity
							subDfg.addStartActivity(target, cardinality);
						}
					} else {
						//edge unrelated to sigma
						if (cut.getOperator() == Operator.sequence) {
							if (node2sigma.get(source) < sigmaN && node2sigma.get(target) > sigmaN) {
								subDfg.addEmptyTraces(cardinality);
							}
						}
					}
				}
			}

			if (cut.getOperator() == Operator.sequence) {
				//add empty traces for start activities in sigmas after this one
				for (int sigmaJ = sigmaN + 1; sigmaJ < cut.getPartition().size(); sigmaJ++) {
					for (XEventClass activity : cut.getPartition().get(sigmaJ)) {
						subDfg.addEmptyTraces(dfg.getStartActivityCardinality(activity));
					}
				}

				//add empty traces for end activities in sigmas before this one
				for (int sigmaJ = 0; sigmaJ < sigmaN; sigmaJ++) {
					for (XEventClass activity : cut.getPartition().get(sigmaJ)) {
						subDfg.addEmptyTraces(dfg.getEndActivityCardinality(activity));
					}
				}
			}

			sigmaN++;
		}

		return new DfgSplitResult(subDfgs);
	}
}
