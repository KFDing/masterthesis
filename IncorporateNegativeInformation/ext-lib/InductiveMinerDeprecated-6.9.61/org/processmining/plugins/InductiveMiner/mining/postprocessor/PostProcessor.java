package org.processmining.plugins.InductiveMiner.mining.postprocessor;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;

public interface PostProcessor {
	public Node postProcess(Node node, IMLog log, IMLogInfo logInfo, MinerState minerState);
}
