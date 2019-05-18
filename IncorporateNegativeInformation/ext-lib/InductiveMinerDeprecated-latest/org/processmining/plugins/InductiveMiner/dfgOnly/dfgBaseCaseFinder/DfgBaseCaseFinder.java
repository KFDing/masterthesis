package org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface DfgBaseCaseFinder {

	/**
	 * usage: if there is no base case in this log, returns null if there is a
	 * base case, returns a Node. Each (in)direct child of that Node must be
	 * attached to tree.
	 * 
	 * Must be thread-safe and static, i.e, no side-effects allowed.
	 * @param dfg
	 * @param tree
	 * @param minerState
	 * @return
	 */
	Node findBaseCases(Dfg dfg, ProcessTree tree, DfgMinerState minerState);

}
