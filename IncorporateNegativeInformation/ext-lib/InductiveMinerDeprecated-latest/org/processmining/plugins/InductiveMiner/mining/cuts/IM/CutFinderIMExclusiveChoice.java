package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents2;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMExclusiveChoice implements CutFinder, DfgCutFinder {

	public Cut findCut(final IMLog log, final IMLogInfo logInfo, final MinerState minerState) {
		return findCut(logInfo.getDfg().getDirectlyFollowsGraph());
	}

	public Cut findCut(final Dfg dfg, final DfgMinerState minerState) {
		return findCut(dfg.getDirectlyFollowsGraph());
	}

	public static Cut findCut(final Graph<XEventClass> graph) {
		//compute the connected components of the directly follows graph
		Collection<Set<XEventClass>> connectedComponents = ConnectedComponents2.compute(graph);

		return new Cut(Operator.xor, connectedComponents);
	}

}
