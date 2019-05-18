package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoop implements FallThrough {

	private final boolean useLifeCycle;

	/**
	 * 
	 * @param useLifeCycle
	 *            Denotes whether activity instances (i.e. combination of start
	 *            &nbsp; a complete event) should be kept together at all times.
	 *            True = keep activity instances together; false = activity
	 *            instances may be split.
	 */
	public FallThroughTauLoop(boolean useLifeCycle) {
		this.useLifeCycle = useLifeCycle;
	}

	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().toSet().size() > 1) {

			//try to find a tau loop
			XLog sublog = new XLogImpl(new XAttributeMapImpl());

			for (IMTrace trace : log) {
				filterTrace(log, sublog, trace, logInfo.getDfg(), useLifeCycle);
			}

			if (sublog.size() > logInfo.getNumberOfTraces()) {
				Miner.debug(" fall through: tau loop", minerState);
				//making a tau loop split makes sense
				Block loop = new XorLoop("");
				Miner.addNode(tree, loop);

				{
					Node body;
					if (log instanceof IMLogStartEndComplete) {
						body = Miner.mineNode(
								new IMLogStartEndComplete(sublog, log.getClassifier(), log.getLifeCycleClassifier()),
								tree, minerState);
					} else {
						body = Miner.mineNode(new IMLogImpl(sublog, log.getClassifier(), log.getLifeCycleClassifier()),
								tree, minerState);
					}
					Miner.addChild(loop, body, minerState);
				}

				{
					Node redo = new Automatic("tau");
					Miner.addNode(tree, redo);
					Miner.addChild(loop, redo, minerState);
				}

				{
					Node exit = new Automatic("tau");
					Miner.addNode(tree, exit);
					Miner.addChild(loop, exit, minerState);
				}

				return loop;
			}
		}

		return null;
	}

	public static void filterTrace(IMLog log, XLog sublog, IMTrace trace, Dfg dfg, boolean useLifeCycle) {
		boolean first = true;
		XTrace partialTrace = new XTraceImpl(new XAttributeMapImpl());

		MultiSet<XEventClass> openActivityInstances = new MultiSet<>();

		for (XEvent event : trace) {

			XEventClass activity = log.classify(trace, event);

			if (!first && dfg.isStartActivity(activity)) {
				//we discovered a transition body -> body
				//check whether there are no open activity instances
				if (!useLifeCycle || openActivityInstances.size() == 0) {
					sublog.add(partialTrace);
					partialTrace = new XTraceImpl(new XAttributeMapImpl());
					first = true;
				}
			}

			if (useLifeCycle) {
				if (log.getLifeCycle(event) == Transition.complete) {
					if (openActivityInstances.getCardinalityOf(activity) > 0) {
						openActivityInstances.remove(activity, 1);
					}
				} else if (log.getLifeCycle(event) == Transition.start) {
					openActivityInstances.add(log.classify(trace, event));
				}
			}

			partialTrace.add(event);
			first = false;
		}
		sublog.add(partialTrace);
	}
}
