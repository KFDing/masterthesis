package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles;

public class LogSplitterIMlc implements LogSplitter {

	private static final LogSplitter logSplitterIMi = new LogSplitterIMi();

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {

		LogSplitResult result = logSplitterIMi.split(log, logInfo, cut, minerState);

		//if the operator is sequence or loop, check for inconsistent traces and repair them
		if (minerState.parameters.isRepairLifeCycle() && cut.getOperator() != Operator.xor
				&& cut.getOperator() != Operator.concurrent) {
			List<IMLog> newSublogs = new ArrayList<>();
			for (IMLog sublog : result.sublogs) {
				newSublogs.add(new LifeCycles(minerState.parameters.isDebug()).preProcessLog(sublog));
			}
			result.sublogs = newSublogs;
		}

		return result;
	}

}
