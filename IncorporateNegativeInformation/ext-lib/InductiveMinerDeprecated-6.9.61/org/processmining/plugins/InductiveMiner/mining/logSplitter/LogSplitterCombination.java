package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class LogSplitterCombination implements LogSplitter {

	public final LogSplitter xor;
	public final LogSplitter sequence;
	public final LogSplitter concurrent;
	public final LogSplitter loop;
	public final LogSplitter maybeInterleaved;
	public final LogSplitter interleaved;
	public final LogSplitter or;

	public LogSplitterCombination(LogSplitter xor, LogSplitter sequence, LogSplitter concurrent, LogSplitter loop,
			LogSplitter maybeInterleaved, LogSplitter interleaved, LogSplitter or) {
		this.xor = xor;
		this.sequence = sequence;
		this.concurrent = concurrent;
		this.loop = loop;
		this.maybeInterleaved = maybeInterleaved;
		this.interleaved = interleaved;
		this.or = or;
	}
	
	@Deprecated
	public LogSplitterCombination(LogSplitter xor, LogSplitter sequence, LogSplitter concurrent, LogSplitter loop,
			LogSplitter maybeInterleaved, LogSplitter interleaved) {
		this.xor = xor;
		this.sequence = sequence;
		this.concurrent = concurrent;
		this.loop = loop;
		this.maybeInterleaved = maybeInterleaved;
		this.interleaved = interleaved;
		this.or = concurrent;
	}

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		switch (cut.getOperator()) {
			case xor :
				return xor.split(log, logInfo, cut, minerState);
			case sequence :
				return sequence.split(log, logInfo, cut, minerState);
			case concurrent :
				return concurrent.split(log, logInfo, cut, minerState);
			case loop :
				return loop.split(log, logInfo, cut, minerState);
			case maybeInterleaved :
				return maybeInterleaved.split(log, logInfo, cut, minerState);
			case interleaved :
				return interleaved.split(log, logInfo, cut, minerState);
			case or :
				return or.split(log, logInfo, cut, minerState);
		}
		try {
			throw new UnknownTreeNodeException();
		} catch (UnknownTreeNodeException e) {
			e.printStackTrace();
		}
		return null;
	}
}
