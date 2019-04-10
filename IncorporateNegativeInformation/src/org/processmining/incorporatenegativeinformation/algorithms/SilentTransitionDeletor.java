package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

/**
 * this class is used to delete silent transition after the model generation in
 * order to make model better looking..
 * 
 * @author ding
 *
 */

@Plugin(name = "Delete Silent Transition in Seq", level = PluginLevel.Regular, returnLabels = {
		"Reduced Net" }, returnTypes = { Petrinet.class }, parameterLabels = { "Petri net" }, userAccessible = true)
public class SilentTransitionDeletor {

	// make a test on petri net with silent transitions
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Delete Silent Transtion", requiredParameterLabels = { 0 })
	public Petrinet deleteSilentTranstionPlugin(PluginContext context, Petrinet net) {
		return deleteForSeq(net);
	}

	/**
	 * this method is based the limits for one transition transition: only in
	 * sequence situation, we can deal with easier. p1-->t-->t2 if
	 * |Outedges(p1)| >= 2 and |Inedges(p2)| >=2, this silent transition can not
	 * deleted.. else, we can delete the silent transition,
	 * 
	 * // will it happen, after we delete one transition, it affect the others??
	 * // because the other situations are exclusive, then not affect.
	 * 
	 * 
	 * @param net
	 * @return
	 */
	public static Petrinet deleteForSeq(Petrinet onet) {
		// check all the silent transitions in net
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		Petrinet dnet = PetrinetFactory.clonePetrinet(onet, map);
		Collection<Transition> ts = onet.getTransitions();

		for (Transition t : ts) {
			if (t.isInvisible()) {
				// get the places source and target
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tInEdges = onet.getInEdges(t);
				if (tInEdges.size() > 1)
					continue;
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tOutEdges = onet
						.getOutEdges(t);
				if (tOutEdges.size() > 1)
					continue;

				// get source and target place
				Place source = null, target = null;
				for (PetrinetEdge inEdge : tInEdges) {
					source = (Place) inEdge.getSource();
				}

				for (PetrinetEdge outEdge : tOutEdges) {
					target = (Place) outEdge.getTarget();
				}
				// test the situations on the source and target place
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> pOutEdges = onet
						.getOutEdges(source);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> pInEdges = onet
						.getInEdges(target);
				if (pOutEdges.size() > 1 && pInEdges.size() > 1) {
					//keep this silent transition
					continue;
				} else {
					// delete this silent transition
					deleteSilentTransition(dnet, (Transition) map.get(t), (Place) map.get(source),
							(Place) map.get(target));
				}

			}

		}
		return dnet;
	}

	/**
	 * here we delete one silent transition tau from net steps are : find the
	 * places before and after the tau count the outedges and inedges for places
	 * keep the place with edges number > 1, delete the place with edges number
	 * < 2 to keep it simple, we can give more information: the place to delete
	 * :: but still the place before, because we need to pass the edges to them
	 * 
	 * @param net
	 * @param tau
	 * @param source
	 * @param target
	 * @return a net without this silent transition
	 */
	public static Petrinet deleteSilentTransition(Petrinet net, Transition tau, Place source, Place target) {
		// check the number of each source and target
		if(net.getInEdges(source).size()<1) {
			System.out.println("Tau as the start place can not be deleted");
			return net;
		}
		
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> pOutEdges = net.getOutEdges(source);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> pInEdges = net.getInEdges(target);
		// check if it also deltes the acrs with tau
		net.removeTransition(tau);
		if (pOutEdges.size() < 2) {
			// delete the source and keep the target place
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> sInEdges = net.getInEdges(source);
			// connect all the sInEdges to place target
			for (PetrinetEdge inEdge : sInEdges) {
				// if we want to change the sInEdges connection, we need to get all the upstream transitions
				// can not set the inEdge target directly..
				Transition sNode = (Transition) inEdge.getSource();
				net.addArc(sNode, target);

			}
			net.removePlace(source);

		} else if (pInEdges.size() < 2) {
			// delete the target and keep the source
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tOutEdges = net
					.getOutEdges(target);
			// connect all the sInEdges to place target
			for (PetrinetEdge outEdge : tOutEdges) {
				// if we want to change the sInEdges connection, we need to get all the upstream transitions
				// can not set the inEdge target directly..
				Transition tNode = (Transition) outEdge.getTarget();
				net.addArc(source, tNode);

			}
			net.removePlace(target);
		}

		return net;
	}

}
