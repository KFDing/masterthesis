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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.preprocess.util.NetUtilities;
import org.processmining.plugins.ding.process.dfg.model.PotentialNFCCluster;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XorSplitCluster;

public class NFCDetector {

	Petrinet net;
	

	XLog log;
	
	Map<Place, List<PetrinetNode>> xorJoinSet ;
	Map<Place, List<PetrinetNode>> xorSplitSet ;
	Map<PetrinetNode, PotentialNFCCluster> totalCluster ;
	
	Map<Transition, XEventClass> maps;
	
	public NFCDetector(Petrinet pnet, XLog log) {
		// we need to clone a net
		this.net = PetrinetFactory.clonePetrinet(pnet);
		this.log = log;
		maps = NetUtilities.getTransition2EventMap(log, net , null);
		xorJoinSet = new HashMap<Place, List<PetrinetNode>>();
		xorSplitSet = new HashMap<Place, List<PetrinetNode>>();
		totalCluster = new HashMap<PetrinetNode, PotentialNFCCluster>();
	}
	/**
	 * at first, we need to decide if we need it or not !! If there exists already NonFreeChoice in the net
	 * so, we don't need to add anymore
	 * @return
	 */
	public  Petrinet getNet() {
		return net;
	}

	public void setNet(Petrinet net) {
		this.net = net;
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
	public void buildPotentialCombination(){
		// from xor split and join, we create the possible combination of non free choice structure
		// after this, we should have initialized PotentialNFC Cluster
		// -- for place xor join 
		// 	-- for place xor split
		//     -- we get one transition of it.. 
		//     -- add all the rest xor split into it ??? How to control the direction of it ?? 
		//     -- we can have such A-(D,E), but we could also have D-(A,B).. then we need one direction 
		//     --- but add later to improve efficiency
		// if we put them into combination, it means actually that single object exists 
		PotentialNFCCluster cluster;
		setXORStructure();
		for(Place pXORJoin: xorJoinSet.keySet()) {
			List<PetrinetNode> joinNodes = xorJoinSet.get(pXORJoin);
			
			for(Place pXORSplit: xorSplitSet.keySet()) {
				
				List<PetrinetNode> splitNodes = xorSplitSet.get(pXORSplit);
				// we test them they are in the same structure,
				if(isInSameStructure(joinNodes, splitNodes)) {
					continue;
				}
				// how many they are, or just check it in the variant and then record it here..
				// now they are binary relation, how about many of them?? 
				
				for(PetrinetNode jNode: joinNodes) {
					// if there is no such structure in the Whole Map, then we need to add it into this
					if(totalCluster.containsKey(jNode)) {
						cluster = totalCluster.get(jNode);
					}else {
						// add to combinations
						cluster =  new PotentialNFCCluster(jNode);
						totalCluster.put(jNode, cluster);
					}
					
					XorSplitCluster sc = new XorSplitCluster(splitNodes.size()); 
					// sc.setCompletSize(splitNodes.size());
					for(PetrinetNode sNode: splitNodes) {
						ArrayList<Double> freq = new ArrayList<Double>();
						initFreq(freq);
						sc.addPotentialConnection(sNode, freq);
					}
					cluster.addCluster(sc);
				}
				
			}
		}
	}
	
	private void initFreq(ArrayList<Double> freq) {
		for(int i=0; i< ProcessConfiguration.RULESET_IDX_NUM; i++) 
			freq.add(0.0);
	}

	private boolean isInSameStructure(List<PetrinetNode> joinNodes, List<PetrinetNode> splitNodes) {
		Set<PetrinetNode> joinSet = new HashSet<PetrinetNode>(joinNodes);
		Set<PetrinetNode> splitSet = new HashSet<PetrinetNode>(splitNodes);
		if(joinSet.containsAll(splitSet) && splitSet.containsAll(joinSet))
			return true;	
		return false;
	}

	
	private void fillPotentialNFCCluster(LabeledTraceVariant var, PotentialNFCCluster cluster) {
	
		// List<XEventClass> rules = new ArrayList<XEventClass>();
		List<XEventClass> traceVariant = var.getTraceVariant();
		// rules.add(maps.get(cluster.getKeyNode()));
		int idx = findNodeIndex(cluster.getKeyNode(), traceVariant);
		if(idx != -1) {
			for(XorSplitCluster sc :cluster.getNFCClusters()) {
				// consider now only one transition and one xor split structure
				
				for(PetrinetNode node: sc.getKeySet()) {
					// it is behind the key Node, we put it there, else, we use another structure
					if(findNodeIndex(node, traceVariant) > idx) {	// maybe we can build it here; if it exists, then we put it, else no worry.
						ArrayList<Double> counts = new ArrayList<Double>();
						counts.add(0, 0.0); // for existing ones
						counts.add((double) var.getPosNum());
						counts.add((double) var.getNegNum());
						
						sc.addPotentialConnection(node, counts);
					}
				}
			}
		}
	}
	
	public void detectNFCClusters() {
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		for(LabeledTraceVariant var : variants) {
			for(PetrinetNode key: totalCluster.keySet()) {
				fillPotentialNFCCluster(var, totalCluster.get(key));	
			}
		}
		// after all of this, we check for each PotentialNFCCluster
		// the structure if like that <Transition A, Transition B>, A->B one place, or 
		// if it possible that multiple in to multiple out edges??
		// so the rules can be that 
		for(PetrinetNode ruleKey: totalCluster.keySet()) {
			// here will create rule set from this transition
			totalCluster.get(ruleKey).buildRuleSet();
		}
		
		// get all the set and then combine them together.. 
		// reduceRuleSet(rules);
		removeNFCConstraints();
		
		int i= reduceRuleSet();
		while(i>0) {
			i = reduceRuleSet();
		}
	}
	
	public void removeNFCConstraints() {
		for(PetrinetNode ruleKey: totalCluster.keySet()) {
			
			PotentialNFCCluster pCluster = totalCluster.get(ruleKey);
			for(XorSplitCluster cluster: pCluster.getNFCClusters()) {
				List<PetrinetNode> targets = cluster.getNFSet();
				if(cluster.isComplete()) {
					for(PetrinetNode ruleTarget: targets) {
							// delete the structure of NFC structure, but we can also put it somewhere else
							Place nfcPlace = NFCAvailabel(ruleKey, ruleTarget);
							if(nfcPlace != null) {
								net.removeArc(ruleKey, nfcPlace);
								net.removeArc(nfcPlace, ruleTarget);
								net.removePlace(nfcPlace);
							}
						}
				}
			}
		}
	}
	public void addNFCConstraints() {
		if(totalCluster.size()<1)
			return;
		// add places to net
		// but we also need to delete some nfc from net
		// but how to distinguish them?? 
		for(PetrinetNode ruleKey: totalCluster.keySet()) {
			// here will create rule set from this transition
			for(PetrinetNode ruleTarget : totalCluster.get(ruleKey).getRuleSet())
				 // here is already the new graph
				// test here if there exists direct place which connect ruleKey and ruleTarget
				if(NFCAvailabel(ruleKey, ruleTarget) == null) {
					String label = ruleKey.getLabel() +"_"+ ruleTarget.getLabel();
					Place place = net.addPlace(label);
					net.addArc((Transition) ruleKey, place);
					net.addArc(place, (Transition) ruleTarget);
				}
		}
		
	}
	
	private Place NFCAvailabel(PetrinetNode ruleSource, PetrinetNode ruleTarget) {
		Collection<Place> places =  net.getPlaces();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
    	boolean match = false;
		for(Place p: places) {
			preset = net.getInEdges(p);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				Arc arc = (Arc) edge;
				if(arc.getSource().equals(ruleSource)) {
					match = true;
					break;
				}
			}
			if(match) {
				postset = net.getOutEdges(p);
				match = false;
				// we need to see two transitions together???  
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
					Arc arc = (Arc) edge;
					if(arc.getTarget().equals(ruleTarget)) {
						match = true;
						break;
					}
				}
			}
			if(match)
				return p;
		}
		return null;
	}
	private int reduceRuleSet(){
		// here will create rule set from this transition
		// we have ruleSource and ruleTarget, now we try to find if they are direct connected,
		// there is no another rules between them
		int i=0;
		// we create a new totalCluster to represent this one!! 
		Map<PetrinetNode, PotentialNFCCluster> newCluster = new HashMap<PetrinetNode, PotentialNFCCluster>();
		for(PetrinetNode ruleSource : totalCluster.keySet()) {
			// most important is we also change ruleValue from this step
			// then we need to assign the value differently
			List<PetrinetNode> ruleValue = totalCluster.get(ruleSource).getRuleSet();	
			Iterator<PetrinetNode> iter = ruleValue.iterator();
			while(iter.hasNext()) {
				newCluster.put(ruleSource, totalCluster.get(ruleSource));
				PetrinetNode node = iter.next();
				// check if we keep this rules, or not, only second structure
				if(totalCluster.containsKey(node)){
					List<PetrinetNode> secRuleValue =  totalCluster.get(node).getRuleSet();
					if(secRuleValue.contains(node)) {
						// remove the node from ruleValue 
						iter.remove();
						i++;
					}
				}
			}
			// after this step, we get one ruleValue which contains possible relations
			// test if ruleValue and iter at last the same?? 
		}
		totalCluster = newCluster;
		return i;
	}
	
	private int findNodeIndex(PetrinetNode keyNode, List<XEventClass> traceVariant) {
		return traceVariant.indexOf(maps.get(keyNode));
	}
	
}
