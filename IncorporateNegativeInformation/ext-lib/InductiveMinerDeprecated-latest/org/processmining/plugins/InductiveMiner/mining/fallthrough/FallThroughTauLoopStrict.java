package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class FallThroughTauLoopStrict implements FallThrough {

	private final boolean useLifeCycle;
	
	/**
	 * 
	 * @param useLifeCycle
	 *            Denotes whether activity instances (i.e. combination of start
	 *            &nbsp; a complete event) should be kept together at all times. True
	 *            = keep activity instances together; false = activity instances
	 *            may be split.
	 */
	public FallThroughTauLoopStrict(boolean useLifeCycle) {
		this.useLifeCycle = useLifeCycle;
	}
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

		if (logInfo.getActivities().toSet().size() > 1) {

			//try to find a tau loop
			IMLog sublog = log.clone();
			if (filterLog(sublog, logInfo.getDfg(), useLifeCycle)) {

				Miner.debug(" fall through: tau loop strict", minerState);
				//making a tau loop split makes sense
				Block loop = new XorLoop("");
				Miner.addNode(tree, loop);

				{
					Node body = Miner.mineNode(sublog, tree, minerState);
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

	/**
	 * Split the trace on a crossing end to start
	 * 
	 * @param log
	 * @param dfg
	 * @return
	 */
	public static boolean filterLog(IMLog log, Dfg dfg, boolean useLifeCycle) {
		boolean somethingSplit = false;

		for (IMTrace trace : log) {
			MultiSet<XEventClass> openActivityInstances = new MultiSet<>();
			
			boolean lastEnd = false;
			IMEventIterator it = trace.iterator();
			while (it.hasNext()) {

				XEvent event = it.next();

				XEventClass activity = it.classify();

				if (lastEnd && dfg.isStartActivity(activity) && (!useLifeCycle || openActivityInstances.size() == 0)) {
					it.split();
					somethingSplit = true;
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

				lastEnd = dfg.isEndActivity(activity);
			}
		}
		return somethingSplit;
	}
}
