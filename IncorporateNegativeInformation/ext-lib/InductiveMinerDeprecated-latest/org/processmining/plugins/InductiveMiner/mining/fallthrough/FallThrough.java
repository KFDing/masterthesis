package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface FallThrough {

	/**
	 * usage: returns a Node. Each (in)direct child of that Node must be
	 * attached to tree. Is allowed to return null, but there should at least be
	 * one fall-through that succeeds.
	 * 
	 * 
	 * @param log
	 * @param logInfo
	 * @param tree
	 * @param minerState
	 * @return
	 */
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState);
}
