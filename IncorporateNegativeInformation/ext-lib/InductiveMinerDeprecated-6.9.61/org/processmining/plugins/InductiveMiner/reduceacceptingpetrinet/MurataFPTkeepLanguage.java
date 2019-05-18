package org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Reduce a Petri net using Murata's Fusion of Series Transitions. However,
 * preserve the language of the net, i.e. only remove invisible transitions.
 * 
 * Adapted from Eric Verbeek's code in the Murata package of ProM.
 * 
 * @author sander
 *
 */

public class MurataFPTkeepLanguage {

	public static boolean reduce(AcceptingPetriNet anet, Canceller canceller) {
		Petrinet net = anet.getNet();

		HashMap<Transition, HashSet<Arc>> inputMap = new HashMap<Transition, HashSet<Arc>>();
		HashMap<Transition, HashSet<Arc>> outputMap = new HashMap<Transition, HashSet<Arc>>();
		/*
		 * Iterate over all transitions. Build inputMap and outputMap if all
		 * incident edges regular.
		 */
		for (Transition transition : net.getTransitions()) {

			if (canceller.isCancelled()) {
				return true;
			}

			/*
			 * Get input edges. Should all be regular.
			 */
			HashSet<Arc> inputArcs;
			{
				boolean ok;
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
						.getInEdges(transition);
				inputArcs = new HashSet<Arc>();
				ok = true;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
					if (edge instanceof Arc) {
						inputArcs.add((Arc) edge);
					} else {
						ok = false;
					}
				}
				if (!ok) {
					continue;
				}
			}

			/*
			 * Get output edges. Should all be regular.
			 */
			HashSet<Arc> outputArcs;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
						.getOutEdges(transition);
				outputArcs = new HashSet<Arc>();
				boolean ok = true;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
					if (edge instanceof Arc) {
						outputArcs.add((Arc) edge);
					} else {
						ok = false;
					}
				}
				if (!ok) {
					continue;
				}
			}

			inputMap.put(transition, inputArcs);
			outputMap.put(transition, outputArcs);
		}

		/*
		 * Iterate over all transitions with only regular incident edges.
		 */
		for (Transition transition : inputMap.keySet()) {

			if (!transition.isInvisible()) {
				continue;
			}

			HashSet<Arc> inputArcs = inputMap.get(transition);
			HashSet<Arc> outputArcs = outputMap.get(transition);

			/*
			 * Checking for matching transitions.
			 */
			for (Transition siblingTransition : inputMap.keySet()) {

				if (!siblingTransition.isInvisible()) {
					continue;
				}

				if (siblingTransition == transition) {
					continue;
				}

				HashSet<Arc> siblingInputArcs = inputMap.get(siblingTransition);
				HashSet<Arc> siblingOutputArcs = outputMap.get(siblingTransition);
				if (siblingInputArcs.size() != inputArcs.size()) {
					continue;
				}
				if (siblingOutputArcs.size() != outputArcs.size()) {
					continue;
				}
				boolean equal = true;
				boolean found;
				for (Arc arc : inputArcs) {
					if (equal) {
						found = false;
						for (Arc siblingArc : siblingInputArcs) {
							if ((arc.getSource() == siblingArc.getSource())
									&& (arc.getWeight() == siblingArc.getWeight())) {
								found = true;
							}
						}
						if (!found) {
							equal = false;
						}
					}
				}
				for (Arc arc : outputArcs) {
					if (equal) {
						found = false;
						for (Arc siblingArc : siblingOutputArcs) {
							if ((arc.getTarget() == siblingArc.getTarget())
									&& (arc.getWeight() == siblingArc.getWeight())) {
								found = true;
							}
						}
						if (!found) {
							equal = false;
						}
					}
				}
				if (equal) {
					/*
					 * Found a sibling with identical inputs and outputs. Remove
					 * the sibling.
					 */

					net.removeTransition(siblingTransition);
					return true;
				}
			}
		}
		return false;
	}
}
