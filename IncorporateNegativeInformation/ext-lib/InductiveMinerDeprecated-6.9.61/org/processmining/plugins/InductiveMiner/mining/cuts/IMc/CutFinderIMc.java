package org.processmining.plugins.InductiveMiner.mining.cuts.IMc;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.jobList.JobListConcurrent;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities.Probabilities;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.SATSolveLoop;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.SATSolveParallel;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.SATSolveSequence;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.SATSolveXor;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMc implements CutFinder, DfgCutFinder {

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		float threshold = minerState.getParameters().getIncompleteThreshold();
		JobList jobList = new JobListConcurrent(minerState.getSatPool());

		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();
		Graph<XEventClass> transitiveGraph = TransitiveClosure.transitiveClosure(XEventClass.class, graph);
		Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = null;
		Probabilities satProbabilities = minerState.getParameters().getSatProbabilities();
		boolean debug = minerState.getParameters().isDebug();
		CutFinderIMinInfo info = new CutFinderIMinInfo(dfg, graph, transitiveGraph,
				minimumSelfDistancesBetween, satProbabilities, jobList, debug);
		return findCut(info, threshold, minerState);
	}

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		float threshold = minerState.parameters.getIncompleteThreshold();
		JobList jobList = new JobListConcurrent(minerState.getSatPool());

		Graph<XEventClass> graph = logInfo.getDfg().getDirectlyFollowsGraph();
		Graph<XEventClass> transitiveGraph = TransitiveClosure.transitiveClosure(XEventClass.class, graph);
		Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween = logInfo.getMinimumSelfDistancesBetween();
		Probabilities satProbabilities = minerState.parameters.getSatProbabilities();
		boolean debug = minerState.parameters.isDebug();
		CutFinderIMinInfo info = new CutFinderIMinInfo(logInfo.getDfg(), graph, transitiveGraph,
				minimumSelfDistancesBetween, satProbabilities, jobList, debug);
		return findCut(info, threshold, minerState);
	}

	public static Cut findCut(CutFinderIMinInfo info, float threshold, MinerStateBase minerState) {
		AtomicResult bestSATResult = new AtomicResult(threshold);
		(new SATSolveXor(info, bestSATResult, minerState)).solve();
		(new SATSolveParallel(info, bestSATResult, minerState)).solve();

		(new SATSolveSequence(info, bestSATResult, minerState)).solve();

		(new SATSolveLoop(info, bestSATResult, minerState)).solve();

		try {
			info.getJobList().join();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}

		//long end2 = (new Date()).getTime() - start2;

		//System.out.println("yices " + end1 + ", sat4j " + end2);

		SATResult satResult = bestSATResult.get();

		return satResult.getCut();
	}
}
