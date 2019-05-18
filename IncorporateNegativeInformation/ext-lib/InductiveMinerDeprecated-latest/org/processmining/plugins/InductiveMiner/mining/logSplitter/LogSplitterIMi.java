package org.processmining.plugins.InductiveMiner.mining.logSplitter;


public class LogSplitterIMi extends LogSplitterCombination {

	public LogSplitterIMi() {
		super(
				new LogSplitterXorFiltering(), 
				new LogSplitterSequenceFiltering(), 
				new LogSplitterParallel(),
				new LogSplitterLoop(),
				new LogSplitterMaybeInterleaved(),
				new LogSplitterParallel(),
				new LogSplitterOr());
	}
}