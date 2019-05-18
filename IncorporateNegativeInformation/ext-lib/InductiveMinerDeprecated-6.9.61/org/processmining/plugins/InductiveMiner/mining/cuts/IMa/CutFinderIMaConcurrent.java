package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.graphs.Cliques;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.UndirectedSimpleGraph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMConcurrentWithMinimumSelfDistance;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

/**
 * Detects concurrent cuts, and takes care to not discover cuts that might
 * introduce false taus.
 * 
 * @author sleemans
 *
 */
public class CutFinderIMaConcurrent implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut cut = CutFinderIMConcurrentWithMinimumSelfDistance.findCutImpl(log, logInfo, minerState);

		if (cut == null || !cut.isValid()) {
			return null;
		}

		if (cut.getPartition().size() == 2) {
			return cut;
		}

		/**
		 * Walk through the event log to discovery which components of the cut
		 * always are skipped together.
		 */

		Components<XEventClass> components = new Components<XEventClass>(cut.getPartition());
		BitSet componentsWithEventInTrace = new BitSet(components.getNumberOfComponents());
		UndirectedSimpleGraph<Collection<XEventClass>> graph = new UndirectedSimpleGraph(Collection.class,
				components.getComponents());

		for (IMTrace trace : log) {

			//record which components have at least one event in this trace
			componentsWithEventInTrace.clear();
			for (IMEventIterator it = trace.iterator(); it.hasNext();) {
				it.next();
				componentsWithEventInTrace.set(components.getComponentOf(it.classify()));
			}

			/**
			 * Update the graph: if we have seen evidence of difference in
			 * producing tau, i.e. missing events, then record this in the
			 * graph.
			 */
			for (int component1 = 0; component1 < components.getNumberOfComponents(); component1++) {
				for (int component2 = component1 + 1; component2 < components.getNumberOfComponents(); component2++) {
					if (componentsWithEventInTrace.get(component1) != componentsWithEventInTrace.get(component2)) {
						graph.addEdge(component1, component2);
					}
				}
			}
		}

		/**
		 * Invert the graph. Now, the graph only contains edges between
		 * components that have never seen a difference in occurrence.
		 */
		graph.invert();

		/**
		 * Now we have to find cliques in the graph. Finding all maximal cliques
		 * is NP-complete, however we have only two requirements: 1) fitness
		 * should be preserved, i.e. if there is no edge in the negated graph,
		 * components should not be merged, and 2) rediscoverability, i.e. we're
		 * only considering cases in which there are clearly separated cliques.
		 */

		Components<Collection<XEventClass>> cliques = Cliques.compute(graph);
		List<Set<XEventClass>> y = flatten(cliques.getComponents());

		return new Cut(Operator.concurrent, y);
	}

	public List<Set<XEventClass>> flatten(List<Set<Collection<XEventClass>>> list) {
		List<Set<XEventClass>> result = new ArrayList<>();
		for (Set<Collection<XEventClass>> sigma : list) {
			Set<XEventClass> r = new THashSet<>();
			for (Collection<XEventClass> s : sigma) {
				r.addAll(s);
			}
			result.add(r);
		}
		return result;
	}
}
