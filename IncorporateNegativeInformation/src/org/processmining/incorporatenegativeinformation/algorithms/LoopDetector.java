package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.incorporatenegativeinformation.models.ReplayState;
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
 */
public class LoopDetector {
	static List<ReplayState> path;
	static List<Transition> blockTransitions;
	public static boolean isInLoop(Transition t, List<List<PetrinetNode>> loops) {
		
		for(List<PetrinetNode> loop: loops) {
			if(loop.contains(t))
				return true;
		}
		
		return false;
	} 
	
	public static List<List<PetrinetNode>> getLoopWithSilentTransitions(Petrinet net){
		// for each place in the Petri net, we check it 
		List<List<PetrinetNode>> sLoops = new ArrayList<List<PetrinetNode>>();
		// blockTransitions = new ArrayList<>();
		path = new ArrayList<ReplayState>();
		// for each silent transitions
		List<Transition> stSet = NetUtilities.getSilentTransitions(net);
		
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet;
		// we can remove the nodes from it and let repeat if again
		int i= 0;
		while(i< stSet.size()) {
			path.clear();
			Transition  st = stSet.get(i);
			// for each out edge from t test if it can go back, 
			// using only the traceBack strategy?? Not really
			// it differs in different edges out, so we can check the next place it connects
			Marking assumeMarking = new Marking();
			preEdgeSet = net.getInEdges(st);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
				Place place = (Place) edge.getSource();
				// if there is a cycle here then add it here// else pass it
				// for a small circle we can delete it there from the net and try not look at it
				assumeMarking.add(place);
				
				// the loop is related to some edges.. but let use first to detect a loop
			}
			if(!isInLoop(st, sLoops)) {
				
				List<PetrinetNode> loop = getLoop(net, st, assumeMarking);
				if(!loop.isEmpty()) {
					// it contains loop on those places, but if we delete this 
					sLoops.add(loop);
				}else {
					// remove the connection of this silent transitions when considering it 
					// blockTransitions.add(st);
				}
				// delete this transition from nodes and check next one, if next not done, 
				// then put it again
				// but how to know it is not done!! 
			}
			i++;
		}
		return sLoops;
	}
	
	// so here if we want to test a loop full of silent transitions,check all the silent transitions
	// and find out if they are connected with each others
	public static List<List<PetrinetNode>> getSilentLoops(Petrinet net){
		List<List<PetrinetNode>> sLoops = new ArrayList<List<PetrinetNode>>();
		
		// for one silent transition, if it can visit all silent transitions and back to itself
		// then we says it has a silent loop
		List<Transition> stList = NetUtilities.getSilentTransitions(net);
		
		for(int i=0; i< stList.size(); i++) {
			
			
		}
		
		
		return null;
	}
	

	private static List<PetrinetNode> getLoop(Petrinet net, Transition st, Marking fireMarking) {
		// TODO Auto-generated method stub
		// the assumeMarking is for the current st available
		List<PetrinetNode> loop = new ArrayList<>();
		Marking marking = new Marking(fireMarking);
		
		// fire this transitions
		fire(net, st, marking);
		
		// traceBack by DFS methods, how to write it here??
		boolean inLoop = true;
		for(Place p: fireMarking) {
			if(!traceBackByDFS(net, st, p, marking)) {
				inLoop = false;
				break;
			}
			
		}
		if(inLoop) {
			// we check path information and then put them into loop!! 
			// find the beginning place for the loop and end place for this loop.
			// get all the transitions for this path
			//Set<Transition> ltSet = getTransitionsInPath(); 
			// loop.addAll(ltSet);
			//List<Place> beginPlaces = new ArrayList<>();
			//List<Place> endPlaces = new ArrayList<>();
			//int beginIdx, endIdx;
			for(int i = 0; i< path.size();i++) {
				ReplayState state = path.get(i);
				// check place which is before this silent transitions
				// we can have a set 
				Marking firedMarking = state.getFiredMarking();
				Marking enabledMarking = new Marking(state.getMarking());
				// get the changed marking before it
				enabledMarking.removeAll(firedMarking);
				loop.addAll(enabledMarking);
				loop.add(state.getTransition());
				/*
				for(Place p: enabledMarking) {
					// if this place is loop begin or loop end
					
					/*
					if(isBeginLoopPlace(net, p, ltSet)) {
						// we begin to organize the loop from this place 
						beginPlaces.add(p);
						beginIdx = i;
					}
					if(isEndLoopPlace(net, p, ltSet)) {
						// but they can have many places as end
						endPlaces.add(p);
						endIdx = i;
					}
					
				}
				*/
			}
			
			// organize the loop in list but might not in order but with all the nodes in it
			// do we need to mark the begin and end places..
			// from the beginIdx and then back to visit all transitions there... 
			
			
		}
		return loop;
	}
	
	private static Set<Transition> getTransitionsInPath() {
		// TODO Auto-generated method stub
		Set<Transition> ltSet = new HashSet<>();
		for (ReplayState state: path)
			ltSet.add(state.getTransition());
		
		// if there is a path between this pair 
		
		
		return ltSet;
	}

	private static boolean isBeginLoopPlace(Petrinet net, Place p,Set<Transition> ltSet) {
		// TODO 
		// loop begin place
		//    -- one of inEdge from loop
		//    -- one outEdge to the loop
		//    -- one of inEdge not in loop!! 
		
		// check the path situations, check the transitions in the loop or not, also the places
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(p);
		boolean itFromLoop = false;
		boolean itNotFromLoop = false;
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			// it is a place
			Transition t = (Transition) edge.getSource();
			if(ltSet.contains(t))
				itFromLoop = true;
			else
				itNotFromLoop = true;
			
			if(itFromLoop && itNotFromLoop)
				break;
		}
		
		if(itFromLoop && itNotFromLoop) {
			
			// check of the outEdge to the loop
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(p);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
				// it is a place
				Transition t = (Transition) edge.getTarget();
				if(ltSet.contains(t))
					return true;
			}
		}
		
		return false;
	}

	private static boolean isEndLoopPlace(Petrinet net, Place p, Set<Transition> ltSet) {
		// TODO 
		// loop end place
		//    -- one of inEdge from loop
		//    -- one outEdge to the loop
		//    -- one of outEdge not in loop!! 
		
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(p);
		boolean otFromLoop = false;
		boolean otNotFromLoop = false;
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
			// it is a place
			Transition t = (Transition) edge.getTarget();
			if(ltSet.contains(t))
				otFromLoop = true;
			else
				otNotFromLoop = true;
			
			if(otFromLoop && otNotFromLoop)
				break;
		}
		
		if(otFromLoop && otNotFromLoop) {
			
			// check of the outEdge to the loop
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(p);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: postEdgeSet) {
				// it is a place
				Transition t = (Transition) edge.getSource();
				if(ltSet.contains(t))
					return true;
			}
		}
		
		return false;
		
	}

	public static boolean traceBackByDFS(Petrinet net, Transition ct, Place p, Marking marking) {
		// if transition ct can be traced back by this marking, then return it 
		// but make the root it visited
		if(marking.contains(p))
			return true;
		
		// we can only use the silent transitions there
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(p);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tmpEdgeSet = null;
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			// it is a place
			Transition t = (Transition) edge.getSource();
			// not a silent transition
			
			if(t.isInvisible() ) { //&& !blockTransitions.contains(t)
				// check if there is a silent loop here, if it is then stops execute it  
				
				tmpEdgeSet = net.getInEdges(t);
				boolean traceBackOK = true;
				for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> tmpEdge : tmpEdgeSet) {
					Place place = (Place) tmpEdge.getSource();
					// this place is before silent transition t, when it can't be traced back
					// this silent transitions is not ok for this time
					// terrible is the silent loop.. Nana, how to record them and avoid them?
					if(!traceBackByDFS(net, ct, place, marking)) {
						traceBackOK = false;
						break;
					}
					
				}
				
				// else it can be traced back, have a state to check
				if(traceBackOK) {
					// for this silent transition, then we can fire it and put a token in p
					fire(net, t, marking);
					// we will stop at one option?? How about the others??
					// should we record them?? Yes, we shoudl record them or at least not repeat it
					return true;
				}
				
			}
		
		}
		
		return false;
	}
	
	
	private static  void fire(Petrinet net, Transition t, Marking marking) {
		// TODO Auto-generated method stub
		// consume token from marking and produce marking after it
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(t);
		ReplayState cState = new ReplayState(t, new Marking(marking));
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
		cState.setFiredMarking(marking);
		path.add(cState);
	}
	
}
