package org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.MurataUtils;

/**
 * Reduce a Petri net using Murata's Fusion of Series Transitions. However,
 * preserve the language of the net, i.e. only remove invisible transitions.
 * 
 * Adapted from Eric Verbeek's code in the Murata package of ProM.
 * 
 * @author sander
 *
 */

public class MurataFST2keepLanguage {

	public static boolean reduce(AcceptingPetriNet anet, Canceller canceller) {
		Petrinet net = anet.getNet();

		/*
		 * Iterate over all places.
		 */
		for (Place place : net.getPlaces()) {

			if (canceller.isCancelled()) {
				return false;
			}

			/*
			 * The place should not be part of any marking
			 */
			{
				if (anet.getInitialMarking().contains(place)) {
					continue;
				}
				boolean ok = true;
				for (Marking marking : anet.getFinalMarkings()) {
					if (marking.contains(place)) {
						ok = false;
					}
				}
				if (!ok) {
					continue;
				}
			}

			/*
			 * Check the input arc. There should be only one and it should be
			 * regular.
			 */
			int weight;
			Arc inputArc;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net.getInEdges(place);
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
			 * Check the input transition. It should have only one outgoing arc.
			 */
			Transition inputTransition = (Transition) inputArc.getSource();
			if (net.getOutEdges(inputTransition).size() > 1) {
				continue;
			}

			/*
			 * Check the output arc. There should be only one, it should be
			 * regular, and its weight should be identical.
			 */
			Arc outputArc;
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
						.getOutEdges(place);
				if (postset.size() != 1) {
					continue;
				}
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = postset.iterator().next();
				if (!(edge instanceof Arc)) {
					continue;
				}
				outputArc = (Arc) edge;
				if (outputArc.getWeight() != weight) {
					continue;
				}
			}

			/*
			 * Get the output transition. Should have only the place as input.
			 */
			Transition outputTransition;
			{
				outputTransition = (Transition) outputArc.getTarget();
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
						.getInEdges(outputTransition);
				if (preset.size() != 1) {
					continue;
				}
			}

			if (inputTransition == outputTransition) {
				continue;
			}

			/*
			 * The input (inclusive) or the output transition should be
			 * invisible (and the invisible one gets deleted).
			 */
			if (inputTransition.isInvisible()) {
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
						.getInEdges(inputTransition);
				MurataUtils.resetPlace(anet.getInitialMarking(), place);

				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> transferEdge : preset) {
					if (transferEdge instanceof Arc) {
						Arc transferArc = (Arc) transferEdge;
						MurataUtils.addArc(net, transferArc.getSource(), outputTransition, transferArc.getWeight());
					}
				}
				net.removePlace(place);
				net.removeTransition(inputTransition);
				return true; // Removed a place and a transition.
			} else if (outputTransition.isInvisible()) {
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
						.getOutEdges(outputTransition);
				MurataUtils.resetPlace(anet.getInitialMarking(), place);

				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> transferEdge : postset) {
					if (transferEdge instanceof Arc) {
						Arc transferArc = (Arc) transferEdge;
						MurataUtils.addArc(net, inputTransition, transferArc.getTarget(), transferArc.getWeight());
					}
				}
				net.removePlace(place);
				net.removeTransition(outputTransition);
				return true; // Removed a place and a transition.
			}
		}

		return false;
	}

}
