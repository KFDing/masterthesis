package org.processmining.plugins.InductiveMiner.mining.cuts.IMc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities.Probabilities;

public class CutFinderIMinInfo {
	private final Dfg dfg;
	private final Graph<XEventClass> graph;
	private final Graph<XEventClass> transitiveGraph;
	private final Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	private final Probabilities probabilities;
	private final JobList jobList;
	private final boolean debug;

	/**
	 * A CutFinderIMinInfo keeps track of a single call to the IMin cut finder.
	 * 
	 * @param dfg
	 * @param graph2
	 * @param transitiveGraph2
	 * @param minimumSelfDistancesBetween
	 * @param probabilities
	 * @param jobList
	 * @param debug
	 */
	public CutFinderIMinInfo(Dfg dfg, Graph<XEventClass> graph2, Graph<XEventClass> transitiveGraph2,
			Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween, Probabilities probabilities,
			JobList jobList, boolean debug) {
		this.dfg = dfg;
		this.graph = graph2;
		this.transitiveGraph = transitiveGraph2;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.probabilities = probabilities;
		this.jobList = jobList;
		this.debug = debug;
	}

	public Dfg getDfg() {
		return dfg;
	}

	public Graph<XEventClass> getGraph() {
		return graph;
	}

	public Graph<XEventClass> getTransitiveGraph() {
		return transitiveGraph;
	}

	public Probabilities getProbabilities() {
		return probabilities;
	}

	public boolean isDebug() {
		return debug;
	}

	public Set<XEventClass> getActivities() {
		return new HashSet<>(Arrays.asList(graph.getVertices()));
	}

	public MultiSet<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (minimumSelfDistancesBetween == null) {
			throw new RuntimeException("Minimum self distances are not available.");
		}
		if (!minimumSelfDistancesBetween.containsKey(activity)) {
			return new MultiSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public JobList getJobList() {
		return jobList;
	}
}
