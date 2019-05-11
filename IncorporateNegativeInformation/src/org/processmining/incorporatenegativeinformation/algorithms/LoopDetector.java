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

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
/**
 * this class serves TokenReplayer and give out the loops in Petri net
 * -- first detect loops with silent transitions
 * -- try to implement it, just do it
 * 
 * A circle or loop in Petrinet is, if the *beginning place* has a token, 
 * then this token can run back to this place again!! Else, not ok!! 
 * 
 * So, if I want to test loop on silent transitions, I need to ??
 * @author dkf
 * 
 * here we generalize the types for it
 * * References
 * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/cycle/JohnsonSimpleCycles.java

 */
public class LoopDetector{
	Set<PetrinetNode> blockedSet;
	Map<PetrinetNode, Set<PetrinetNode>> blockedMap;
	Deque<PetrinetNode> stack;
	List<List<PetrinetNode>> allCycles;
	Petrinet net;
	Map<PetrinetNode, Boolean> complete;
	Map<PetrinetNode, Boolean> noInLoop;
	
	public List<List<PetrinetNode>> simpleCycles(Petrinet net){
		
		this.net = net;
		blockedSet = new HashSet<>();
        blockedMap = new HashMap<>();
        stack = new LinkedList<>();
        allCycles = new ArrayList<>();
		
        int startIdx = 0;
        
        // but if we use this, then how can we get the silent transitions limits?? not really..
        // then we need to check the others from it for all the loops in graph
        Iterator<Transition> tIter = net.getTransitions().iterator();
        complete = new HashMap<>();
        noInLoop = new HashMap<>();
        // complete is used to record if one silent transitions use too sets, better
        // if one transitions is complete and noInLoop, so skip this transitions
        // if t inLoop and not complete, then check again
        // if t not in loop and not complete, check it
        // if t is complete, skip it, too.
        
        while(startIdx < net.getTransitions().size()) {
        	// how to get the current net node
        	PetrinetNode node = tIter.next();
        	// if a node is already in a loop and has no possibility to be in another loop
        	// then we should skip this test but how?? no possibility to be in another loop?? 
        	if(complete.get(node))
        		continue;
        	
        	blockedMap.clear();
        	blockedSet.clear();
        	Marking assumeMarking = getAssumeMarking(net, node);
        	findCyclesWithNode(node, node, assumeMarking);
        	startIdx ++;
        }
		return allCycles;
		
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

	// we use startNode as transitions for our use
	private boolean findCyclesWithNode(PetrinetNode startNode, PetrinetNode currentNode, Marking marking) {
		// TODO Auto-generated method stub
		boolean foundCycle = false;
		Marking initMarking = new Marking(marking);
		if(!isEnabled(net, (Transition) currentNode, marking))
			return false;
		
		// we need to fire currentNode
		fire(net, (Transition) currentNode, marking);
		stack.push(currentNode);
		blockedSet.add(currentNode);
		
		// ok, if we check the current marking, so it must check if it is enabled in the marking
		Set<Transition> etSet = getEnabledTransitions(net, marking);
		
		for(PetrinetNode node: etSet) {
			
			if(node.equals(startNode)) {
				List<PetrinetNode> cycle = new ArrayList<>();
				// how to we remember the places between them?? we can do it later
				// get the place; Because it can turn to another place
				stack.push(startNode);
				// cycle only for the transitions. Also for place, we do it late!! 
				cycle.addAll(stack);
				
				stack.pop();
				allCycles.add(cycle);
				
				foundCycle = true;
			}else if(! blockedSet.contains(node)) {
				boolean gotCycle =  findCyclesWithNode(startNode, node, marking);
				foundCycle = foundCycle || gotCycle;
				
			} 
			
		}
		
		if(foundCycle) {
			unblock(currentNode);
			// we can only say, not in this loop, but for others, how can say it??
			noInLoop.put(currentNode, false);
		}else {
			// if no cycle found, then add its neighbors and this node to their blockMap
			
			// etSet is available for this
			for(PetrinetNode node: etSet) {
				Set<PetrinetNode> bSet = getBSet(node);
				bSet.add(currentNode);
			}
			
			// if it blocks all its subset, then we say it is complete and not in loop
			
			
		}
		stack.pop();
		
		return foundCycle;
	} 
	
	private Set<PetrinetNode> getBSet(PetrinetNode node) {
		// TODO Auto-generated method stub
		
		return blockedMap.computeIfAbsent(node, (key)-> new HashSet<>());
	}

	private void unblock(PetrinetNode node) {
		// TODO Auto-generated method stub
		blockedSet.remove(node);
		if(blockedMap.get(node)!=null) {
				
			blockedMap.get(node).forEach( v -> {
				if(blockedSet.contains(v)) {
					unblock(v);
				}
			});
			blockedMap.remove(node);
		}
		
	}

	private  Set<Transition> getEnabledTransitions(Petrinet net, Transition ct){
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(ct);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tmpEdgeSet = null;
		
		Set<Transition> subTransitions =  new HashSet<>();
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> tmpEdge : postEdgeSet) {
			Place p = (Place) tmpEdge.getTarget();
			tmpEdgeSet = net.getOutEdges(p);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: tmpEdgeSet) {
				Transition t = (Transition) edge.getTarget();
				// here we check the tmp if enabled
				subTransitions.add(t);
			}
		}
		return subTransitions;
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

	private  void fire(Petrinet net, Transition t, Marking marking) {
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
		// return marking;
		// we need to record the visited path from this model;
		// else, if it repeats, it takes a lot of times!! The path has walked by the path;;
		// but what to remember?? transition, tIdx, and marking at that time?? 
		// cState.setFiredMarking(marking);
		
	}
}
