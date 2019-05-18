package org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Reduce a Petri net using Murata's Fusion of Elimination of Self-Loop
 * Transitions. However, preserve the language of the net, i.e. only remove
 * invisible transitions.
 * 
 * Adapted from Eric Verbeek's code in the Murata package of ProM.
 * 
 * @author sander
 *
 */

public class MurataESTkeepLanguage {

	public static boolean reduce(AcceptingPetriNet anet, Canceller canceller) {
		Petrinet net = anet.getNet();

		/*
		 * Iterate over all transitions.
		 */
		for (Transition transition : net.getTransitions()) {

			if (canceller.isCancelled()) {
				return true;
			}

			/*
			 * Check whether the transition is silent.
			 */
			if (!transition.isInvisible()) {
				continue;
			}

			/*
			 * Check input arc.
			 */
			int weight;
			Arc inputArc;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
						.getInEdges(transition);
				if (preset.size() != 1) {
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = preset.iterator().next();
				if (!(edge instanceof Arc)) {
					continue;
				}
				inputArc = (Arc) edge;
				weight = inputArc.getWeight();
			}

			/*
			 * Check output arc.
			 */
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
						.getOutEdges(transition);
				if (postset.size() != 1) {
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = postset.iterator().next();
				if (!(edge instanceof Arc)) {
					continue;
				}
				Arc outputArc = (Arc) edge;
				if (outputArc.getWeight() != weight) {
					continue;
				}

				/*
				 * Check whether self loop.
				 */
				if (inputArc.getSource() != outputArc.getTarget()) {
					continue;
				}
			}

			/*
			 * We have a self loop for a transition. Remove the transition.
			 */
			net.removeTransition(transition);
			return true; // A transition has been removed.

		}
		return false;
	}

}
