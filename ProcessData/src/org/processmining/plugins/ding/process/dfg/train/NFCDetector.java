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
	Map<PetrinetNode, PotentialNFCCluster> reducedCluster ;
	List<PetrinetNode> addedPlaces;
	Map<Transition, XEventClass> maps;
	
	public NFCDetector(Petrinet pnet, XLog log) {
		// we need to clone a net
		this.net = PetrinetFactory.clonePetrinet(pnet);
		this.log = log;
		maps = NetUtilities.getTransition2EventMap(log, net , null);
		xorJoinSet = new HashMap<Place, List<PetrinetNode>>();
		xorSplitSet = new HashMap<Place, List<PetrinetNode>>();
		totalCluster = new HashMap<PetrinetNode, PotentialNFCCluster>();
		addedPlaces = new ArrayList<PetrinetNode>();
		
		setXORStructure();
		removeNFC();
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
					
					XorSplitCluster sc = new XorSplitCluster(jNode, splitNodes.size()); 
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
		
		
		int i= reduceRuleSet();
		while(i>0) {
			i = reduceRuleSet();
		}
		
		// get all the set and then combine them together.. 
		// reduceRuleSet(rules);
		// removeNFCConstraints();
				
	}
	
	// at preprocess part
	public void removeNFC() {
		// we need to say that they have global dependency
		for(Place pXORJoin: xorJoinSet.keySet()) {
			List<PetrinetNode> joinNodes = xorJoinSet.get(pXORJoin);
			
			for(Place pXORSplit: xorSplitSet.keySet()) {
				
				List<PetrinetNode> splitNodes = xorSplitSet.get(pXORSplit);
				// we test them they are in the same structure,
				if(isInSameStructure(joinNodes, splitNodes)) {
					continue;
				}
				
				for(PetrinetNode jNode : joinNodes) 
					for(PetrinetNode sNode: splitNodes) {
						List<PetrinetNode> nfcNodes = getNFCConnection(jNode, sNode);
						if(! nfcNodes.isEmpty()) {
							
						   net.removeArc(jNode, nfcNodes.get(0));
						   for(int i=0;i< nfcNodes.size() -1;i++) 
								net.removeArc(nfcNodes.get(i), nfcNodes.get(i+1));
						
						   net.removeArc( nfcNodes.get(nfcNodes.size() - 1), sNode);
						   
						   for(int i=0;i< nfcNodes.size() -1;i++) {
							   if(nfcNodes.get(i) instanceof Place)
								   net.removePlace((Place) nfcNodes.get(i));
							   else if(nfcNodes.get(i) instanceof Transition)
								   net.removeTransition((Transition) nfcNodes.get(i));
						   }
						   
						}
					}
				
			}
		}
	}
	
	/**
	 * // now we check it from one structure to another structure?? Or we do it w.r.t. 
		// to the totalCluster?? It can be faster, and also by the way, we need to mark it
		// if for this xor join structure it has done all things already
		// if we do the structure search, much simpler!! 
		// -- for each xor split structure
		//    -- if there is any uncomplete nfc cluster including one of node from xor split structure
		//       -- Y: we get the xor join structure with nfc cluster 
		// 		 	   -- for nfc in such xor join structure
		// 				  -- we create places and arcs, one is after the keyNode, others before the xor split
		//			 	  -- connect to places by silent transitions
		
	 */
	public void addNFCConstraints() {
		if(totalCluster.size()<1)
			return;
		// at this step there are already the only cluster with uncomplete structures. 
		for(PetrinetNode ruleKey: reducedCluster.keySet()) {
			PotentialNFCCluster pCluster = reducedCluster.get(ruleKey);
			
			for(XorSplitCluster cluster: pCluster.getNFCClusters()) {
				if(cluster.isComplete())
					continue;
				// we find the same xorJoinStructure from xorJoinSet by using ruleKey
				// then check the other situations. Get the ruleSets from them
				// then we move to next structure, if we have alreay test their connections
				// we could put some flags into it and say, we have already check it 
				List<PetrinetNode> nodesInSameStructure = getNodeInSameStructure(ruleKey,false );
				
				// for each cluster, we need to add constraints on the net 
				for(PetrinetNode node: nodesInSameStructure) {	
					// but we need to get the same cluster from each node 
					// from the same xor join to cluster, they should create a place before the cluster nodes
					// A->G and B->H, one place before G
					// and then if we change to another structure we create new nodes. 
					// E->G and D->G or D->H we have another place before G, which is different 
					// now, we distinguish them by names
					addOneClusterConstaint(cluster, node);
				}
				
			}
		}
		
	}
	
	private void addOneClusterConstaint(XorSplitCluster refCluster, PetrinetNode ruleSource) {
		// what we want to add is the corresponding cluster not the whole structure,
		// we need to get the cluster from refCluster
		// -- check all the cluster of ruleSource 
		/*
		System.out.println(ruleSource.getLabel()+" with " + refCluster.getName());
		
		if(!totalCluster.containsKey(ruleSource)) {
			// if we don't have any information about it 
		}
		System.out.println(ruleSource.getLabel()+" with " + refCluster.getName());
		if(pCluster.getNFCClusters().isEmpty()) {
			System.out.println(ruleSource.getLabel()+" with " + refCluster.getName()+" pcluster is empty, which is not so often, I think, must something happens.."
					+ "how about D?? ");
			return;
		}
		*/
		XorSplitCluster cluster = null;
		PotentialNFCCluster pCluster = totalCluster.get(ruleSource);
		for(XorSplitCluster c: pCluster.getNFCClusters()) {
			if(c.inSameStructure(refCluster)) {
				cluster = c;
				break;
			}
		}
		
		if(cluster.isVisited())
			return; 
		// we assume there is no new added place after A
		// what if there are alreday existing structures there?? I think we shoudl delete them at first
		// then we can add pure new structure on net
		// here we can't create it like this, because A can have another connections, so we need to think 
		// of another way, like to combine the cluster information?? 
		Place postNode = (Place) getAddedPostPlace(ruleSource, cluster);
		if(postNode == null) {
			postNode = net.addPlace("Place After "+ ruleSource.getLabel() + cluster.getName());
			addedPlaces.add(postNode);
			net.addArc((Transition) ruleSource, postNode);
		}
		List<PetrinetNode> targets = cluster.getNFSet();
		// for all the targets we only create one place in net, all the left connect to it
		// check if there exists one edge from source to any target?
		// if exists we don't need to create any place and arc
		// then for the left targets, if it has arc then we don't add it, else we add it
		// add arcs for each branch
		Place preNode; 
		for(PetrinetNode ruleTarget : targets) {
			// we need to check  the place  before the target
			// should we keep some where of preTarget places??? 
			preNode = (Place) getAddedPrePlace(ruleTarget, ruleSource);
			// to get the place before this ruleTarget
			if(preNode == null) {
				preNode = net.addPlace("Place Before "+  getSameStructureName(ruleSource) + ruleTarget.getLabel());
				addedPlaces.add(preNode);
				net.addArc(preNode, (Transition) ruleTarget);
			}
			// after we get it then we can create silent transition to connect the two places
			Transition sTransition = net.addTransition(ruleSource.getLabel() + ruleTarget.getLabel());
			sTransition.setInvisible(true);
			net.addArc(postNode, sTransition);
			net.addArc(sTransition, preNode);
			
		}
		cluster.setVisited(true);
	}
		

	private PetrinetNode getAddedPrePlace(PetrinetNode target, PetrinetNode source) {
		// we need to get the same structure of ruleSource  and check if it exists
		
		for(PetrinetNode p: addedPlaces) {
			if(p.getLabel().contains(target.getLabel())  &&  p.getLabel().contains(getSameStructureName(source)) && p.getLabel().contains("Before"))
				return p;
		}
		return null;
	}
	
	private String getSameStructureName(PetrinetNode source) {
		List<PetrinetNode> nodes = getNodeInSameStructure(source,false );
		String name = "";
		for(PetrinetNode node: nodes )
			name += node.getLabel();
		
		return name;
	}
	
	private PetrinetNode getAddedPostPlace(PetrinetNode target, XorSplitCluster refCluster) {
		// we use it to check if there is alreay place pre or post of one transition
		
			for(PetrinetNode p: addedPlaces) {
				if(p.getLabel().contains(target.getLabel())  &&  p.getLabel().contains(refCluster.getName()) && p.getLabel().contains("After"))
					return p;
			}
		return null;
	}
	private List<PetrinetNode> getNodeInSameStructure(PetrinetNode ruleKey, boolean inSplit) {
		// TODO Auto-generated method stub
		List<PetrinetNode> nodes;
		Place place =  null;
		if(!inSplit) {
			
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
			postset = net.getOutEdges(ruleKey);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				Arc arc = (Arc) edge;
				place = (Place) arc.getTarget();
				if(xorJoinSet.containsKey(place)) {
					nodes = xorJoinSet.get(place);
					if(nodes.contains(ruleKey))
						return nodes;
				}
			}
		}else {
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
			preset = net.getInEdges(ruleKey);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				Arc arc = (Arc) edge;
				place = (Place) arc.getSource();
				if(xorSplitSet.containsKey(place)) {
					nodes = xorSplitSet.get(place);
					if(nodes.contains(ruleKey))
						return nodes;
				}
			}
		}
		return null;
	}
	/**
	 * we shoudl return a list of PetrinetNode which connects ruleSource and ruleTarget
	 * but do we need to delete them all, or just the silent transition between them, because
	 * we could find some fragments during the deletion. 
	 * @param ruleSource
	 * @param ruleTarget
	 * @return
	 */
	private List<PetrinetNode> getNFCConnection(PetrinetNode ruleSource, PetrinetNode ruleTarget) {
		List<PetrinetNode> paths = new ArrayList<PetrinetNode>();
		
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
				// here we could have silent transition , or like this direct connection is also possible
				// so we need to trace into it!!! 
				postset = net.getOutEdges(p);
				match = false;
				// we need to see two transitions together???  
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
					Arc arc = (Arc) edge;
					// check if it is possible silent transition to arc and after silent transition it works
					if(arc.getTarget().equals(ruleTarget)) {
						match = true;
						// we just return one place
						paths.add(p);
						break;
					}else {
						if(((Transition)arc.getTarget()).isInvisible()) {
							
							List<PetrinetNode> silentPaths = getNFCConnection(arc.getTarget(), ruleTarget);
							if(!silentPaths.isEmpty()) {
								paths.add(p);
								paths.add(arc.getTarget());
								paths.addAll(silentPaths);
								match = true;
								break;
							}
						}
					}
				}
			}
			if(match)
				return paths;
		}
		return paths;
	}
	private int reduceRuleSet(){
		// here will create rule set from this transition
		// we have ruleSource and ruleTarget, now we try to find if they are direct connected,
		// there is no another rules between them
		int i=0;
		// we create a new totalCluster to represent this one!! 
		// but we need to keep the nodes in the same structure they are also kept at end
		// if we have E-->G, then we also keep D-->G, and D-->H
		reducedCluster = new HashMap<PetrinetNode, PotentialNFCCluster>();
		for(PetrinetNode ruleSource : totalCluster.keySet()) {
			// most important is we also change ruleValue from this step
			// then we need to assign the value differently
			// here we should also remove the cluster which is not used like. D--> [A,B] cluster
			List<PetrinetNode> ruleValue = totalCluster.get(ruleSource).getRuleSet();	
				
			Iterator<PetrinetNode> iter = ruleValue.iterator();
			while(iter.hasNext()) {
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
			
			if(ruleValue.size() > 0 ) {
				// here we also put the same structure into the newCluster
				reducedCluster.put(ruleSource, totalCluster.get(ruleSource)); // because it is map, so don't worry about the repeated node	
			}
			
		}
		// totalCluster = reducedCluster;
		return i;
	}
	
	private int findNodeIndex(PetrinetNode keyNode, List<XEventClass> traceVariant) {
		return traceVariant.indexOf(maps.get(keyNode));
	}
	
}
