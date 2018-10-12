package org.processmining.plugins.ding.process.dfg.train;
/**
 * this class is used as post processing. To detect the global dependency in the petri net
 * it is introduced to test my idea.
 * -- list all the global dependency that IM can't find out
 * -- how to detect the structure which contrains non-free choices[there two words, I exchange to use it]
 * -- use the negative information to address it 
 * -- create some relations and add places on it
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;


public class DetectGlobalDependency {

	Petrinet net;
	XLog log;
	Map<Place, List<PetrinetNode>> xorJoinSet = new HashMap<Place, List<PetrinetNode>>();
	Map<Place, List<PetrinetNode>> xorSplitSet = new HashMap<Place, List<PetrinetNode>>();
	Map<XEventClass, Transition> maps;
	
	public void init(Petrinet net, XLog log) {
		this.net = net;
		this.log = log;
		maps = EventLogUtilities.getEventTransitionMap(log, net , null);
	}
	
	/**
	 * get the potential Non free choice structure of petri net
	 * @return
	 */
	public void setXORStructure(){
		
		// potential are at the XOR split and XOR join, so we need to take care of them
		// but how to combine them together, I don't know..
		// maybe we can trace back actually from the transition system, they have the same input set
		// but goes specific set. But maybe better to do it on Petrinet 
		// -- we get the nodes before the xor-join and after the xor-split. 
		Collection<Place> places = net.getPlaces();
		Collection<Transition> transitions = net.getTransitions();
		
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preEdges;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postEdges;
		for(Place p: places) {
			preEdges = net.getInEdges(p);
			// xor join
			if(preEdges.size()>1) {
				List<PetrinetNode> nodes = new ArrayList<PetrinetNode>();
				
				for(PetrinetEdge edge: preEdges) {
					PetrinetNode node = (PetrinetNode) edge.getSource();
					nodes.add(node);
				}
				xorJoinSet.put(p, nodes);
			}
			
			postEdges = net.getOutEdges(p);
			// xor split
			if(postEdges.size()>1) {
				List<PetrinetNode> nodes = new ArrayList<PetrinetNode>();
				
				for(PetrinetEdge edge: postEdges) {
					PetrinetNode node = (PetrinetNode) edge.getTarget();
					nodes.add(node);
				}
				xorSplitSet.put(p, nodes);
			}
		}
		
	}
	/**
	 * after we get the xorJoinNodes and xorSplitNodes. we then create the potential structure of it. 
	 * we combine them together to create the potential NFC Structure 
	 * @return
	 */
	public Map<List<PetrinetNode>, List<Integer>> getPotentialNFCStructure(Place pXORJoin, Place pXORSplit){
		// after we have different combination, and then after this, we just to test them
		// one part is from the place and another part is from another place, so we just read from the Map and test each of them
		List<PetrinetNode> joinNodes = xorJoinSet.get(pXORJoin);
		List<PetrinetNode> splitNodes = xorSplitSet.get(pXORSplit);
		// how many they are, or just check it in the variant and then record it here..
		// now they are binary relation, how about many of them?? 
		Map<List<PetrinetNode>, List<Integer>> combinations =  new HashMap<List<PetrinetNode>, List<Integer>>();
		for(PetrinetNode jNode: joinNodes) {
			for(PetrinetNode sNode: splitNodes) {
				List<PetrinetNode> key = new ArrayList<PetrinetNode>();
				key.add(jNode);
				key.add(sNode);
				combinations.put(key, new ArrayList<Integer>());
			}
		}
		return combinations;
	}
	
	public boolean isNFCStructure(Place pXORJoin, Place pXORSplit, List<LabeledTraceVariant> variants) {
		
		
		// check in the log and find how they combine together
		// but we need to get the traceVariants, if we find it in pos and also not in neg, then 
		// we make it as one non free choice structure 
		// -- for each traceVariant, 
		// -- we get combinations from joinNodes, and splitNodes... we can create a map to count its frequency,
		// --  we check if there are such combination exists in it. 
		Map<List<PetrinetNode>, List<Integer>> combinations = getPotentialNFCStructure(pXORJoin, pXORSplit);
		for(LabeledTraceVariant var : variants) {
			List<XEventClass> traceVariant= var.getTraceVariant();
			for(List<PetrinetNode> key: combinations.keySet()) {
				if(isKeyContained(traceVariant, key)) {
					List<Integer> counts = new ArrayList<Integer>();
					counts.add(var.getPosNum());
					counts.add(var.getNegNum());
					combinations.put(key, counts);
				}
			}
			
		}
		
		// check the combination counts
		
		
		return false;
	}
	
	private Collection<List<PetrinetNode>> getNFCCombination(Map<List<PetrinetNode>, List<Integer>> combinations) {
		// check the count of combinations. 
		// -- there are counts pos : neg
		// but also the whole combinations, we need to think of.. 
		// -- if all the combinations only have positive,then we see it a free choice structure
		// -- if part of the combinations have positive, and partial negative, but we also need 
		// to pay attention if the negative violates positive.
		
		// focus one the positive ones, if positive are partial,
		// one item has positive and negative frequency, god, we still need to think of the weight from each side
		// but because we just add places, so my strategy is happens than an threshold, or compare to the positive ones
		Collection<List<PetrinetNode>> nonFCSet = new ArrayList<>();
		// we might need another structure to store all the data, or is there some
		PetrinetNode preNode;
		List<Integer> keyValue;
		for(List<PetrinetNode> key : combinations.keySet()) {
			// check how many postNodes for each preNode with positive ones,
			// if it has all the posibility, then we don't care about it
			// but if one combination has 0, or they have negative values for it
			keyValue = combinations.get(key);
			
			if(keyValue.get(0)<1 || keyValue.get(1)>1) {
				// here maybe a non free choice change
				// now we should consider the other structure, add places to other branches
				
			}
			
			if(keyValue.get(0)>1 && keyValue.get(1)>1 && keyValue.get(1)> keyValue.get(0)) {
				
			}
			
		}
		
		return null;
	}
	
	/**
	 * what if we split it into an object?? will it be better?? 
	 * @param traceVariant
	 * @param key
	 * @return
	 */
	private boolean isKeyContained(List<XEventClass> traceVariant, List<PetrinetNode> key) {
		// if there is a map, it could be great
		for(PetrinetNode node: key) {
			if(!traceVariant.contains(maps.get((Transition)node)))
				return false;
		}
		return true;
	}
	
}
