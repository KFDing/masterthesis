package org.processmining.plugins.ding.process.dfg.train;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.NewLTConnection;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

/**
 * this class is special to add long-term dependency on net,
 * which demands parameters of net, process tree, clusterPairList, it is I guess enough for all the things
 * how to process ?? 
 *  -- get rule set for each cluster pair, I guess??? ::   
 *       LTRule:: it should store the original and the new ones, so we can divide them easily
 *       ++ how to merge those rules?? 
 *       ++ how to add those rules ??
 *       ++ we only have rules in current situations
 *  -- add places w.r.t. the rule set in this branch pair. 
 *     ++ first to get the places to combine by silent transtion
 *     ++ add silent transtion and places
 *  -- return net.
 *  
 *  How to deal with the different branch pair, only the parallel branch needs special attention?? 
 *  I will organize it again and code it later
 * @author dkf
 *
 */
public class AddLT2Net {
	// before writing them , design it clearly and then write codes
	Set<LTRule<PetrinetNode>> ruleSet;
	Petrinet net;
	ProcessTree tree;
	Map<Node, Transition> tnMap;
	Map<String, PetrinetNode> pnNodeMap ;
	
	public AddLT2Net(Petrinet net, ProcessTree tree) {
		this.net = net;
		this.tree = tree;
		ruleSet = new HashSet<LTRule<PetrinetNode>>();
	}
	
	
	
    public void addLTOnPair(XORClusterPair<ProcessTreeElement> pair) {
	   XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
	   sourceCluster = pair.getSourceXORCluster();
	   targetCluster = pair.getTargetXORCluster();
	   
	   if(sourceCluster.isNotNXORCluster() && targetCluster.isNotNXORCluster()) {
		   addOnNotNestedPair(pair);
	   }else if(sourceCluster.isParallelCluster()) {
		   // we need to merge rule set 
		   addLTOnParallel(pair);
		   // what we need to do later ??
		   
	   }else  {
		   // we need to look for the ltBranchPair on it 
		   for(XORClusterPair<ProcessTreeElement> branchPair: pair.getLtBranchClusterPair()) {
			   addLTOnPair(branchPair);
		   }
		   
	   }
    }
	
	
	private void addLTOnParallel(XORClusterPair<ProcessTreeElement> pair) {
		// TODO should we return the new added places ??
		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
		sourceCluster = pair.getSourceXORCluster();
		targetCluster = pair.getTargetXORCluster();
		   
		for(XORClusterPair<ProcessTreeElement> branchPair: pair.getLtBranchClusterPair()) {
			   addLTOnPair(branchPair);
		}
		
		// here after adding all places on its branch, we merge rules here.
		// for the same target ltBranchCluster, we have 
		
	}


	// but our task is mainly to merge the rule set and then create new places for them
	public void addOnNotNestedPair(XORClusterPair<ProcessTreeElement> pair) {
		// two xor are in a seq pair, both are xor, now what to do then?? 
		List<XORClusterPair<ProcessTreeElement>> ltBranchPair = pair.getLtBranchClusterPair();
		
		// should we get the ltconnection, somehow ?? 
		// if we get the branchPair
		// if we get the return place from them, it could be great
		
	}
	
