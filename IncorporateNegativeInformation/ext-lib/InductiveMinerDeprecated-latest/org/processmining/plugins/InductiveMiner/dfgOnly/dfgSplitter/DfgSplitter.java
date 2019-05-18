package org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter;

import java.util.List;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public interface DfgSplitter {

	public class DfgSplitResult {
		public List<Dfg> subDfgs;
		public DfgSplitResult(List<Dfg> subDfgs) {
			this.subDfgs = subDfgs;
		}
	}
	
	/**
	 * usage: returns a list of sublogs and a multiset of noise events
	 * 
	 * Must be thread-safe and static, i.e, no side-effects allowed.
	 * @param dfg
	 * @param cut
	 * @param minerState
	 * @return
	 */
	DfgSplitResult split(Dfg dfg, Cut cut, DfgMinerState minerState);

}
