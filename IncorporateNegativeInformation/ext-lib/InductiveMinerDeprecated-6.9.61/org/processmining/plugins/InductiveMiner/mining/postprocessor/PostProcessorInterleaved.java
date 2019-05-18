package org.processmining.plugins.InductiveMiner.mining.postprocessor;

import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.interleaved.DetectInterleaved;
import org.processmining.plugins.InductiveMiner.mining.interleaved.MaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;

/**
 * replace interleaved nodes if necessary
 * 
 * @author sleemans
 *
 */
public class PostProcessorInterleaved implements PostProcessor {

	public Node postProcess(Node node, IMLog log, IMLogInfo logInfo, MinerState state) {

		if (node instanceof MaybeInterleaved) {
			try {
				node = DetectInterleaved.remove((MaybeInterleaved) node, state.parameters.getReduceParameters());
			} catch (UnknownTreeNodeException e) {
				e.printStackTrace();
			}
		}
		return node;
	}

}
