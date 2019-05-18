package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.processtree.ProcessTree;

public class IMdProcessTree {

	public static ProcessTree mineProcessTree(Dfg dfg, DfgMiningParameters parameters) {
		return DfgMiner.mine(dfg, parameters, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
	}

	public static ProcessTree mineProcessTree(Dfg dfg, DfgMiningParameters parameters, Canceller canceller) {
		return DfgMiner.mine(dfg, parameters, canceller);
	}
}
