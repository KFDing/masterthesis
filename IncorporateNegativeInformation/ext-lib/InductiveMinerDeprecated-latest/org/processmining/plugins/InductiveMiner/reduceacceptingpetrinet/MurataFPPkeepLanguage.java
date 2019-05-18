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
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.MurataUtils;

import gnu.trove.set.hash.THashSet;

/**
 * Reduce a Petri net using Murata's Fusion of Parallel Places rule. However,
 * preserve the language of the net, i.e. only remove invisible transitions.
 * 
 * Adapted from Eric Verbeek's code in the Murata package of ProM.
 * 
 * @author sander
 *
 */

public class MurataFPPkeepLanguage {

	public static boolean reduce(AcceptingPetriNet anet, Canceller canceller) {
		Petrinet net = anet.getNet();

		HashMap<Place, HashSet<Arc>> inputMap = new HashMap<Place, HashSet<Arc>>();
		HashMap<Place, HashSet<Arc>> outputMap = new HashMap<Place, HashSet<Arc>>();
		/*
		 * Iterate over all places. Build inputMap and outputMap if all incident
		 * edges regular.
		 */
		for (Place place : net.getPlaces()) {

			if (canceller.isCancelled()) {
				return false;
			}

			/*
			 * Get input edges. Should all be regular.
			 */
			HashSet<Arc> inputArcs = new HashSet<Arc>();
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net.getInEdges(place);
				boolean ok = true;
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
			HashSet<Arc> outputArcs = new HashSet<Arc>();
			{
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
						.getOutEdges(place);
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

			inputMap.put(place, inputArcs);
			outputMap.put(place, outputArcs);
		}
		/*
		 * Iterate over all places with only regular incident edges.
		 */
		for (Place place : inputMap.keySet()) {
			HashSet<Arc> inputArcs = inputMap.get(place);
			HashSet<Arc> outputArcs = outputMap.get(place);
			/*
			 * Checking for matching transitions.
			 */
			for (Place siblingPlace : inputMap.keySet()) {
				if (siblingPlace == place) {
					continue;
				}
				HashSet<Arc> siblingInputArcs = inputMap.get(siblingPlace);
				HashSet<Arc> siblingOutputArcs = outputMap.get(siblingPlace);
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

					/*
					 * Move tokens from siblingPlace to place in the initial and
					 * final markings
					 */
					{
						{
							int tokens = anet.getInitialMarking().occurrences(siblingPlace);
							anet.getInitialMarking().add(place, tokens);
							MurataUtils.updateLabel(place, anet.getInitialMarking());
							MurataUtils.resetPlace(anet.getInitialMarking(), siblingPlace);
						}

						for (Marking marking : anet.getFinalMarkings()) {
							int tokens = marking.occurrences(siblingPlace);
							marking.add(place, tokens);
							MurataUtils.updateLabel(place, marking);
							MurataUtils.resetPlace(marking, siblingPlace);
						}

						//the markings might have changed, so we need to re-index the final markings
						anet.setFinalMarkings(new THashSet<>(anet.getFinalMarkings()));
					}
					net.removePlace(siblingPlace);
					return true; // The sibling has been removed.
				}
			}
		}
		return false;
	}

}
