package org.processmining.plugins.InductiveMiner.mining;

import java.util.concurrent.ExecutorService;

public interface MinerStateBase {
	public ExecutorService getSatPool();
	
	public boolean isCancelled();
}
