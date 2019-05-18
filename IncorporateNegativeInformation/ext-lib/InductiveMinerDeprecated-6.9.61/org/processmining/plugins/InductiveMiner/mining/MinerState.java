package org.processmining.plugins.InductiveMiner.mining;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.MultiSet;

import com.google.common.util.concurrent.MoreExecutors;

public class MinerState implements MinerStateBase {
	public final MultiSet<XEventClass> discardedEvents;
	public final MiningParameters parameters;
	private final ExecutorService minerPool;
	private final ExecutorService satPool;
	private final Canceller canceller;

	public MinerState(MiningParameters parameters, Canceller canceller) {
		this.parameters = parameters;
		this.discardedEvents = new MultiSet<XEventClass>();
		this.canceller = canceller;

		if (!parameters.isUseMultithreading()) {
			minerPool = MoreExecutors.sameThreadExecutor();
			satPool = MoreExecutors.sameThreadExecutor();
		} else {
			minerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, "IM miner pool thread");
				}
			});
			satPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, "IM sat pool thread");
				}
			});
		}
	}

	public ExecutorService getMinerPool() {
		return minerPool;
	}

	public ExecutorService getSatPool() {
		return satPool;
	}

	public boolean isCancelled() {
		return canceller.isCancelled();
	}

	public void shutdownThreadPools() {
		minerPool.shutdownNow();
		satPool.shutdownNow();
	}
}
