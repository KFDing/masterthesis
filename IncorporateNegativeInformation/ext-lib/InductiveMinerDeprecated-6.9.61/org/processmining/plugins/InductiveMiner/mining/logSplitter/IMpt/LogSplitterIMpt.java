package org.processmining.plugins.InductiveMiner.mining.logSplitter.IMpt;

import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterMaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterOr;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXorFiltering;

public class LogSplitterIMpt extends LogSplitterCombination {

	public LogSplitterIMpt() {
		super(
				new LogSplitterXorFiltering(), 
				new LogSplitterSequenceFilteringIMpt(), 
				new LogSplitterParallel(),
				new LogSplitterLoopIMpt(),
				new LogSplitterMaybeInterleaved(),
				new LogSplitterParallel(),
				new LogSplitterOr());
	}
}