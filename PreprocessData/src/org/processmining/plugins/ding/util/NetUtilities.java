package org.processmining.plugins.ding.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
public class NetUtilities {
	
	
	public static Place getStartPlace(Petrinet net) {
		// first we get all the places if one place has no preset edges
		// then it is the startPlace
		Collection<Place> places = net.getPlaces();
		Place p, startp = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
		Iterator<Place> pIterator = places.iterator();
		while(pIterator.hasNext()) {
			p = pIterator.next();
			preset = net.getInEdges(p);
			if(preset.size() < 1) {
				startp =  p;
			}
		}
		// if there is no start position, then we create one
		if(startp == null) {
			System.out.println("There is no Start Place and create start place");
			// and also the Arc to it 
			// Place pstart = net.addPlace("Start");
		}
		return startp;
	}
	
	public static Place getEndPlace(Petrinet net) {
		// firstly to get all places, if one place has no postset edges, then
		// it is the endPlace
		Collection<Place> places = net.getPlaces();
		Place p, endp = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		Iterator<Place> pIterator = places.iterator();
		while(pIterator.hasNext()) {
			p = pIterator.next();
			postset = net.getOutEdges(p);
			if(postset.size() < 1) {
				endp = p;
			}
		}
		if(endp == null) {
			System.out.println("There is no End Place and create end place");
			// and also the Arc to it 
		}
		return endp;
	}
	
	public static Collection<PetrinetNode> findSilentNodes(Place p,Petrinet net) {
		// how to find the previus transition of one place
		Collection<PetrinetNode>  result = new HashSet<>();
		
		Collection<Transition> nodes = net.getTransitions();
		Iterator piter = nodes.iterator();
		while(piter.hasNext()) {
			Transition node = (Transition) piter.next();
			if(node.isInvisible() && net.getArc(node, p) != null ) {
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
            PetrinetNode cn = (PetrinetNode)iter.next();
            PetrinetNode n = (PetrinetNode) NetUtilities.mapNet(cnet,net).get(cn);
            AttributeMap map =  n.getAttributeMap();
            for (String key : map.keySet()) {
    			cn.getAttributeMap().put(key, map.get(key));
    		}
        }
		return cnet;
	}
	
	public static Map mapNet(Petrinet fnet, Petrinet tnet) {
		Map<PetrinetNode, PetrinetNode> nodeMap = new HashMap<PetrinetNode, PetrinetNode>();
		Iterator<PetrinetNode> fIterator = fnet.getNodes().iterator();
		
		while(fIterator.hasNext()) {
			PetrinetNode fNode = fIterator.next();
			Iterator<PetrinetNode> tIterator = tnet.getNodes().iterator();
			PetrinetNode tNode = tIterator.next();
			while(fNode.getLabel() != tNode.getLabel()) {
				tNode = tIterator.next();
			}

			nodeMap.put(fNode, tNode);
		}
		
		return nodeMap;
	}
	
	public static void initPNToken(Petrinet net) {
		for(Place place : net.getPlaces()) {
			place.getAttributeMap().put(Configuration.TOKEN, 0);
		}
	}
	
	public static boolean fitPN(Petrinet net, Marking marking, List<XEventClass> trace, Map<XEventClass, Transition> maps) {
		
		initPNToken(net);
    	Transition transition = null;
    	Arc arc = null;
    	// Place place = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
    	// set a token at first place... Na, we need to check it from another code
    	// this doesn't include the token stuff. Maybe we should include it.
    	Place splace = NetUtilities.getStartPlace(net);
    	splace.getAttributeMap().put(Configuration.TOKEN, 1);
    	// boolean fit = true;
		// first transition if it connects the initial place
    	for(XEventClass eventClass : trace) {
			// we need to create the map from the Petrinet to the event log.
    		
			transition = maps.get(eventClass);
			
			preset = net.getInEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getSource(); 
				
				int tnum = (Integer)p.getAttributeMap().get(Configuration.TOKEN);
				if(tnum == 1) {
					p.getAttributeMap().put(Configuration.TOKEN, tnum-1);
				}else {
				// for each transition, check the preset places of it the tokens number is greater than one?? 
					if(isTokenMissing(p,net))
						return false;
					
				}
			}
			
			// we need to generate the token for the next places
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getTarget();
				p.getAttributeMap().put(Configuration.TOKEN, 1);
			}
			
		}
    	Place eplace = NetUtilities.getEndPlace(net);
    	
    	int tnum = (Integer)eplace.getAttributeMap().get(Configuration.TOKEN);
        if(tnum!=1) 
        	return false;
		// if not we see it's not fit, return false
		return true;
	}
	// if not token missing, so I could trace back and generate token before and consume later by this place.
	public static boolean isTokenMissing(Place p, Petrinet net) {
		
    	Arc arc= null;
		// if trace back to the silent transition.
		Collection<PetrinetNode> silentNodes = findSilentNodes(p, net);
		if(silentNodes.size() == 0) {
			return true;
		}else {
			Iterator<PetrinetNode> niter = silentNodes.iterator();
			
			while(niter.hasNext()) {
				// find the path until the place with token or, without silent transition
				PetrinetNode node = niter.next();
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> spreset = net.getInEdges(node);
				// we need to see two transitions together???  
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> sedge : spreset) {
					arc = (Arc) sedge;
					// get the prior place for transition
					Place sp= (Place) arc.getSource();
					
					int tnum = (Integer)sp.getAttributeMap().get(Configuration.TOKEN);
					
					if(tnum == 0 ) {
						// go back to check the place before and see if works
						return isTokenMissing(sp, net); 
					}
					
					if(tnum == 1 ) {
						sp.getAttributeMap().put(Configuration.TOKEN, tnum -1);
						// after we consume one token, we need to generate token..
						// now it is the node, we get the postset for this transition 
						
						Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> spostset = net.getOutEdges(node);
						// we need to see two transitions together???  
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : spostset) {
							arc = (Arc) edge;
							// get the prior place for transition
							Place place= (Place) arc.getTarget();
							place.getAttributeMap().put(Configuration.TOKEN, 1);
						}
					}
				}
			}
			
		}
		// after we traverse all the silent transition, still no answer it mean it is false
		p.getAttributeMap().put(Configuration.TOKEN, (int)p.getAttributeMap().get(Configuration.TOKEN)-1);
		return false;
	}

	/**
	 * check if they fit and add fit or not fit label to log file
	 * @param log
	 * @param net
	 */
	public static void checkAndAssignLabel(XLog log, Petrinet net) {
		
	}
}
