package org.processmining.plugins.InductiveMiner.graphs;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;

public class Reachability {

	private final Dfg dfg;

	private final LongBitSet reachable;

	public Reachability(Dfg dfg) {
		this.dfg = dfg;
		reachable = new LongBitSet();

		//initialise
		for (long edge : dfg.getDirectlyFollowsEdges()) {
			int source = dfg.getDirectlyFollowsEdgeSourceIndex(edge);
			int target = dfg.getDirectlyFollowsEdgeTargetIndex(edge);

			reachable.set(getIndex(source, target), true);
		}
		
		for (int i = 0; i < dfg.getNumberOfActivities(); i++) {
			reachable.set(getIndex(i, i), true);
		}

		for (int i = 0; i < dfg.getNumberOfActivities(); i++) {
			for (int j = 0; j < dfg.getNumberOfActivities(); j++) {
				if (reachable.get(getIndex(j, i))) {
					for (int k = 0; k < dfg.getNumberOfActivities(); k++) {
						if (reachable.get(getIndex(j, i)) && reachable.get(getIndex(i, k))) {
							reachable.set(getIndex(j, k), true);
						}
					}
				}
			}
		}
	}

	private long getIndex(int from, int to) {
		return (((long) from) << 32) | (to & 0xFFFFFFFFL);
	}

	public boolean isReachable(int from, int to) {
		return reachable.get(getIndex(from, to));
	}

	public boolean isReachable(XEventClass from, XEventClass to) {
		return isReachable(dfg.getIndexOfActivity(from), dfg.getIndexOfActivity(to));
	}
}