package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;

import com.google.common.util.concurrent.MoreExecutors;

public class DfgMinerState implements MinerStateBase {
	private final DfgMiningParameters parameters;
	private final MultiSet<XEventClass> discardedEvents;
	private final ExecutorService satPool;
	private final Canceller canceller;
	
	public DfgMinerState(DfgMiningParameters parameters, Canceller canceller) {
		this.parameters = parameters;
		discardedEvents = new MultiSet<>();
		this.canceller = canceller;
		
		if (!parameters.isUseMultiThreading()) {
			satPool = MoreExecutors.sameThreadExecutor();
		} else {
			satPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		}
	}

	public DfgMiningParameters getParameters() {
		return parameters;
	}

	public MultiSet<XEventClass> getDiscardedEvents() {
		return discardedEvents;
	}
	
	public ExecutorService getSatPool() {
		return satPool;
	}
	
	public void shutdownThreadPools() {
		satPool.shutdownNow();
	}

	public boolean isCancelled() {
		return canceller.isCancelled();
	}
}
