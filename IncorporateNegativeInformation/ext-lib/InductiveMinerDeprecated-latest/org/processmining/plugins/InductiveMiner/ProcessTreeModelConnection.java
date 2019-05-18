package org.processmining.plugins.InductiveMiner;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.processtree.ProcessTree;


public class ProcessTreeModelConnection extends AbstractProcessTreeModelConnection<MiningParameters> {
	public ProcessTreeModelConnection(XLog log, ProcessTree model, MiningParameters parameters) {
		super(log, model, parameters);
	}
}
