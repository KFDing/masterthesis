package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;


/**
 * Get all cycles in Petri net with Tarjan's algorithm
 * 
 * The cycles are different from normal ones, because it only records the transitions in a cycle 
 * and related places.
 * 
 * Reference - https://ecommons.cornell.edu/handle/1813/5941
 * https://github.com/jgrapht/jgrapht/tree/master/jgrapht-core/src/main/java/org/jgrapht/alg/cycle

 * @author dkf
 *
 */
public class LoopDetectorTarjan {
	private Set<PetrinetNode> visited;
    private Deque<PetrinetNode> pointStack;
    private Deque<Marking> markingStack;
    private Deque<PetrinetNode> markedStack;
    private Set<PetrinetNode> markedSet;
    Map<DirectedGraphElement, DirectedGraphElement> map, c2oMap;
    Petrinet net; 
    public LoopDetectorTarjan(Petrinet net, boolean onlySilent) {
    	if(onlySilent) {
    		this.net = getNetOnlyWithSilent(net);
    	}else
    		this.net = net;
    	
    	reset();
    }
    
    public Petrinet getNetOnlyWithSilent(Petrinet net) {
    	map = new HashMap<>();
    	
    	
    	Petrinet clonedNet = PetrinetFactory.clonePetrinet(net, map);
    	
    	// init the map to correspond from the clone to new copied.
    	
    	// Set<Transition> stSet = new HashSet<>();
		for(Transition t: net.getTransitions()) {
			if(!t.isInvisible()) {
				// only the transitions is removed..
				clonedNet.removeTransition((Transition) map.get(t));
			}
		}
		
		// get rid of the places has no edges
		for(Place place: net.getPlaces()) {
			// no way to delete the beginning and end places.. 
			if(net.getInEdges(place).isEmpty() || net.getOutEdges(place).isEmpty()) {
				clonedNet.removePlace((Place) map.get(place));
			}
		}
		// after this the remaining net should be ok
		c2oMap = new HashMap<>();
        for(DirectedGraphElement key: map.keySet()) {
        	DirectedGraphElement value = map.get(key);
        	c2oMap.put(value, key);
        }
		return clonedNet;
    }
 

	private void reset() {
    	if(visited!=null)
    		visited.clear();
    	else
    		visited = new HashSet<>();
    	if(pointStack!=null)
    		pointStack.clear();
    	else
    		pointStack = new LinkedList<>();
    	if(markedStack!=null)
    		markedStack.clear();
    	else
    		markedStack = new LinkedList<>();
    	
    	if(markedSet!=null)
    		markedSet.clear();
    	else
    		markedSet = new HashSet<>();
    	
    	if(markingStack!=null)
    		markingStack.clear();
    	else
    		markingStack = new LinkedList<>();
    	
    }
    
    public List<List<PetrinetNode>> findAllSimpleCycles() {
    	
        reset();
        
        // here we also prepare to use only silent transitions and change it
        List<List<PetrinetNode>> result = new ArrayList<>();
        // at first to check the length with self loop nodes and then with the length
        // then increase the length?? But how to find the problems here to test selfloop??
        // detect selfloop for transition
        /*
        for(PetrinetNode transition : net.getTransitions()) {
        	
        	Marking assumeMarking = getAssumeMarking(net, transition);
        	fire( (Transition) transition, assumeMarking);
        	
        	if(assumeMarking.equals(getAssumeMarking(net, transition))) {
        		List<PetrinetNode> cycle = new ArrayList<>();
        		cycle.add(transition);
        		cycle.add(transition);
        		visited.add(transition);
        		result.add(cycle);
        	}
        }
        */
        for(PetrinetNode transition : net.getTransitions()) {
        	
        	Marking assumeMarking = getAssumeMarking(net, transition);

    		markingStack.offerFirst(new Marking(assumeMarking));
            findAllSimpleCycles(transition, transition, assumeMarking, result);
            // one limit to the place to go?? We just want simple cycles
            // visited.addAll(assumeMarking);
            visited.add(transition);
            while(!markedStack.isEmpty()) {
                markedSet.remove(markedStack.pollFirst());
            }
            markingStack.clear();
            // pointStack.clear();
        }
        
        return result;
    }

    public List<List<PetrinetNode>>  addPlace2Loop(List<List<PetrinetNode>> result) {
    	List<List<PetrinetNode>> resultWithPlaces = new ArrayList<>();
        
        for(List<PetrinetNode> loop: result) {
        	// add places into list just before the transitions to use it
        	List<PetrinetNode> loopWithPlaces = new ArrayList<>();
        	
        	PetrinetNode current;
        	for(int i=0; i< loop.size(); i++) {
        		current = loop.get(i);
        		
        		// checked the assummedmarking for this current nodes
        		Marking assumeMarking = getAssumeMarking(net, current);
        		
        		for(Place p: assumeMarking) {
        			// if(!loopWithPlaces.contains(p))
        			if(c2oMap!=null)
        				loopWithPlaces.add((PetrinetNode) c2oMap.get(p));
        			else
        				loopWithPlaces.add(p);
        		}
        		if(c2oMap!=null)
        			loopWithPlaces.add((PetrinetNode) c2oMap.get(current));
        		else
        			loopWithPlaces.add(current);
        	}
        	// a little bit leak of memory, but it should be ok
        	resultWithPlaces.add(loopWithPlaces);
        }
        return resultWithPlaces;
    }
    
