package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.processtree.ProcessTree;

public class IMProcessTree {

	public ProcessTree mineProcessTree(PluginContext context, XLog xlog) {
		return mineProcessTree(xlog);
	}

	public static ProcessTree mineProcessTree(XLog xlog) {
		return mineProcessTree(xlog, new MiningParametersIM());
	}

	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters) {
		//prepare the log
		IMLog log = new IMLogImpl(xlog, parameters.getClassifier(), parameters.getLifeCycleClassifier());
		return mineProcessTree(log, parameters);
	}

	public static ProcessTree mineProcessTree(XLog xlog, MiningParameters parameters, Canceller canceller) {
		//prepare the log
		IMLog log = new IMLogImpl(xlog, parameters.getClassifier(), parameters.getLifeCycleClassifier());
		return mineProcessTree(log, parameters, canceller);
	}

	public static ProcessTree mineProcessTree(IMLog log, MiningParameters parameters) {
		return Miner.mine(log, parameters, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
	}

	public static ProcessTree mineProcessTree(IMLog log, MiningParameters parameters, Canceller canceller) {
		return Miner.mine(log, parameters, canceller);
	}
}
