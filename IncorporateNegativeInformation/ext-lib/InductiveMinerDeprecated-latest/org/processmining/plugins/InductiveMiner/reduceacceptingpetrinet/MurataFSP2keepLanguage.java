package org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet;

import java.util.ArrayList;
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

import gnu.trove.set.hash.THashSet;

/**
 * Reduce a Petri net using Murata's Fusion of Series Places rule. However,
 * preserve the language of the net, i.e. only remove invisible transitions.
 * 
 * Adapted from Eric Verbeek's code in the Murata package of ProM.
 * 
 * @author sander
 *
 */

public class MurataFSP2keepLanguage {

	public static boolean reduce(AcceptingPetriNet anet, Canceller canceller) {
		boolean reduced = false;
		Petrinet net = anet.getNet();

		/*
		 * Iterate over all transitions.
		 */
		ArrayList<Transition> transitions = new ArrayList<>(net.getTransitions());
		for (Transition transition : transitions) {

			if (canceller.isCancelled()) {
				return false;
			}

			/*
			 * A transition should be invisible, otherwise this rule might
			 * change the language of the model.
			 * 
			 */
			if (!transition.isInvisible()) {
				continue;
			}

			/*
			 * Check the input arc. There should be only one, it should be
			 * regular, and it weight should be one.
			 */
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
					.getInEdges(transition);
			if (preset.size() != 1) {
				continue;
			}
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = preset.iterator().next();
			if (!(edge instanceof Arc)) {
				continue;
			}
			Arc inputArc = (Arc) edge;
			if (inputArc.getWeight() != 1) {
				continue;
			}

			/*
			 * Get the input place. No additional requirements.
			 */
			Place inputPlace = (Place) inputArc.getSource();

			/*
			 * Check the output arc. There should be only one, it should be
			 * regular, and its weight should be one.
			 */
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
					.getOutEdges(transition);
			if (postset.size() != 1) {
				continue;
			}
			edge = postset.iterator().next();
			if (!(edge instanceof Arc)) {
				continue;
			}
			Arc outputArc = (Arc) edge;
			if (outputArc.getWeight() != 1) {
				continue;
			}

			/*
			 * Get the output place. This place should not have any other
			 * incoming arcs.
			 */
			Place outputPlace = (Place) outputArc.getTarget();
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inset = net
					.getInEdges(outputPlace);
			if (inset.size() != 1) {
				continue;
			}

			if (inputPlace == outputPlace) {
				continue;
			}

			reduced = true;
			/*
			 * Remove the input place.
			 */
			{
				/*
				 * Move tokens from input place to output place.
				 */
				{
					{
						int tokens = anet.getInitialMarking().occurrences(inputPlace);
						anet.getInitialMarking().add(outputPlace, tokens);
						MurataUtils.updateLabel(outputPlace, anet.getInitialMarking());
						MurataUtils.resetPlace(anet.getInitialMarking(), inputPlace);
					}

					for (Marking marking : anet.getFinalMarkings()) {
						int tokens = marking.occurrences(inputPlace);
						marking.add(outputPlace, tokens);
						MurataUtils.updateLabel(outputPlace, marking);
						MurataUtils.resetPlace(marking, inputPlace);
					}
				}

				/*
				 * Also, transfer any input edge from the input place to the
				 * output place.
				 */
				preset = net.getInEdges(inputPlace);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> transferEdge : preset) {
					if (transferEdge instanceof Arc) {
						Arc transferArc = (Arc) transferEdge;
						MurataUtils.addArc(net, transferArc.getSource(), outputPlace, transferArc.getWeight());
					}
				}

				/*
				 * Also, transfer any output edge from the input place to the
				 * output place.
				 */
				postset = net.getOutEdges(inputPlace);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> transferEdge : postset) {
					if (transferEdge instanceof Arc) {
						Arc transferArc = (Arc) transferEdge;
						if (transferArc.getTarget() != transition) {
							MurataUtils.addArc(net, outputPlace, transferArc.getTarget(), transferArc.getWeight());
						}
					}
				}

				net.removeTransition(transition);
				net.removePlace(inputPlace);
			}
		}
		//the markings might have changed, so we need to re-index the final markings
		anet.setFinalMarkings(new THashSet<>(anet.getFinalMarkings()));

		return reduced;
	}
}
