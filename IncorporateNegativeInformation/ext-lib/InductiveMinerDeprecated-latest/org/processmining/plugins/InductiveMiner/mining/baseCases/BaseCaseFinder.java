package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface BaseCaseFinder {

	/**
	 * usage: if there is no base case in this log, returns null. If there is a
	 * base case, returns a Node. Each (in)direct child of that Node must be
	 * attached to tree.
	 * 
	 * Must be thread-safe and static, i.e, no side-effects allowed.
	 * 
	 * @param log
	 * @param logInfo
	 * @param tree
	 * @param minerState
	 * @return
	 */
	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState);
}
