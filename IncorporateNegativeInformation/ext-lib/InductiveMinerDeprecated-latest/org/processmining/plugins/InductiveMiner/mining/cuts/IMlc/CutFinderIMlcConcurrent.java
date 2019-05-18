package org.processmining.plugins.InductiveMiner.mining.cuts.IMlc;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents2;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMlcConcurrent implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getDfg(), logInfo.getDfg().getConcurrencyGraph());
	}

	public static Cut findCut(Dfg dfg, Graph<XEventClass> concurrencyGraph) {

		//noise filtering can have removed all start and end activities.
		//if that is the case, return
		if (!dfg.hasStartActivities() || !dfg.hasEndActivities()) {
			return null;
		}

		Graph<XEventClass> negatedGraph = GraphFactory
				.create(XEventClass.class, concurrencyGraph.getNumberOfVertices());
		negatedGraph.addVertices(concurrencyGraph.getVertices());

		for (XEventClass a1 : concurrencyGraph.getVertices()) {
			for (XEventClass a2 : concurrencyGraph.getVertices()) {
				if (a1 != a2) {
					if (!concurrencyGraph.containsEdge(a1, a2) && !concurrencyGraph.containsEdge(a2, a1)) {
						negatedGraph.addEdge(a1, a2, 1);
					}
				}
			}
		}

		Collection<Set<XEventClass>> connectedComponents = ConnectedComponents2.compute(negatedGraph);

		List<Set<XEventClass>> connectedComponents2 = CutFinderIMConcurrent
				.ensureStartEndInEach(dfg, connectedComponents);

		if (connectedComponents2 == null) {
			return null;
		} else {
			return new Cut(Operator.concurrent, connectedComponents2);
		}
	}

}
