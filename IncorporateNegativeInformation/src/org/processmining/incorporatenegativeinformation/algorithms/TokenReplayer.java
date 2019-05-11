package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.incorporatenegativeinformation.models.ReplayState;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;


/**
 * Although it seems that I reinvent the wheels, but I do it by myself and improve my skills
 * This method is used to check the token-based conformance , but naive one only care
 * for the total fitting part. There are codes but not in java.. Or I tried to find them
 * but not worked out.
 * 
 * It is based on the DFS in graph
 * @author dkf
 *
 */
public class TokenReplayer {
	Marking initMarking;
	Marking finalMarking;
	Petrinet net;
	Map<XEventClass, List<Transition>> maps;
	List<XEventClass> trace; 
	List<ReplayState> path;
	
	List<List<PetrinetNode>> sLoops;
	
	public TokenReplayer(Petrinet net, Marking initMarking2, Marking finalMarking2, Map<XEventClass, List<Transition>> maps) {
		this.net = net;
		this.maps = maps;
		finalMarking = finalMarking2;
		initMarking = initMarking2;
		path = new ArrayList<>();
		
		LoopDetectorTarjan detector = new LoopDetectorTarjan(net, true);
		sLoops = detector.findAllSimpleCycles();
	}
	
	public void setTrace(List<XEventClass> trace) {
		this.trace = trace;
	}

	public void setFinalMarking(Marking finalMarking) {
		this.finalMarking = finalMarking;
	}

	public void setNet(Petrinet net) {
		this.net = net;
	}

	public void setMaps(Map<XEventClass, List<Transition>> maps) {
		this.maps = maps;
	}

	
	
	public boolean traceFit( List<XEventClass> trace) {
		setTrace(trace);
		// sLoops = LoopDetector.getLoopWithSilentTransitions(net);
		// we do sth here about the initMarking??
		if(reachByDFS(0, new Marking(initMarking)))
			return true;
		return false;
	}
	
	public boolean reachByDFS(int idx, Marking marking) {
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet;
		
		if(idx >= trace.size()) {
			// demand to check if the marking is equal to current marking
			if(marking.equals(finalMarking))
				return true;
			else {
				// check if the marking can go to the finalMarking
				// from one marking to another marking only with silent transitions
				for(Place p: finalMarking) {
					if(!traceBackByDFS(null, p, marking, idx))
						return false;
				}
				
				return true;
			}
			
		}
		
		
		// get list of transitions ready to fire
		XEventClass eventClass  = trace.get(idx);
		// corresponding transitions for event class in log, should we record the visited ones?
		List<Transition> ctList = maps.get(eventClass);
		// enabledTransition under current marking
		Set<Transition> etSet = getEnabledTransitions(net, marking);
		// get the transitions should be fired now
		Set<Transition> t2fSet = new HashSet<>(ctList);
		t2fSet.retainAll(etSet);
		// the thing is how to go deeper down?? to another around?
		
		for(Transition t: t2fSet) {
			// fire t and check the state later
			// but here we change the marking... which should be reserved for its value
			fire(net, t, marking, idx);
			
			if(reachByDFS(idx + 1, marking)) {
				return true;
			}else {
				// here we need to recover tha path:: for what we don't use it 
				removeState(t, idx);
			}
		}
		// the situation trace back can be silent transition before?? we need to check it
		// 
		
		for(Transition ct: ctList) {
			// get the place before it and then have all
			preEdgeSet = net.getInEdges(ct);
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
				Place place = (Place) edge.getSource();
				if(traceBackByDFS(ct, place, marking, idx)) {
					// fire ct and 
					fire(net, ct, marking, idx);
					
					reachByDFS(idx+1, marking);
				}	
			}
		}
		
