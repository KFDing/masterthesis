package org.processmining.incorporatenegativeinformation.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class NetUtilities {

	public static Marking guessInitialMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> place = getStartPlace(net);
		Marking iniMarking = new Marking();
		iniMarking.addAll(place);
		return iniMarking;
	}

	public static Set<Marking> guessFinalMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> placeList = getEndPlace(net);
		Set<Marking> finalSet = new HashSet<>();
		for(Place p: placeList) {
			Marking finalMarking = new Marking();
			finalMarking.add(p);
			finalSet.add(finalMarking);
		}
		return finalSet;
	}

	public static List<Place> getStartPlace(Petrinet net) {
		// first we get all the places if one place has no preset edges
		// then it is the startPlace
		Collection<Place> places = net.getPlaces();
		Place p = null;
		List<Place> startp = new ArrayList<>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
		Iterator<Place> pIterator = places.iterator();
		while (pIterator.hasNext()) {
			p = pIterator.next();
			preset = net.getInEdges(p);
			if (preset.size() < 1) {
				startp.add(p);
			}
		}
		// if there is no start position, then we create one
		if (startp.isEmpty()) {
			System.out.println("There is no Start Place and create start place");
			// and also the Arc to it 
			// Place pstart = net.addPlace("Start");
		}
		return startp;
	}

	public static List<Place> getEndPlace(Petrinet net) {
		// firstly to get all places, if one place has no postset edges, then
		// it is the endPlace
		Collection<Place> places = net.getPlaces();
		Place p;
		List<Place> endp = new ArrayList<>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		Iterator<Place> pIterator = places.iterator();
		while (pIterator.hasNext()) {
			p = pIterator.next();
			postset = net.getOutEdges(p);
			if (postset.size() < 1) {
				endp.add(p);
			}
		}
		if (endp.isEmpty()) {
			System.out.println("There is no End Place and create end place");
			// and also the Arc to it 
		}
		return endp;
	}

	public static Collection<PetrinetNode> findSilentNodes(Place p, Petrinet net) {
		// how to find the previus transition of one place
		Collection<PetrinetNode> result = new HashSet<>();

		Collection<Transition> nodes = net.getTransitions();
		Iterator piter = nodes.iterator();
		while (piter.hasNext()) {
			Transition node = (Transition) piter.next();
			if (node.isInvisible() && net.getArc(node, p) != null) {
				// how to set the Label for silent transiton in Petri net
				result.add(node);
			}
		}

		return result;
	}

	public static Petrinet clone(Petrinet net) {
		Petrinet cnet = PetrinetFactory.clonePetrinet(net);
		Collection<PetrinetNode> nodes = cnet.getNodes();
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			// pair<Arc, count>
			PetrinetNode cn = (PetrinetNode) iter.next();
			PetrinetNode n = (PetrinetNode) NetUtilities.mapNet(cnet, net).get(cn);
			AttributeMap map = n.getAttributeMap();
			for (String key : map.keySet()) {
				cn.getAttributeMap().put(key, map.get(key));
			}
		}
		return cnet;
	}

	public static Map<Transition, XEventClass> getTransition2EventMap(XLog log, Petrinet net,
			XEventClassifier classifier) {
		// too complex, so now, I will just change back original ones.
		Map<Transition, XEventClass> map = new HashMap<Transition, XEventClass>();
		Collection<Transition> transitions = net.getTransitions();
		XEventClasses classes = null;

		if (classifier != null && log.getClassifiers().contains(classifier))
			classes = XLogInfoFactory.createLogInfo(log).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(log).getNameClasses();

		XEventClass tauClassinLog = new XEventClass(Configuration.Tau_CLASS, classes.size());

		boolean match;
		for (Transition transition : transitions) {
			match = false;
			for (XEventClass eventClass : classes.getClasses()) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs 
				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(transition, eventClass);
					match = true;
					break;
				}
			}
			if (!match) {
				map.put(transition, tauClassinLog);
			}
		}
		// three cases: silent transition
		// in net but not shown in event, how to match them??? Then return null
		// in event log but not in net  // return null .
		return map;
	}

	public static Map mapNet(Petrinet fnet, Petrinet tnet) {
		Map<PetrinetNode, PetrinetNode> nodeMap = new HashMap<PetrinetNode, PetrinetNode>();
		Iterator<PetrinetNode> fIterator = fnet.getNodes().iterator();

		while (fIterator.hasNext()) {
			PetrinetNode fNode = fIterator.next();
			Iterator<PetrinetNode> tIterator = tnet.getNodes().iterator();
			PetrinetNode tNode = tIterator.next();
			while (fNode.getLabel() != tNode.getLabel()) {
				tNode = tIterator.next();
			}

			nodeMap.put(fNode, tNode);
		}

		return nodeMap;
	}

	public static void initPNToken(Petrinet net) {
		for (Place place : net.getPlaces()) {
			place.getAttributeMap().put(Configuration.TOKEN, 0);
		}
	}

}