	public void getRuleInAnd(XORClusterPair<ProcessTreeElement> pair) {
		// and pair includes xor, and now it's their relation 
		
		// three situations: source and, target and, both and, so how to deal with it
		// but we just consider the source is and, that's all, if there are some in target, we don't care
		// because it's decided by the and pair before
		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
		sourceCluster = pair.getSourceXORCluster();
		targetCluster = pair.getTargetXORCluster();
		
		// only source is and with xor over 1
		if(sourceCluster.getEndXORList().size() > 1) {
			// we have different situations, w.r.t. to the target cluster, 
			// since it should show all the situations of following xor cluster, so we can have all combinations
			// get all combinations w.r.t. ltConnection
			List<NewLTConnection<ProcessTreeElement>> ltConns = pair.getLtConnections();
			
			// because it is binary, so we check at first ltConns with the same target 
			
			// get the target nodes set from ltConns
			
			List<ProcessTreeElement> targetNodes = getTargetNodeSet(ltConns);
			
			// for the same target, get all the connection of it, but we need to make sure that we need to do them all.
			ProcessTreeElement target = targetNodes.get(0);
			
			// return the source set with same target
			List<ProcessTreeElement> sourceWithTarget = getSourceWithTarget(ltConns, target);
			
			// decide the relation of each source node 
			ProcessTreeElement source = sourceWithTarget.get(0);
			
			List<ProcessTreeElement> sourceInXOR = getSourceInXOR(pair, source);
			
			// how to decide they should be together ?
			List<ProcessTreeElement> sourceInAnd = getSourceInAnd(source, sourceInXOR, sourceWithTarget);
			
			// change the rule set
			// combine them all together, put them into ruleSet
			LTRule<PetrinetNode> rule = findRuleForConn(source, target);
			if(rule != null) {
				for(ProcessTreeElement s: sourceInAnd) {
					LTRule<PetrinetNode> tmpRule = findRuleForConn(source, target);
					if(tmpRule!= null) {
						// merge this two rules together
						// but to change which one?? tmpRule is better
						tmpRule.addRuleSource(tnMap.get(source));
					}
				}
			}
			
			ruleSet.remove(rule);
			
			
		}
		
	}
	
	
	
	// image we have rules which has multiple sources but one target?? should we also assign multiple targets??
	// input is the current rules sets and current pair to deal with
	public void addLTInAnd(XORCluster<ProcessTreeElement> sBranch, List<LTRule<ProcessTreeElement>> andRules) {
		// for each rule in andRules, we find the places for them and combine them together 
		// by using silent transition, and the add one places for it, can we store one places there and then combine them ??
		// each rules has one places to represent the source and target places, could we do this ??
		
		// sBranch is a parallel cluster with over 1 xor cluster, now we are going to find the places for each rule
		for(LTRule<ProcessTreeElement> rule: andRules) {
			List<Place> postPlaces = getSourcePlace(rule.getSources(), rule.getSources());
			
			// add one silent transtion
			
			Transition sTransition = null;
			// this is not proper to add this transition, we need to mark this silent transtion here to combine all the  postplaces
			// postPlace
			String transtionName = null ;
			for(Place pp: postPlaces) {
				 transtionName += pp.getLabel();
			}
			
			if(!pnNodeMap.containsKey(transtionName)) {
				sTransition = net.addTransition(transtionName);
				sTransition.setInvisible(true);
				
				pnNodeMap.put(transtionName, sTransition);
			}else {
				sTransition = (Transition) pnNodeMap.get(transtionName);
			}
			for(Place p: postPlaces)
				net.addArc(p, sTransition);
			// also one place for silent transition
			
			Place sBranchPlace;
			String branchName = transtionName;
			if(!pnNodeMap.containsKey(branchName)) {
				sBranchPlace = net.addPlace(branchName);
				// we need to add the silent transition after it
				pnNodeMap.put(branchName, sBranchPlace);
			}else
				sBranchPlace = (Place) pnNodeMap.get(branchName);
			
			net.addArc(sTransition, sBranchPlace);
			
		}
		
		
	}
	
	
	private List<Place> getSourcePlace(List<ProcessTreeElement> sources, List<ProcessTreeElement> newSources) {
		// TODO get source places w.r.t. to the sources
		List<Place> postPlaces = new ArrayList<Place>();
		
		// we need to divide the sources into different parts, if they belong to xor 
		// or they belong to, we can get them w.r.t. the structure of tree
		// the least common ancestor is xor then it is in xor, 
		// the least common ancestor is parallel, then in and 
		
		// mewSources are the source new added to this rule, then we can know how to find them
		String placeName = null;
		//List<ProcessTreeElement> originalSource = new ArrayList<ProcessTreeElement>();
		for(ProcessTreeElement source: sources)
			if(!newSources.contains(source)) {
				//originalSource.add(s);
				placeName += source.getName(); 
			}
		String newPlaceName = null;
		for(ProcessTreeElement source: newSources) {
			newPlaceName += source.getName();
		}
		// for original source, we find the place for it
		for(String keyName: pnNodeMap.keySet()) {
			if((keyName.contains(placeName) || keyName.contains(newPlaceName) )&& pnNodeMap.get(keyName) instanceof Place) {
				postPlaces.add((Place) pnNodeMap.get(keyName));
			}
		}
		return postPlaces;
	}