		return false;
	}
	
	
	private void removeState(Transition t, int idx) {
		// this remove the transition because it is not allowed by now until this transition
		// List<Integer> stateIdx = new ArrayList<>();
		int i = path.size() -1;
		while(i >= 0) {
			ReplayState state = path.get(i);
			if(state.getIndex() >=idx && !state.getTransition().equals(t)) {
				path.remove(i);
				i--;
			}else
				break;
		}
		
		// here we find the state leading to further execution. so we remove it too
		if(i>=0)
			path.remove(i);
		else {
			// we couldn't find this state,then it must be wrong at the first place
			System.out.println("Some exception happening");
		}
		
	}

	// this is used to trace back from one silent transitions then back to current marking 
	// check if it is enabled now.. 
	// a place directly after one silent transition
	public boolean traceBackByDFS(Transition ct, Place p, Marking marking, int idx) {
		if(marking.contains(p))
			return true;
		
		// to check if this place is possible after ct?? how to check it while ct is waiting for execution
		// how to check silent transitions are in a loop... 
		
		// if this place is visited before, return false;; but if it in a loop?? How to test this??
		// in normal situation, when there is a place with token before a transition, which enables this transition
		// but it chooses another one, then we will not trace back and return false;
		// --- any of  places after the transition is in path, it means that it passed this, and can not be back
		List<Integer> stateIdx = findPlaceInPath(p);
		boolean needVisit = false;
		if(stateIdx.size() >0) {
			for(int i: stateIdx) {
				// if it is not a loop
				if(path.get(i).getTransition().equals(ct)) {
					needVisit = true;
					break;
				}
			}
			
			if(!needVisit)
				return false;
		}
		// one transitions can be repeated visited, because it is in a loop; 
		// else it can't go back, and be marked as visited
		
		// but in the loop situation, if we can prove they are the same transitions, then fine
		
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(p);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tmpEdgeSet = null;
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			// it is a place
			Transition t = (Transition) edge.getSource();
			// not a silent transition
			
			if(t.isInvisible()) {
				// check if there is a silent loop here, if it is then stops execute it  
				if(isSilentInLoop(t))
					continue;
				
				tmpEdgeSet = net.getInEdges(t);
				boolean traceBackOK = true;
				for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> tmpEdge : tmpEdgeSet) {
					Place place = (Place) tmpEdge.getSource();
					// this place is before silent transition t, when it can't be traced back
					// this silent transitions is not ok for this time
					// terrible is the silent loop.. Nana, how to record them and avoid them?
					if(!traceBackByDFS(ct, place, marking, idx)) {
						traceBackOK = false;
						break;
					}
					
				}
				
				// else it can be traced back, have a state to check
				if(traceBackOK) {
					// for this silent transition, then we can fire it and put a token in p
					fire(net, t, marking, idx);
					// we will stop at one option?? How about the others??
					// should we record them?? Yes, we shoudl record them or at least not repeat it
					return true;
				}
				
			}
		}
		// after this, we check what??
		
		return false;
	}
	
	private boolean isSilentInLoop(Transition ct) {
		// give one token at this place p, and it can fire transition t, then we know it
		for(List<PetrinetNode> loop: sLoops) {
			if(loop.contains(ct))
				return true;
		}
		return false;
	}

	private List<Integer> findPlaceInPath(Place p) {
		// TODO check from backforward, if p is in the marking of path, we output true
		List<Integer> stateIdx = new ArrayList<>();
		int i = path.size() -1;
		while(i >= 0) {
			ReplayState state = path.get(i);
			if(state.getMarking().contains(p)) {
				// we need information of ct..
				Marking firedMarking = state.getFiredMarking();
				if(!firedMarking.contains(p)) {
					// the token in p is consumed and not used by this transition
					stateIdx.add(i);
					
				}
				
			}
			i--;
		}
		return stateIdx;
	}

	// here is also DFS search, but backwards to the marking of current way
	private Set<Transition> getSilentTransitions(Petrinet net, Transition ct) {
		// TODO get the silent transitions directly before transitions, also check if it is fine to 
		// reach from the marking
		Set<Transition> stSet = new HashSet<>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(ct);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> tmpEdgeSet = null;
		
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			// it is a place
			PetrinetNode node = edge.getSource();
			tmpEdgeSet = net.getInEdges(node);
			
		}
		
		
		return null;
	}
	private  void fire(Petrinet net, Transition t, Marking marking, int tIdx) {
		// TODO Auto-generated method stub
		// consume token from marking and produce marking after it
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(t);
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdgeSet = net.getOutEdges(t);
		ReplayState cState = new ReplayState(t, new Marking(marking), tIdx);
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

}
