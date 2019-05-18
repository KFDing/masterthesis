package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import java.util.BitSet;
import java.util.Map.Entry;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

/**
 * Detect an interleaved cut. Guarantee local fitness preservation by walking
 * over the log.
 * 
 * @author sleemans
 *
 */
public class CutFinderIMaInterleaved implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(log, logInfo, logInfo.getDfg(), true);
	}

	public static Cut findCut(IMLog log, IMLogInfo logInfo, Dfg dfg, boolean preserveFitness) {
		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

		//put each activity in a component.
		final Components<XEventClass> components = new Components<XEventClass>(graph.getVertices());

		/*
		 * By semantics of the interleaved operator, a non-start activity cannot
		 * have connections from other subtrees. Thus, walk over all such
		 * activities and merge components.
		 */
		for (int activityIndex : graph.getVertexIndices()) {
			if (!dfg.isStartActivity(activityIndex)) {
				for (long edgeIndex : graph.getIncomingEdgesOf(activityIndex)) {
					int source = graph.getEdgeSourceIndex(edgeIndex);
					components.mergeComponentsOf(source, activityIndex);
				}
			}
			if (!dfg.isEndActivity(activityIndex)) {
				for (long edgeIndex : graph.getOutgoingEdgesOf(activityIndex)) {
					int target = graph.getEdgeTargetIndex(edgeIndex);
					components.mergeComponentsOf(activityIndex, target);
				}
			}
		}

		/*
		 * All start activities need to be doubly connected from all end
		 * activities from other components. Thus, walk through the start
		 * activities and end activities and merge violating pairs. The reverse
		 * direction is implied.
		 */
		for (int startActivity : dfg.getStartActivityIndices()) {
			for (int endActivity : dfg.getEndActivityIndices()) {
				if (startActivity != endActivity) {
					if (!graph.containsEdge(endActivity, startActivity)) {
						components.mergeComponentsOf(startActivity, endActivity);
					}
				}
			}
		}

		/*
		 * Between components, there cannot be minimum self-distance
		 * connections.
		 */
		if (logInfo != null) {
			for (Entry<XEventClass, MultiSet<XEventClass>> e : logInfo.getMinimumSelfDistancesBetween().entrySet()) {
				for (XEventClass v : e.getValue()) {
					components.mergeComponentsOf(e.getKey(), v);
				}
			}
		}

		if (components.getNumberOfComponents() < 2) {
			return null;
		}

		if (preserveFitness) {
			/*
			 * Perform an extra check for fitness: walk through all the traces
			 * and make sure that the interleaving is not violated, i.e. that no
			 * trace enters a component twice.
			 */

			for (IMTrace trace : log) {
				int currentComponent;
				BitSet enteredComponents = new BitSet(components.getNumberOfComponents());
				boolean done = false;

				while (!done) {
					done = true;
					currentComponent = -1;
					enteredComponents.clear();
					for (IMEventIterator it = trace.iterator(); it.hasNext();) {
						it.next();
						int component = components.getComponentOf(it.classify());
						if (component != currentComponent) {
							//we are entering a new component with this event
							//check whether it was visited before
							if (!enteredComponents.get(component)) {
								//first time we are entering this component: no problem
								enteredComponents.set(component);
							} else {
								//second time we are entering this component: fitness violation
								//merge the components
								components.mergeComponents(currentComponent, component);
								//recheck the trace
								done = false;
								break;
							}
							currentComponent = component;
						}
					}
				}
			}
		}

		return new Cut(Operator.interleaved, components.getComponents());
	}
}
