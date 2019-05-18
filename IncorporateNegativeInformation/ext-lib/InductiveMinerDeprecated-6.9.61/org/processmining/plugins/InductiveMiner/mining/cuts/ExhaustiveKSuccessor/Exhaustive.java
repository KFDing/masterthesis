package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.jobList.ThreadPoolMiner;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIMi;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class Exhaustive {

	public class Result {
		int distance;
		public Cut cut;
		public Collection<IMLog> sublogs;
	}

	private UpToKSuccessorMatrix kSuccessor;
	private IMLog log;
	private IMLogInfo logInfo;
	private MinerState minerState;
	private ThreadPoolMiner pool;
	private final AtomicInteger bestTillNow;
	
	private final LogSplitter logSplitter;
	private final IMLog2IMLogInfo log2dfg;

	public Exhaustive(IMLog log, IMLogInfo logInfo, UpToKSuccessorMatrix kSuccessor, MinerState minerState) {
		this.kSuccessor = kSuccessor;
		this.log = log;
		this.logInfo = logInfo;
		this.minerState = minerState;
		bestTillNow = new AtomicInteger();
		logSplitter = new LogSplitterIMi();
		log2dfg = minerState.parameters.getLog2LogInfo();
	}

	public Result tryAll() {
		final int nrOfBits = logInfo.getActivities().setSize();

		final XEventClass[] activities = new XEventClass[logInfo.getActivities().toSet().size()];
		int i = 0;
		for (XEventClass e : logInfo.getActivities()) {
			activities[i] = e;
			i++;
		}

		pool = ThreadPoolMiner.useFactor(2);
		int threads = pool.getNumerOfThreads();
		final Result[] results = new Result[threads];
		bestTillNow.set(Integer.MAX_VALUE);

		long globalStartCutNr = 1;
		long globalEndCutNr = (int) (Math.pow(2, nrOfBits) - 1);

		long lastEnd = globalStartCutNr - 1;
		long step = (globalEndCutNr - globalStartCutNr) / threads;

		//debug("Start threads " + globalStartCutNr + " " + globalEndCutNr);

		for (int t = 0; t < threads; t++) {
			final long startCutNr = lastEnd + 1;
			final long endCutNr = startCutNr + step;
			lastEnd = endCutNr;
			final int threadNr = t;
			//debug("Start thread  " + startCutNr + " " + endCutNr);
			pool.addJob(new Runnable() {
				public void run() {
					results[threadNr] = tryRange(nrOfBits, activities, startCutNr, endCutNr);
				}
			});
		}

		try {
			pool.join();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}

		Result result = new Result();
		result.distance = Integer.MAX_VALUE;
		for (int t = 0; t < threads; t++) {
			if (results[t].distance < result.distance) {
				result = results[t];
			}
		}

		return result;
	}

	public Result tryRange(int nrOfBits, final XEventClass[] activities, long startCutNr, long endCutNr) {
		Result result = new Result();
		result.distance = Integer.MAX_VALUE;
		Result result2;
		List<Set<XEventClass>> partition;
		for (long cutNr = startCutNr; cutNr < Math.pow(2, nrOfBits) - 1 && result.distance > 0 && cutNr <= endCutNr; cutNr++) {
			partition = generateCut(cutNr, nrOfBits, activities);

			//parallel
			result2 = processCutParallel(partition);
			if (result.distance > result2.distance) {
				result = result2;
				if (updateBestTillNow(result2.distance)) {
					Miner.debug(result2.distance + " " + result2.cut, minerState);
				}
			}

			//loop
			result2 = processCutLoop(partition);
			if (result.distance > result2.distance) {
				result = result2;
				if (updateBestTillNow(result2.distance)) {
					Miner.debug(result2.distance + " " + result2.cut, minerState);
				}
			}
		}

		return result;
	}

	public Result processCutParallel(Collection<Set<XEventClass>> partition) {

		Result result = new Result();

		//split log
		LogSplitter logSplitter = new LogSplitterIMi();
		Cut cut = new Cut(Operator.concurrent, partition);
		result.sublogs = logSplitter.split(log, logInfo, cut, minerState).sublogs;

		//make k-successor relations
		Iterator<IMLog> it = result.sublogs.iterator();
		IMLog log0 = it.next();
		IMLog log1 = it.next();
		UpToKSuccessorMatrix successor0 = UpToKSuccessor.fromLog(log0, log2dfg.createLogInfo(log0));
		UpToKSuccessorMatrix successor1 = UpToKSuccessor.fromLog(log1, log2dfg.createLogInfo(log1));

		//combine the logs
		UpToKSuccessorMatrix combined = CombineParallel.combine(successor0, successor1);

		result.distance = DistanceEuclidian.computeDistance(kSuccessor, combined);

		result.cut = cut;

		return result;
	}

	public Result processCutLoop(Collection<Set<XEventClass>> partition) {

		Result result = new Result();

		//split log
		Cut cut = new Cut(Operator.loop, partition);
		result.sublogs = logSplitter.split(log, logInfo, cut, minerState).sublogs;

		//make k-successor relations
		Iterator<IMLog> it = result.sublogs.iterator();
		IMLog log0 = it.next();
		IMLog log1 = it.next();
		UpToKSuccessorMatrix successor0 = UpToKSuccessor.fromLog(log0, log2dfg.createLogInfo(log0));
		UpToKSuccessorMatrix successor1 = UpToKSuccessor.fromLog(log1, log2dfg.createLogInfo(log1));

		//combine the logs
		UpToKSuccessorMatrix combined = CombineLoop.combine(successor0, successor1);

		result.distance = DistanceEuclidian.computeDistance(kSuccessor, combined);

		result.cut = cut;

		return result;
	}

	public List<Set<XEventClass>> generateCut(long input, int nrOfBits, XEventClass[] activities) {

		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		Set<XEventClass> a = new HashSet<XEventClass>();
		Set<XEventClass> b = new HashSet<XEventClass>();

		for (int i = nrOfBits - 1; i >= 0; i--) {
			if ((input & (1 << i)) != 0) {
				a.add(activities[i]);
			} else {
				b.add(activities[i]);
			}
		}

		result.add(a);
		result.add(b);

		return result;
	}

	private boolean updateBestTillNow(int newBest) {
		int now = bestTillNow.get();
		if (now > newBest) {
			while (!bestTillNow.compareAndSet(now, newBest)) {
				now = bestTillNow.get();
				if (now <= newBest) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
