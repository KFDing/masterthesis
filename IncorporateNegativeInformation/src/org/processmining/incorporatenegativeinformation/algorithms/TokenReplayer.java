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

	List<List<PetrinetNode>> loops;
	List<List<PetrinetNode>> sLoops;
	
	List<List<ReplayState>> mLoops;
	List<List<ReplayState>> sfmLoops;
	
	public TokenReplayer(Petrinet net, Marking initMarking2, Marking finalMarking2, Map<XEventClass, List<Transition>> maps) {
		this.net = net;
		this.maps = maps;
		finalMarking = finalMarking2;
		initMarking = initMarking2;
		path = new ArrayList<>();
		
		LoopDetectorTarjan detector = new LoopDetectorTarjan(net, false);
		loops = detector.findAllSimpleCycles();
		mLoops = addMarking2Loops(loops);
		
		
		sLoops = detector.getSilentLoops(loops);
		// with places are listed here
		List<List<PetrinetNode>> sfLoops = detector.getFinalSilentLoops(sLoops, finalMarking);
		
		sfmLoops = addMarking2Loops(sfLoops);
		// after this we should get all the silent transitions loops
	}
	// given silent loops
		public List<List<ReplayState>> getFinalSilentLoops(List<List<ReplayState>> smLoops, Marking finalMarking) {
			List<List<ReplayState>> sfmLoops = new ArrayList<>();
	        // to make the loop goes, then the final marking must be in between of places
	        for(List<ReplayState> smLoop: smLoops) {
	        	if(isMarkingInLoop(finalMarking, smLoops) {
	        		sfmLoops.add(smLoop);
	        	}
	        	
	        }
	        return sfmLoops;
		}
		
	
	private List<List<ReplayState>> addMarking2Loops(List<List<PetrinetNode>> loopList) {
		
		List<List<ReplayState>> result = new ArrayList<>();
		
		for(List<PetrinetNode> loop: loopList) {
        	// add places into list just before the transitions to use it
			List<ReplayState> loopWithMarking = new ArrayList<>();
        	
        	PetrinetNode current;
        	for(int i=0; i< loop.size(); i++) {
        		current = loop.get(i);
        		
        		Marking assumeMarking = getAssumeMarking((Transition) current);
        		ReplayState state = new ReplayState((Transition) current, assumeMarking);
        		loopWithMarking.add(state);
        		
        	}
        	result.add(loopWithMarking);
		}
		return result;
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
				// check if the marking can go to the finalMarking by silent transitions
				if(isMarkingInLoop(marking, sfmLoops))
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
		
		for(Transition ct: ctList) {
			// get the place before it and then have all
			preEdgeSet = net.getInEdges(ct);
			Marking goalMarking = getAssumeMarking(ct);
			// after we do this, the marking should change during the way..so we need to infer it
			Marking cMarking = new Marking(marking);
			if(traceBackByDFS(ct, goalMarking, cMarking, idx)) {
				// fire ct and 
				fire(net, ct, cMarking, idx);
				// simple the DFS search, and no record
				reachByDFS(idx+1, cMarking);
			}	
		}
		
		return false;
	}
	
	// how to get the finalLoop ?? we have final marking... then we
	
	
	
	private boolean isMarkingInLoop(Marking marking, List<List<ReplayState>> mLoopList) {
		// TODO Auto-generated method stub
		for(List<ReplayState> mLoop: mLoopList) {
			// but it must be a proper places there, deal with situations that 
			// partial marking fit for a loop. It is good. So we can do it like this
			
			for(ReplayState state: mLoop) {
				Marking tmpMarking = state.getMarking();
				if(marking.containsAll(tmpMarking))
					return true;
			}
		}
		return false;
	}

	public Marking getAssumeMarking(Transition node) {
		// TODO Auto-generated method stub
		Marking marking = new Marking();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdgeSet = net.getInEdges(node);
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: preEdgeSet) {
			Place p = (Place) edge.getSource();
			marking.add(p);
		}
		return marking;
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

	/**
	 * We check if we can go from cmarking to goalMarking by only silent transitions:: 
	 *   the methods work in the following way:
	 *      -- if current marking already include it, then return true;
	 *      else, 
	 *         check all the silent transitions from the cmarking
	 *         
	 * @param ct  the corresponding transitions with current trace
	 * @param goalMarking : marking to fire ct
	 * @param cmarking : the current marking in the net
	 * @param idx : current index of trace
	 * @return
	 */
	public boolean traceBackByDFS(Transition ct, Marking goalMarking, Marking cmarking, int idx) {
		boolean traceBackOK = false;
		if(cmarking.contains(goalMarking))
			return true;
		// check if this place can be visited twice
		// when it is in a loop, then possible, else not again!! 
		// if we have visited cmarking before, now, we shouldn't visit it!!
		if(!isMarkingInLoop(cmarking, mLoops))
			return false;
		
		Set<Transition> etSet = getEnabledTransitions(net, cmarking);
		
		for(Transition t : etSet ) {
			
			if(t.isInvisible()) {
				// check if there is a silent loop here, if it is then stops execute it  
				if(isSilentInLoop(t))
					continue;
				
				// else we fire it and trace it back by using the traceBack methods
				fire(net, t, cmarking, idx);
				
				traceBackOK = traceBackOK || traceBackByDFS(ct, goalMarking, new Marking(cmarking), idx);
				
				// If we find one way, then we should return true, right?? 
				// we don't need to check it anymore
				if(traceBackOK) {
					return true;
				}
			}
		}
		
		if(traceBackOK) {
			return true;
		}else {
			// remove the state here until the current marking
			removeState(cmarking);
		}
		
		return traceBackOK;
	}
	
	private void removeState(Marking cmarking) {
		// TODO Auto-generated method stub
		int i = path.size() -1;
		while(i >= 0) {
			ReplayState state = path.get(i);
			if(!state.getMarking().equals(cmarking))
				i--;
		}
		
		// here we find the state leading to further execution. so we remove it too
		if(i>=0)
			path.remove(i);
		else {
			// we couldn't find this state,then it must be wrong at the first place
			System.out.println("Some exception happening");
		}
	}


	private boolean isSilentInLoop(Transition ct) {
		// give one token at this place p, and it can fire transition t, then we know it
		for(List<PetrinetNode> loop: sLoops) {
			if(loop.contains(ct))
				return true;
		}
		return false;
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
