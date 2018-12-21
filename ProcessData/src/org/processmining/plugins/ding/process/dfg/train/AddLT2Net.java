package org.processmining.plugins.ding.process.dfg.train;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.processtree.Block;
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
	Set<LTRule<XORCluster<ProcessTreeElement>>> ruleSet;
	Petrinet net;
	ProcessTree tree;
	Map<Node, Transition> tnMap;
	Map<String, PetrinetNode> pnNodeMap ;
	
	public AddLT2Net(Petrinet net, ProcessTree tree) {
		this.net = net;
		this.tree = tree;
		ruleSet = new HashSet<LTRule<XORCluster<ProcessTreeElement>>>();
	}

    public void addLTOnPair(XORClusterPair<ProcessTreeElement> pair) {
	   XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
	   sourceCluster = pair.getSourceXORCluster();
	   targetCluster = pair.getTargetXORCluster();
	   // focus on source 
	   if(sourceCluster.isPureBranchCluster()) {
		   if(targetCluster.isPureBranchCluster())
			   addLTOnPureBranch(pair);
		   
	   }else if(sourceCluster.isParallelCluster()) {
		   // here we need to do what ?? source is parallel
		   // simple one parallel one, xor pair, or one pure branch.. 
		   addLTOnParallel(pair);
	   }else {
		   // it happens when target nested together...
		   // we should go far into the structure for what?? 
		   // or should we wait and get the branchPair to check their situation??
		   for(XORClusterPair<ProcessTreeElement> branchPair: pair.getLtBranchClusterPair()) {
			   addLTOnPair(branchPair);
		   }
	   }
	   // after this,adding places for sources, we need to add the target places and the other places
	   // how to we get all the  target, we don't really know, how to end them, but we need to make it end 
	   
	   for(LTRule<XORCluster<ProcessTreeElement>> rule: ruleSet ) {
		   List<XORCluster<ProcessTreeElement>> sourceBranches = rule.getSources();
		   String sBranchName = ProcessConfiguration.POST_PREFIX;
		   
		   for(XORCluster<ProcessTreeElement> sBranch: sourceBranches) {
				String tmpName = sBranch.getLabel();
				sBranchName += tmpName;
		   }
		   
		   Place splitPlace = (Place) pnNodeMap.get(sBranchName);
		   // get the target with same source
		   List<XORCluster<ProcessTreeElement>> targetWithSource = getTargetWithSource(sourceBranches);
		   
		   // here we divide them into different xor branches, we could have a list of list
		   List<XORCluster<ProcessTreeElement>> targetXOR = divideTargetsInXOR(targetWithSource) ; 
		   // after we get the targetXOR, we need to get the and relation of them
		   for(XORCluster<ProcessTreeElement> tBranch: targetWithSource) {
			   
			   // we get the target in And, but we only need places of xor cluster those
			   List<XORCluster<ProcessTreeElement>> xorInAnd = getTargetInAnd(tBranch, targetXOR);
			    // we need to get the source place from sBranch here
			   List<Place> placeList = splitPlaces(splitPlace, xorInAnd);
			   
			   XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
			   addLTFromXOR2Branch(parentXOR, tBranch, sBranchName);
			   
		   }
	   }
	   
	   
    }


	private void addLTFromXOR2Branch(XORCluster<ProcessTreeElement> parentXOR, XORCluster<ProcessTreeElement> tBranch, String sBranchName) {
		// TODO add lt dependency of parent xor and tBranch
		Place parentPlace = (Place)getSourcePlace(parentXOR);
		// now it is one branch and one xor list, we need to ??
		
		// create silent transtion
		Transition sTransition = null;
		// this transition should be connected to the parentXOR, but also the source List
		String transitionName = sBranchName + parentPlace.getLabel() + tBranch.getLabel();
		
		if(!pnNodeMap.containsKey(transitionName)) {
			sTransition = net.addTransition(transitionName);
			sTransition.setInvisible(true);
			
			pnNodeMap.put(transitionName, sTransition);
		}else {
			sTransition = (Transition) pnNodeMap.get(transitionName);
		}
		
		net.addArc(parentPlace, sTransition);
		
		String branchName = ProcessConfiguration.PRE_PREFIX + tBranch.getLabel();
		Place tBranchPlace ;
		if(!pnNodeMap.containsKey(branchName)) {
			tBranchPlace = net.addPlace(branchName);
			// we need to add the silent transition after it
			pnNodeMap.put(branchName, tBranchPlace);
		}else
			tBranchPlace = (Place) pnNodeMap.get(branchName);
		
		net.addArc(sTransition, tBranchPlace);
		
	}

	private PetrinetNode getSourcePlace(XORCluster<ProcessTreeElement> parentXOR) {
		// TODO Auto-generated method stub
		String placeName = parentXOR.getLabel();
		
		for(String keyName: pnNodeMap.keySet())
			if(placeName.contains(keyName) && placeName.contains(ProcessConfiguration.POST_PREFIX))
				return pnNodeMap.get(keyName);
		
		return null;
	}

	private void addLTOnPureBranch(XORClusterPair<ProcessTreeElement> branchPair) {
		// add lt on pure branch, we need to test the structure of target
		XORCluster<ProcessTreeElement> sBranch, tBranch;
		sBranch = branchPair.getSourceXORCluster();
		tBranch = branchPair.getTargetXORCluster();
		branchPair.testConnected();
		if(branchPair.isConnected()) {
			// if this branch is connected, we need to add them self for it and keep into place list
			List<LTRule<XORCluster<ProcessTreeElement>>> connRules = branchPair.getLtConnections();
			ruleSet.addAll(connRules);
			// we have the connRules, even if we know there is only one
			for(LTRule<XORCluster<ProcessTreeElement>> rule: connRules) {
				// those are branches but whatever, we add them
				List<PetrinetNode> endNodeList = transform2PNNodes(sBranch.getEndNodeList());
				List<PetrinetNode> beginNodeList = transform2PNNodes(tBranch.getBeginNodeList());
				
				// add the places for source branch
				List<Place> postPlaces = new ArrayList<Place>();
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
				
				// add merged transition to combine end node list 
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
				
				// add for the target
				List<Place> prePlaces = new ArrayList<Place>();
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
		// here we need to check the rules they have, we put rules into ruleSet, image now we only have one pure branch target
		// how many xor branch it has
		List<XORCluster<ProcessTreeElement>> endXORList= sourceCluster.getEndXORList();
		if(endXORList.size()>1) {
			List<LTRule<XORCluster<ProcessTreeElement>>> ltConns = pair.getLtConnections();
			// we have connection rules here
			
			// to merge rules together, we need to find all the sources with the same target
			List<XORCluster<ProcessTreeElement>> targetBranches = getTargetNodeSet(ltConns);
			
			for(XORCluster<ProcessTreeElement> tBranch: targetBranches) {
				// return the source set with same target
				List<XORCluster<ProcessTreeElement>> sourceWithTarget = getSourceWithTarget(ltConns, tBranch);
				
				for(XORCluster<ProcessTreeElement> sBranch: sourceWithTarget) {
					
					List<XORCluster<ProcessTreeElement>> sourceInAnd = getSourceInAnd(sBranch, sourceWithTarget);
					
					// combine them all together, put them into ruleSet
					// here we need to modify the rules to make them together
					// first to make them work... 
					LTRule<XORCluster<ProcessTreeElement>> rule = findRuleForConn(sBranch, tBranch, ltConns);
					
					if(rule != null) {
						// sourceInAnd, could also be changed, because it can be more stuff here..
						for(XORCluster<ProcessTreeElement> s: sourceInAnd) {
							// the source can be in a list
							LTRule<XORCluster<ProcessTreeElement>> tmpRule = findRuleForConn(s, tBranch, ltConns);
							if(tmpRule!= null) {
								// merge this two rules together, the rules can be together, so we need to find them into ltConns
								// still, due to they are in parallel, it should work.
								
								
								// after merge rules, we need to add places and silent transition
								// for sBranch and s here, but we need to find the old rule??
								// find the place for original source and then one for the new added source
								List<Place> postPlaces = new ArrayList<Place>();
								// combine them together and merge them
								Place origPlace = (Place)getSourcePlace(tmpRule.getSources());
								// we need to get the places there
								List<XORCluster<ProcessTreeElement>> tmpSources = new ArrayList<XORCluster<ProcessTreeElement>>();
								tmpSources.add(s);
								Place addedPlace = (Place) getSourcePlace(tmpSources);
								
								postPlaces.add(origPlace);
								postPlaces.add(addedPlace);
								// here we can create a new silent transition and place for it !!
								combinePlaces(postPlaces);
								
								tmpRule.addRuleSource(s);
							}
						}
						ruleSet.remove(rule);
					}
					
				}
				
			}
			
		}
		
	}

	private List<Place> splitPlaces(Place splitPlace, List<XORCluster<ProcessTreeElement>> xorInAnd) {
		// after this splitPlace, we xor list happen in parallel
		if(xorInAnd.size() >1) {
			   System.out.println("the size of xor in And is this");
			   return null;
		}
	   List<Place> placeList = new ArrayList<Place>();
	   
	   Transition sTransition = null;
	   String transitionName = null;
	   // we give it the name of target label
	   for(XORCluster<ProcessTreeElement> xor: xorInAnd)
		   transitionName += xor.getLabel();
	   
	   if(!pnNodeMap.containsKey(transitionName)) {
			sTransition = net.addTransition(transitionName);
			sTransition.setInvisible(true);
			
			pnNodeMap.put(transitionName, sTransition);
	   }else {
			sTransition = (Transition) pnNodeMap.get(transitionName);
	   }
	   
	   // connect them together
	   net.addArc(splitPlace, sTransition);
	   
	   for(XORCluster<ProcessTreeElement> xor: xorInAnd) {
		   // generate the place for it 
		   Place sBranchPlace;
		   String branchName = transitionName + xor.getLabel();
			if(!pnNodeMap.containsKey(branchName)) {
				sBranchPlace = net.addPlace(branchName);
				// we need to add the silent transition after it
				pnNodeMap.put(branchName, sBranchPlace);
			}else
				sBranchPlace = (Place) pnNodeMap.get(branchName);
			
			net.addArc(sTransition, sBranchPlace);
			
			placeList.add(sBranchPlace);
	   }
		return placeList;
	}
	

	private Place combinePlaces(List<Place> postPlaces) {
		// TODO combine places origPlace, addedPlace
		// we need to give them a name, we could use the combine name in them or we give them a name
		String combineName = ProcessConfiguration.POST_PREFIX;
		
		for(Place p: postPlaces) {
			String tmpName = p.getLabel();
			combineName += tmpName.split("-", 1)[1];
		}
		
		Transition sTransition = null;
		// this is not proper to add this transition, we need to mark this silent transtion here to combine all the  postplaces
		// postPlace

		if(!pnNodeMap.containsKey(combineName)) {
			sTransition = net.addTransition(combineName);
			sTransition.setInvisible(true);
			
			pnNodeMap.put(combineName, sTransition);
		}else {
			sTransition = (Transition) pnNodeMap.get(combineName);
		}
		for(Place p: postPlaces)
			net.addArc(p, sTransition);
		// also one place for silent transition
		
		Place sBranchPlace;
		String branchName = combineName;
		if(!pnNodeMap.containsKey(branchName)) {
			sBranchPlace = net.addPlace(branchName);
			// we need to add the silent transition after it
			pnNodeMap.put(branchName, sBranchPlace);
		}else
			sBranchPlace = (Place) pnNodeMap.get(branchName);
		
		net.addArc(sTransition, sBranchPlace);
		
		return sBranchPlace;
	}

	private PetrinetNode getSourcePlace(List<XORCluster<ProcessTreeElement>> sources) {
		// TODO we search them into the pnMap
		String placeName = null;
		for(XORCluster<ProcessTreeElement> s: sources)
			placeName += s.getLabel();
		
	
		for(String keyName: pnNodeMap.keySet())
			if(placeName.contains(keyName) && placeName.contains(ProcessConfiguration.POST_PREFIX))
				return pnNodeMap.get(keyName);
		
		return null;
	}


	private List<XORCluster<ProcessTreeElement>> getSourceInAnd(XORCluster<ProcessTreeElement> sBranch,
			List<XORCluster<ProcessTreeElement>> sourceWithTarget) {
		// then we check sources in same XOR
		List<XORCluster<ProcessTreeElement>> sourceInXOR = getSourceInXOR(sBranch);
		
		List<XORCluster<ProcessTreeElement>> sourceInAnd = new ArrayList<XORCluster<ProcessTreeElement>>();
		
		for(XORCluster<ProcessTreeElement> s : sourceWithTarget) {
			if(!sourceInXOR.contains(s))
				sourceInAnd.add(s);
		}
		return sourceInAnd;
	}

	private List<XORCluster<ProcessTreeElement>> divideTargetsInXOR(
			List<XORCluster<ProcessTreeElement>> targetWithSource) {
		
		// we need to get the place number by target xor
		List<XORCluster<ProcessTreeElement>> targetXOR = new ArrayList<XORCluster<ProcessTreeElement>>();
		
		for(XORCluster<ProcessTreeElement> tBranch: targetWithSource) {
			// target, we need to get the parent of it 
			XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
			// we need to save the parent xor here 
			if(!targetXOR.contains(parentXOR))
				targetXOR.add(parentXOR);
		}
		
		return targetXOR;
	}

	private List<XORCluster<ProcessTreeElement>> getTargetInAnd(XORCluster<ProcessTreeElement> tBranch,
			List<XORCluster<ProcessTreeElement>> targetXOR) {
		// first get the parent xor and then get the prallel xor
		XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
		List<XORCluster<ProcessTreeElement>> xorInAnd = new ArrayList<XORCluster<ProcessTreeElement>>();
		
		for(XORCluster<ProcessTreeElement> tXOR : targetXOR) {
			if(isXORInAnd(parentXOR, tXOR))
				xorInAnd.add(tXOR);
		}
		
		return xorInAnd;
	}

	private boolean isXORInAnd(XORCluster<ProcessTreeElement> parentXOR, XORCluster<ProcessTreeElement> tXOR) {
		// check if those two xor in parallel, if they are in parallel
		// the least common ancestor is parallel
		Node pNode = (Node)parentXOR.getKeyNode();
		Node tNode = (Node)parentXOR.getKeyNode();
		
		// get the least common ancestor
		Block ltAncestor = (Block)getLeastCommonAncestor(pNode, tNode);
		
		if(isParallel(ltAncestor)) {
			return true;
		}
		return false;
	}

	private boolean isParallel(Block block) {
		// TODO 
		if(block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}

	private Node getLeastCommonAncestor(Node pNode, Node tNode) {
		// TODO how to get the least common ancestor of two node?? 
		// if one is the ancestor of the other, then stop return that one 
		List<Block> pParent = getAncestors(pNode);
		List<Block> tParent = getAncestors(tNode);
		// we need to find them from the end to the start
		int pIdx = pParent.size() -1, tIdx = tParent.size() -1;
		while(pParent.get(pIdx) == tParent.get(tIdx)) {
			pIdx --;
			tIdx --;
		}
		pIdx++;
		
		// it stops when they are not equal, we can put the pIdx and tIdx ++;
		return pParent.get(pIdx);
		
	}

	private List<Block> getAncestors(Node pNode) {
		// here we only have one parent for each node, we can simply it by one
		List<Block> parentList = new ArrayList<Block>();
		Collection<Block> parent = pNode.getParents();
		while(!parentList.containsAll(parent)) {
			parentList.addAll(parent);
			
			
			List<Block> tmpParents = new ArrayList<Block>();
			for(Block block: parent) {
				tmpParents.addAll(block.getParents());
			}
			parent = tmpParents;
			
			if(parent.isEmpty())
				break;
		}
		
		return parentList;
	}

	private List<XORCluster<ProcessTreeElement>> getTargetWithSource(List<XORCluster<ProcessTreeElement>> sourceBranches) {
		// TODO we need to get the target with the same sourceBranches
		List<XORCluster<ProcessTreeElement>> targetList = new ArrayList<XORCluster<ProcessTreeElement>>();
		for(LTRule<XORCluster<ProcessTreeElement>> rule: ruleSet) {
			if(rule.getSources().containsAll(sourceBranches))
				targetList.addAll(rule.getTargets());
		}
		return targetList;
	}
	

	private LTRule<XORCluster<ProcessTreeElement>> findRuleForConn(XORCluster<ProcessTreeElement> sBranch, 
			XORCluster<ProcessTreeElement> tBranch, List<LTRule<XORCluster<ProcessTreeElement>>> connList) {
		// TODO the rule set should be not here but in between of them, so we need one set
		for(LTRule<XORCluster<ProcessTreeElement>> conn: connList) {
			// the condition is not sufficient, if we have parallel, and now we want to combine more
			// let it like this at first and modify it later
			if(conn.getSources().contains(sBranch) && conn.getTargets().contains(tBranch))
				return conn;
			
		}
		return null;
	}

	private List<XORCluster<ProcessTreeElement>> getSourceInXOR(XORCluster<ProcessTreeElement> sBranch) {
		
		XORCluster<ProcessTreeElement> parentXOR = sBranch.getParent();
		return parentXOR.getChildrenCluster();
	}



	private List<XORCluster<ProcessTreeElement>> getSourceWithTarget(List<LTRule<XORCluster<ProcessTreeElement>>> ltConns,
			XORCluster<ProcessTreeElement> tBranch) {
		List<XORCluster<ProcessTreeElement>> sourceNodes = new ArrayList<XORCluster<ProcessTreeElement>>();
		
		for(LTRule<XORCluster<ProcessTreeElement>> conn: ltConns) {
			if(conn.getTargets().contains(tBranch) && !sourceNodes.containsAll(conn.getSources())) {
				sourceNodes.addAll(conn.getSources());
			}
		}

		return sourceNodes;
	}

	
	private List<XORCluster<ProcessTreeElement>> getTargetNodeSet(List<LTRule<XORCluster<ProcessTreeElement>>> ltConns) {
		 
		List<XORCluster<ProcessTreeElement>> targetNodes = new ArrayList<XORCluster<ProcessTreeElement>>();
		
		for(LTRule<XORCluster<ProcessTreeElement>> conn: ltConns) {
			if(!targetNodes.contains(conn.getTargets())) {
				targetNodes.addAll(conn.getTargets());
			}
		}
		
		return targetNodes;
	}



	private List<PetrinetNode> transform2PNNodes(List<ProcessTreeElement> nodeList) {
		// TODO Auto-generated method stub
		List<PetrinetNode> pnNodes = new ArrayList<PetrinetNode>(); 
		for(ProcessTreeElement ptNode: nodeList) {
			pnNodes.add(tnMap.get(ptNode));
		}
		return pnNodes;
	}

}
