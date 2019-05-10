package org.processmining.incorporatenegativeinformation.models;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * this class is used to record the current state for token replay. It includes:
 *   -- transition to fire
 *   -- current visited event in trace
 *   -- current marking in net
 *   
 *  It provides methods:
 *   -- fireTransition : consume and produce tokens after fire one transition
 *     so it changes from one state to another state;
 *   -- check if the replay state is the final state;
 *   -- check if the final state is valid
 *    
 * @author ding
 *
 */
public class ReplayState {
	Petrinet net;
	// current marking
	Marking marking;
	Marking firedMarking;
	
	// current trace idx
	int tIdx;
	// current to fire transition
	Transition ct;
	public ReplayState() {}
	
	public ReplayState(Transition t, Marking dMarking, int idx) {
		// TODO Auto-generated constructor stub
		ct = t;
		marking = dMarking;
		tIdx = idx;
	}
	
	public ReplayState(Transition t, Marking marking2) {
		// TODO Auto-generated constructor stub
		ct = t;
		marking = marking2;
	}

	public void setNet(Petrinet pnet) {
		net = pnet;
	}

	public boolean isFinalStateFit(Marking finalMarking) {
		if(finalMarking.equals(marking))
			return true;
		
		return false;
	}

	public Transition getTransition() {
		// TODO Auto-generated method stub
		return ct;
	}

	public Marking getMarking() {
		// TODO Auto-generated method stub
		return marking;
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return tIdx;
	}

	public void setFiredMarking(Marking marking2) {
		// TODO Auto-generated method stub
		firedMarking = marking2;
	}
	
	public Marking getFiredMarking() {
		// TODO Auto-generated method stub
		return firedMarking;
	}
	
}