    private Marking getAssumeMarking(Petrinet net2, PetrinetNode node) {
		// TODO Auto-generated method stub
		Marking marking = new Marking();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(node);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place p = (Place) edge.getSource();
			marking.add(p);
		}
		return marking;
	}

	private boolean findAllSimpleCycles(PetrinetNode start, PetrinetNode current,  Marking marking,
			List<List<PetrinetNode>> result) {
		
		boolean hasCycle = false;
		// here we should also put the places into it, reduce visit it again
		// how to reduce it visit one node again... with the repeated nodes?
		// we need to remove them out!!
		// can't do it now, So Back writing and correct the thesis
		// if we found the marking is the same, then we go back and not repeat it
		
		pointStack.offerFirst(current);
		markedSet.add(current);
		markedStack.offerFirst(current);
		
		// get the available transitions next to current transition, only with its available marking
		fire( (Transition) current, marking);
		
		if(markingStack.contains(marking)) {
			// we have found a cycle here, we pop it up until we meet the original ones?? 
			// should we pop them up 
			// because we went deeper here, so we can quit 
			
			// markingStack.pollFirst();
			if(marking.equals(markingStack.peekLast())) {
				// it means the begin and end nodes should be the same, so we give a cycle here
				List<PetrinetNode> cycle = new ArrayList<>();
				Iterator<PetrinetNode> itr = pointStack.descendingIterator();
				while(itr.hasNext()) {
					cycle.add(itr.next());
				}
				cycle.add(start);
				result.add(cycle);
				hasCycle = true;
			}else {
				pointStack.pollFirst();
				return false;
			}
		}else
			markingStack.offerFirst(marking);
		
		if(!hasCycle) {
			
			Set<Transition> etSet = getEnabledTransitions(net, marking);
			// and only here we push them into this part
			for(Transition t: etSet) {
				if(visited.contains(t) ) 
					continue;
				if(!markedSet.contains(t)) {
					hasCycle = findAllSimpleCycles(start, t, new Marking( marking), result) || hasCycle;
				}
				
			}
			// we only give peek out the marking when the whole etSet are executed..
			markingStack.pollFirst();
		}
				
		if(hasCycle) {
			// if we also put places into it, so here, we need to change it to sth else..
			while(!markedStack.peekFirst().equals(current)) {
				markedSet.remove(markedStack.pollFirst());
			}
			markedSet.remove(markedStack.pollFirst());
		}
		pointStack.pollFirst();
		return hasCycle;
	}

	private  Set<Transition> getEnabledTransitions(Petrinet net, Marking marking){
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = null;
		Set<Transition> enabledTransitions =  new HashSet<>();
		for(Place p : marking) {
			
			postEdgeSet = net.getOutEdges(p);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
				Transition t = (Transition) edge.getTarget();
				// here we check the tmp if enabled
				if(isEnabled(net, t, marking)) {
					enabledTransitions.add(t);
				}
			}
		}
		return enabledTransitions;
	}
	
	private  boolean isEnabled(Petrinet net, Transition t, Marking marking) {
		// TODO Auto-generated method stub
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place p = (Place) edge.getSource();
			if(!marking.contains(p)) 
				return false;
		}
		return true;
	}
	
	private  void fire(Transition t, Marking marking) {
		// TODO Auto-generated method stub
		// consume token from marking and produce marking after it
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(t);
		// ReplayState cState = new ReplayState(t, new Marking(marking));
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place prePlace = (Place) edge.getSource();
			marking.remove(prePlace);
		}
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
			Place postPlace = (Place) edge.getTarget();
			marking.add(postPlace);
		}
		
	}

	public List<List<PetrinetNode>> getSilentLoops(List<List<PetrinetNode>> loops) {
		// TODO get the silent loops without places
		List<List<PetrinetNode>> sLoops = new ArrayList<>();
        
        for(List<PetrinetNode> loop: loops) {
        	// add places into list just before the transitions to use it
        	boolean silent = true;
        	for(PetrinetNode node: loop) {
        		Transition transition = (Transition) node;
        		if(!transition.isInvisible()) {
        			silent = false;
        			break;
        		}
        	}
        	if(silent)
        		sLoops.add(loop);
        	
        }
		return sLoops;
	}
	
}
