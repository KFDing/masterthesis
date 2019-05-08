package org.processmining.incorporatenegativeinformation.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.incorporatenegativeinformation.models.ReplayState;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class NetUtilities {

	public static Marking guessInitialMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> place = getStartPlace(net);
		Marking iniMarking = new Marking();
		iniMarking.addAll(place);
		return iniMarking;
	}

	public static Marking guessFinalMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> place = getEndPlace(net);
		Marking finalMarking = new Marking();
		finalMarking.addAll(place);
		return finalMarking;
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

	private static boolean isTokenRemaining(Petrinet net) {
		for (Place place : net.getPlaces()) {
			int tnum = (Integer) place.getAttributeMap().get(Configuration.TOKEN);
			if (tnum > 0)
				return true;
		}
		return false;
	}
	/**
	 * to deal with duplicated events and silent transitions here, but here is based on model
	 * @param net
	 * @param marking
	 * @param trace
	 * @param maps
	 * @return
	 */
	public static boolean tokenReplayer(Petrinet net, Marking initMarking, List<XEventClass> trace,
			Map<XEventClass, Transition> maps) {
		// create basic data structure to use
		// store the enabled transitions
		Set<Transition> enabledTransitions =  new HashSet<>();
		// index for the current visited trace
		int tIdx = 0;
		XEventClass event2fire;
		// another marking to express the current situation which do not change initial ones
		Marking marking = new Marking(initMarking);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = null;
		Stack<ReplayState> stack = new Stack<>();
		
		// get the enabled Transitions set
		for(Place p : marking) {
			
			postEdgeSet = net.getOutEdges(p);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
				Transition t = (Transition) edge.getSource();
				// here we check the tmp if enabled
				if(isEnabled(net, t, marking)) {
					enabledTransitions.add(t);
				}
			}
		}
		
		// fire one of them but according to the current event 
		event2fire = trace.get(tIdx);
		// get the list of transitions with potential to be fired
		// and we use a stack structure to store the execution path on it but also with the current marking
		 
		
		for(Transition t: enabledTransitions) {
			if(match(event2fire, t)) {
				Marking dMarking = new Marking(marking);
				ReplayState dState = new ReplayState(t, dMarking, tIdx);
				stack.push(dState);
			}
		}
		// check if there is an silent transitions?? but we should always make it match
		if(stack.size() < 1) {
			return false;
		}
			
		// visit each of those stack transitions to use and record marking changes 
		ReplayState cState = stack.pop();
		Transition ct = cState.getTransition();
		tIdx = cState.getIndex();
		marking = cState.getMarking();
		// consume the marking before this transition and produce marking into it 
		
		fire(net, ct, marking);
		if(!ct.isInvisible()) {
			tIdx++;
			// however, we also need to store the visited trace information at current state
			// so create a class for it
		}
		// after firing, we have a new marking and then return it back to find the positions.
		// until we have reached the final marking place...
		// but we need to make sure the silent transitions
		return false;
	}
	
	private static void fire(Petrinet net, Transition t, Marking marking) {
		// TODO Auto-generated method stub
		// consume token from marking and produce marking after it
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(t);
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place prePlace = (Place) edge.getSource();
			marking.remove(prePlace);
		}
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
			Place postPlace = (Place) edge.getSource();
			marking.add(postPlace);
		}
	}

	private static boolean match(XEventClass event, Transition t) {
		// TODO Auto-generated method stub
		if(t.isInvisible() || event.getId().equals(t.getLabel()))
			return true;
		return false;
	}

	private static boolean isEnabled(Petrinet net, Transition t, Marking marking) {
		// TODO Auto-generated method stub
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place p = (Place) edge.getSource();
			if(!marking.contains(p)) 
				return false;
		}
		return true;
	}

	public static boolean fitPN(Petrinet net, Marking marking, List<XEventClass> trace,
			Map<XEventClass, Transition> maps) {

		initPNToken(net);
		Iterator<Place> titer = marking.iterator();
		while (titer.hasNext()) {
			Place splace = titer.next();
			splace.getAttributeMap().put(Configuration.TOKEN, 1);
		}

		Transition transition = null;
		Arc arc = null;
		// Place place = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		// set a token at first place... Na, we need to check it from another code
		// this doesn't include the token stuff. Maybe we should include it.
		/*
		if (marking == null || marking.size() < 1) {
			Place splace = NetUtilities.getStartPlace(net).get(0);
			splace.getAttributeMap().put(Configuration.TOKEN, 1);
		} else {
			// we use initial marking and get the places. 
			Iterator<Place> titer = marking.iterator();
			while (titer.hasNext()) {
				Place splace = titer.next();
				splace.getAttributeMap().put(Configuration.TOKEN, 1);
			}
		}
		*/
		// boolean fit = true;
		// first transition if it connects the initial place
		for (XEventClass eventClass : trace) {
			// we need to create the map from the Petrinet to the event log.
			if (maps.containsKey(eventClass))
				transition = maps.get(eventClass);
			else
				return false;
			// for repeated data, how to get the naive ways??
			preset = net.getInEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p = (Place) arc.getSource();

				int tnum = (Integer) p.getAttributeMap().get(Configuration.TOKEN);
				if (tnum == 1) {
					p.getAttributeMap().put(Configuration.TOKEN, tnum - 1);
				} else {
					// for each transition, check the preset places of it the tokens number is greater than one?? 
					if (isTokenMissing(p, net, 0))
						return false;
					else {
						p.getAttributeMap().put(Configuration.TOKEN,
								(int) p.getAttributeMap().get(Configuration.TOKEN) - 1);
					}
				}
			}

			// we need to generate the token for the next places
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p = (Place) arc.getTarget();
				p.getAttributeMap().put(Configuration.TOKEN, 1);
			}

		}
		Place eplace = NetUtilities.getEndPlace(net).get(0);

		int tnum = (Integer) eplace.getAttributeMap().get(Configuration.TOKEN);
		if (tnum == 1) {
			eplace.getAttributeMap().put(Configuration.TOKEN, tnum - 1);

		} else {
			// for each transition, check the preset places of it the tokens number is greater than one??
			// to have the naive evaluation checking, hope it is efficient

			if (isTokenMissing(eplace, net, 0))
				return false;
			else {
				eplace.getAttributeMap().put(Configuration.TOKEN,
						(int) eplace.getAttributeMap().get(Configuration.TOKEN) - 1);
			}

		}
		// actually after this, we need to check if there is another place with token in the graph, 
		// if there is such a place, then the trace is not fit.
		if (isTokenRemaining(net))
			return false;

		return true;
	}

	// if not token missing, so I could trace back and generate token before and consume later by this place.
	public static boolean isTokenMissing(Place p, Petrinet net, int loop_count) {

		boolean stopCheck = false;
		Arc arc = null;
		// if trace back to the silent transition before place p.
		Collection<PetrinetNode> silentNodes = findSilentNodes(p, net);
		if (silentNodes.size() == 0) {
			stopCheck = true;
			return true;
		} else {
			Iterator<PetrinetNode> niter = silentNodes.iterator();

			while (!stopCheck && niter.hasNext()) {
				// find the path until the place with token or, without silent transition
				PetrinetNode node = niter.next();
				// transition, to trigger one transition, it demands all the spreset places with tokens!! 
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> spreset = net.getInEdges(node);
				int triggerNum = 0;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> sedge : spreset) {
					arc = (Arc) sedge;
					// get the prior place for transition
					Place sp = (Place) arc.getSource();

					int tnum = (Integer) sp.getAttributeMap().get(Configuration.TOKEN);
					// if one of place in preset has token, then continue until all
					if (tnum == 0) {
						// go depth search for loop
						if (loop_count <= net.getTransitions().size()) {
							// if we find out token missing, then we need to change to another branch
							if (isTokenMissing(sp, net, loop_count + 1))
								break;
							else
								tnum = (Integer) sp.getAttributeMap().get(Configuration.TOKEN);
						} else {
							// if we check all the loops, decide to change to another branch
							break;
						}
					}

					if (tnum >= 1) {
						// directly consume the token?? or wait until next loop to get it ??
						sp.getAttributeMap().put(Configuration.TOKEN, tnum - 1);
						triggerNum++;
					}
				}

				// after we consume one token, we need to generate token..
				// now it is the node, we get the postset for this transition, but before we must make sure
				// that all inedges have token, else we can't do it.
				if (triggerNum == spreset.size()) {
					Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> spostset = net
							.getOutEdges(node);
					// we need to see two transitions together???  
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : spostset) {
						arc = (Arc) edge;
						// get the prior place for transition
						Place place = (Place) arc.getTarget();
						place.getAttributeMap().put(Configuration.TOKEN, 1);
					}
					return false;
				}

			}
			// after we check all the branches, and we couldn't find the corresponding ones 
			return true;
		}
	}

	static int tID = 0;
	public static Map<Node, Transition> getProcessTree2NetMap(Petrinet net, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<Node, Transition> map = new HashMap<Node, Transition>();
		Collection<Node> nodes = pTree.getNodes();
		Collection<Transition> transitions = net.getTransitions();

		boolean match;
		for (Node node : nodes) {
			if (!node.isLeaf())
				continue;

			match = false;
			for (Transition transition : transitions) {
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if (node.getName().equals(transition.getLabel())) {
					map.put(node, transition);
					match = true;
					break;
				}
			}
			if (!match) {// it there is node not showing in the petri net, which we don't really agree
				// but one thing we need to make sure is that, we don't have currently the silent transition
				Transition tTransition = net.addTransition(node.getID().toString());
				// Transition tTransition = net.addTransition(ProcessConfiguration.TRANSITION_TAU_PREFIX + tID++ );
				map.put(node, tTransition);
			}
		}

		return map;
	}

}
