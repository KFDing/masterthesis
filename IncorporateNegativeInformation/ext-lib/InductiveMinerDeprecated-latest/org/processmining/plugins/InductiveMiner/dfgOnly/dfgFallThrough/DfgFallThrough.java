package org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public interface DfgFallThrough {

	/**
	 * usage: returns a Node. Each (in)direct child of that Node must be
	 * attached to tree. Is allowed to return null, but there should at least be
	 * one fall-through that succeeds.
	 * 
	 * @param dfg
	 * @param tree
	 * @param minerState
	 * @return
	 */
	Node fallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState);

}
