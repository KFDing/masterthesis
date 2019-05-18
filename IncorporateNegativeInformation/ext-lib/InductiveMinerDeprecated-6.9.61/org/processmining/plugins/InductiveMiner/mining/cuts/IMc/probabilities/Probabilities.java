package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities;

import java.math.BigInteger;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;

import com.google.common.collect.ImmutableSet;

public abstract class Probabilities {

	public abstract double getProbabilityXor(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract double getProbabilitySequence(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract double getProbabilityParallel(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract double getProbabilityLoopSingle(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract double getProbabilityLoopDouble(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract double getProbabilityLoopIndirect(CutFinderIMinInfo info, XEventClass a, XEventClass b);

	public abstract String toString();

	public final int doubleToIntFactor = 100000;

	public BigInteger toBigInt(double probability) {
		return BigInteger.valueOf(Math.round(doubleToIntFactor * probability));
	}

	public BigInteger getProbabilityXorB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityXor(info, a, b));
	}

	public BigInteger getProbabilitySequenceB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilitySequence(info, a, b));
	}

	public BigInteger getProbabilityParallelB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityParallel(info, a, b));
	}

	public BigInteger getProbabilityLoopSingleB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopSingle(info, a, b));
	}

	public BigInteger getProbabilityLoopDoubleB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopDouble(info, a, b));
	}

	public BigInteger getProbabilityLoopIndirectB(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopIndirect(info, a, b));
	}

	protected long getActivityCount(XEventClass a, CutFinderIMinInfo info) {
		//count how often each activity occurs
		Graph<XEventClass> graph = info.getGraph();
		double sum = 0;
		for (long edge : graph.getOutgoingEdgesOf(a)) {
			sum += graph.getEdgeWeight(edge);
		}
		sum += info.getDfg().getEndActivityCardinality(a);

		return Math.round(sum);
	}

	//Directly follows
	protected boolean D(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		Graph<XEventClass> graph = info.getGraph();
		return graph.containsEdge(a, b);
	}

	//Eventually follows
	protected boolean E(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		Graph<XEventClass> graph = info.getTransitiveGraph();
		return graph.containsEdge(a, b);
	}

	protected double z(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return (getActivityCount(a, info) + getActivityCount(b, info)) / 2.0;
	}

	protected double w(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return info.getMinimumSelfDistanceBetween(a).getCardinalityOf(b)
				+ info.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}

	protected double x(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		return info.getGraph().getEdgeWeight(a, b);
	}

	protected boolean noSEinvolvedInMsd(CutFinderIMinInfo info, XEventClass a, XEventClass b) {
		Set<XEventClass> SE = Sets.union(ImmutableSet.copyOf(info.getDfg().getStartActivities()),
				ImmutableSet.copyOf(info.getDfg().getEndActivities()));
		if (w(info, a, b) > 0 && !SE.contains(a) && !SE.contains(b)) {
			Set<XEventClass> SEmMSD = Sets.intersection(SE, info.getMinimumSelfDistanceBetween(a).toSet());
			if (SEmMSD.size() == 0) {
				return true;
			}
		}
		return false;
	}
}