	private LTRule<PetrinetNode> findRuleForConn(ProcessTreeElement source, ProcessTreeElement target) {
		// TODO now we have more relation here, which is actually 
		PetrinetNode sNode =  tnMap.get(source);
		PetrinetNode tNode =  tnMap.get(target);
		for(LTRule<PetrinetNode> rule: ruleSet) {
			if(rule.getSources().contains(sNode) && rule.getTargets().contains(tNode)) {
				return rule;
			}
		}
		// what if there are more nodes there, what to do then?? 
		return null;
	}


	private List<ProcessTreeElement> getSourceInAnd(ProcessTreeElement source, List<ProcessTreeElement> sourceInXOR,
			List<ProcessTreeElement> sourceWithTarget) {
		// TODO Auto-generated method stub
		List<ProcessTreeElement> sourceInAnd = new ArrayList<ProcessTreeElement>();
		for(ProcessTreeElement s : sourceWithTarget) {
			if(!sourceInXOR.contains(s))
				sourceInAnd.add(s);
		}
		return sourceInAnd;
	}

	private List<ProcessTreeElement> getSourceInXOR(XORClusterPair<ProcessTreeElement> pair, 
			ProcessTreeElement source) {
		// TODO: get all source in the same xor in source cluster
		
		// we need to check the ltBranchPair 
		for(XORClusterPair<ProcessTreeElement> branchPair : pair.getBranchClusterPair()) {
			List<NewLTConnection<ProcessTreeElement>> conns = branchPair.getConnection();
			
			List<ProcessTreeElement> sourceNodes = getSourceNodeSet(conns);
			// get all the sources node from ltConns
			if(sourceNodes.contains(source))
				return sourceNodes;
			
		}
		// get it and check if source in this ltbranchPair, if it is, then get the other source from ltConnection from it.
		
		return null;
	}



	private List<ProcessTreeElement> getSourceWithTarget(List<NewLTConnection<ProcessTreeElement>> ltConns,
			ProcessTreeElement target) {
		List<ProcessTreeElement> sourceNodes = new ArrayList<ProcessTreeElement>();
		
		for(NewLTConnection<ProcessTreeElement> conn: ltConns) {
			if(conn.getTargetBranch().equals(target) && !sourceNodes.contains(conn.getSourceBranch())) {
				sourceNodes.add(conn.getSourceBranch());
			}
		}

		return sourceNodes;
	}

	private List<ProcessTreeElement> getSourceNodeSet(List<NewLTConnection<ProcessTreeElement>> ltConns) {
		// 
		List<ProcessTreeElement> sourceNodes = new ArrayList<ProcessTreeElement>();
		
		for(NewLTConnection<ProcessTreeElement> conn: ltConns) {
			if(!sourceNodes.contains(conn.getTargetBranch())) {
				sourceNodes.add(conn.getTargetBranch());
			}
		}
		
		return sourceNodes;
	}
	
	private List<ProcessTreeElement> getTargetNodeSet(List<NewLTConnection<ProcessTreeElement>> ltConns) {
		// 
		List<ProcessTreeElement> targetNodes = new ArrayList<ProcessTreeElement>();
		
		for(NewLTConnection<ProcessTreeElement> conn: ltConns) {
			if(!targetNodes.contains(conn.getTargetBranch())) {
				targetNodes.add(conn.getTargetBranch());
			}
		}
		
		return targetNodes;
	}

