package org.processmining.plugins.InductiveMiner.mining.baseCases;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask;

public class BaseCaseFinderIMiSingleActivity implements BaseCaseFinder {

	public Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().setSize() == 1) {
			//the log contains just one activity

			//assuming application of the activity follows a geometric distribution, we estimate parameter ^p

			//calculate the event-per-trace size of the log
			double p = logInfo.getNumberOfTraces()
					/ ((logInfo.getNumberOfActivityInstances() + logInfo.getNumberOfTraces()) * 1.0);

			if (0.5 - minerState.parameters.getNoiseThreshold() <= p
					&& p <= 0.5 + minerState.parameters.getNoiseThreshold()) {
				//^p is close enough to 0.5, consider it as a single activity

				Miner.debug(" base case: IMi single activity", minerState);

				XEventClass activity = logInfo.getActivities().iterator().next();
				Node node = new AbstractTask.Manual(activity.toString());
				Miner.addNode(tree, node);

				return node;
			}
			//else, the probability to stop is too low or too high, and we better output a flower model
		}

		return null;
	}
}