	// add one the pure branch cluster, the deepest structure
	public void addOnPureBranch (XORClusterPair<ProcessTreeElement> branchPair) {
		XORCluster<ProcessTreeElement> sBranch, tBranch;
		sBranch = branchPair.getSourceXORCluster();
		tBranch = branchPair.getTargetXORCluster();
		
		LTRule<PetrinetNode> rule = new LTRule<PetrinetNode>();
		
		
		List<PetrinetNode> endNodeList = transform2PNNodes(sBranch.getEndNodeList());
		List<PetrinetNode> beginNodeList = transform2PNNodes(tBranch.getBeginNodeList());
		
		// because they are pure branch, so if it shows here, they have connection!!
		// before we need to change them into petri net 
		rule.addRuleList(endNodeList, beginNodeList);
		ruleSet.add(rule);
		
		List<Place> postPlaces = new ArrayList<Place>();
		List<Place> prePlaces = new ArrayList<Place>();
		
		// add post places for the end node 
		for(PetrinetNode endNode: endNodeList) {
			String keyName =  ProcessConfiguration.POST_PREFIX + "-"+ endNode.getLabel();
			Place postNode ;
			if(!pnNodeMap.containsKey(keyName)) {
				postNode = net.addPlace(keyName);
				// we need to add the silent transition after it
				pnNodeMap.put(keyName, postNode);
			}else
				postNode = (Place) pnNodeMap.get(keyName);
			
			postPlaces.add(postNode);
		}
		
		
		for(PetrinetNode beginNode: beginNodeList) {
			String keyName = ProcessConfiguration.PRE_PREFIX + "-" +beginNode.getLabel();
			Place preNode ;
			if(!pnNodeMap.containsKey(keyName)) {
				preNode = net.addPlace(keyName);
				// we need to add the silent transition after it
				pnNodeMap.put(keyName, preNode);
			}else
				preNode = (Place) pnNodeMap.get(keyName);
			
			prePlaces.add(preNode);
		}
		
		
		// add merged transtion to combine end node list 
		if(endNodeList.size()>1) {
			// add one silent transition to combine all the source nodes
			Transition sTransition = null;
			String transtionName = sBranch.getLabel();
			if(!pnNodeMap.containsKey(transtionName)) {
				sTransition = net.addTransition(transtionName);
				sTransition.setInvisible(true);
				
				pnNodeMap.put(transtionName, sTransition);
			}else {
				sTransition = (Transition) pnNodeMap.get(transtionName);
			}
			for(Place p: postPlaces)
				net.addArc(p, sTransition);
			// also one place for silent transition
			
			Place sBranchPlace;
			String branchName = sBranch.getLabel();
			if(!pnNodeMap.containsKey(branchName)) {
				sBranchPlace = net.addPlace(branchName);
				// we need to add the silent transition after it
				pnNodeMap.put(branchName, sBranchPlace);
			}else
				sBranchPlace = (Place) pnNodeMap.get(branchName);
			
			net.addArc(sTransition, sBranchPlace);
			
		}
		// add merged transtion to combine the begin node list
		if(beginNodeList.size() > 1) {
			// add one silent transition to combine all the target nodes
			Transition tTransition = null;
			String transtionName = tBranch.getLabel();
			if(!pnNodeMap.containsKey(transtionName)) {
				tTransition = net.addTransition(transtionName);
				tTransition.setInvisible(true);
				
				pnNodeMap.put(transtionName, tTransition);
			}else {
				tTransition = (Transition) pnNodeMap.get(transtionName);
			}
			
			for(Place p: prePlaces)
				net.addArc(tTransition, p);
			
			// one place for it 
			Place tBranchPlace;
			String branchName = tBranch.getLabel();
			if(!pnNodeMap.containsKey(branchName)) {
				tBranchPlace = net.addPlace(branchName);
				// we need to add the silent transition after it
				pnNodeMap.put(branchName, tBranchPlace);
			}else
				tBranchPlace = (Place) pnNodeMap.get(branchName);
			
			net.addArc(tBranchPlace, tTransition);
		}
		
		// how to get the return post place and pre places?? 
		if(endNodeList.size() > 1) {
			
		}
		if(beginNodeList.size() > 1) {
			
		}
		
	}


	private List<PetrinetNode> transform2PNNodes(List<ProcessTreeElement> nodeList) {
		// TODO Auto-generated method stub
		List<PetrinetNode> pnNodes = new ArrayList<PetrinetNode>(); 
		for(ProcessTreeElement ptNode: nodeList) {
			pnNodes.add(tnMap.get(ptNode));
		}
		return pnNodes;
	}



	public void addLTInAnd(XORCluster<ProcessTreeElement> sourceCluster, XORCluster<ProcessTreeElement> targetCluster) {
		// TODO Auto-generated method stub
		
	}



	public void addLTInPair(XORCluster<ProcessTreeElement> sourceCluster,
			XORCluster<ProcessTreeElement> targetCluster) {
		// TODO Auto-generated method stub
		
	}
}
