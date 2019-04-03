package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.incorporatenegativeinformation.models.LTRule;
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.incorporatenegativeinformation.models.XORClusterPair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

/**
 * this class is special to add long-term dependency on net, which demands
 * parameters of net, process tree, clusterPairList, it is I guess enough for
 * all the things how to process ?? -- get rule set for each cluster pair, I
 * guess??? :: LTRule:: it should store the original and the new ones, so we can
 * divide them easily ++ how to merge those rules?? ++ how to add those rules ??
 * ++ we only have rules in current situations -- add places w.r.t. the rule set
 * in this branch pair. ++ first to get the places to combine by silent
 * transtion ++ add silent transtion and places -- return net.
 * 
 * How to deal with the different branch pair, only the parallel branch needs
 * special attention?? I will organize it again and code it later
 * 
 * @author dkf
 *
 */
public class AddLT2Net {
	// before writing them , design it clearly and then write codes
	Set<LTRule<XORCluster<ProcessTreeElement>>> ruleSet;
	Petrinet net;
	Map<String, PetrinetNode> pnNodeMap;
	Map<Node, Transition> tnMap;
	public AddLT2Net(Petrinet net, ProcessTree tree) {
		this.net = net;
		ruleSet = new HashSet<LTRule<XORCluster<ProcessTreeElement>>>();
		tnMap = NetUtilities.getProcessTree2NetMap(net, tree, null);
	}

	public void initializeAdder() {
		ruleSet.clear();
		pnNodeMap = new HashMap<String, PetrinetNode>();
	}

	public void addLTOnPair(XORClusterPair<ProcessTreeElement> pair) {

		XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
		sourceCluster = pair.getSourceXORCluster();
		targetCluster = pair.getTargetXORCluster();
		// focus on source 
		if (sourceCluster.isPureBranchCluster()) {
			// for pure branch, we shouldn't connect them early...
			if (targetCluster.isPureBranchCluster()) {
				addLTOnPureBranch(pair);
				return;
			}
		}

		if (sourceCluster.isParallelCluster()) {
			// here we need to do what ?? source is parallel
			// simple one parallel one, xor pair, or one pure branch.. 
			addLTOnParallel(pair);
			return;
		} else {
			// can't deal with situation, source is pure but target is xor, and can't go further 
			// this situation is not well dealt... lack of some situations
			for (XORClusterPair<ProcessTreeElement> branchPair : pair.getLtBranchClusterPair()) {
				addLTOnPair(branchPair);
			}
		}
	}

	public void connectSourceWithTarget() {
		for (LTRule<XORCluster<ProcessTreeElement>> rule : ruleSet) {
			if (rule.isLtVisited())
				continue;

			List<XORCluster<ProcessTreeElement>> sourceBranches = rule.getSources();
			String sBranchName = ProcessConfiguration.PLACE_POST_PREFIX + "-";

			for (XORCluster<ProcessTreeElement> sBranch : sourceBranches) {
				String tmpName = sBranch.getLabel();
				sBranchName += tmpName;
			}
			// here we can also change them into HashMap to organize them better, and more solution..
			Place splitPlace = (Place) pnNodeMap.get(sBranchName);
			// get the target with same source
			List<XORCluster<ProcessTreeElement>> targetWithSource = getTargetWithSource(sourceBranches);

			// here we divide them into different xor branches, we could have a list of list
			List<XORCluster<ProcessTreeElement>> targetXOR = divideTargetsInXOR(targetWithSource);
			// after we get the targetXOR, we need to get the and relation of them
			for (XORCluster<ProcessTreeElement> tBranch : targetWithSource) {
				// get all xor cluster which are parallel with this parent xor of tBranch..
				// we get the target in And, but we only need places of xor cluster those
				List<XORCluster<ProcessTreeElement>> xorInAnd = getTargetInAnd(tBranch, targetXOR);
				if (xorInAnd.size() > 1)
					// we need to get the source place from sBranch here
					splitPlaces(splitPlace, xorInAnd);

				XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
				addLTFromXOR2Branch(parentXOR, tBranch, sBranchName);

			}
			rule.setLtVisited(true);
		}
	}

	private void addLTFromXOR2Branch(XORCluster<ProcessTreeElement> parentXOR, XORCluster<ProcessTreeElement> tBranch,
			String sBranchName) {
		// TODO add lt dependency of parent xor and tBranch, we should name the transition better
		// consider different previous condition, we need to set them, anyway, we can delete them later
		// so to find the place for this branch after sBranch, we need to consider the sBranchName with it 
		Place parentPlace = (Place) getSourcePlace(parentXOR, sBranchName); // here we need more information, and also the different parts
		// if parentPlace is null, find the place from sBranchname
		if (parentPlace == null) {
			parentPlace = (Place) pnNodeMap.get(sBranchName);
		}

		// this transition should be connected to the parentXOR, but also the source List
		// if we only use the branch and find out the branch connection, is it enough? 
		// if we meet nested xor structure, then we can't really make it
		String transitionName = ProcessConfiguration.TRANSITION_PRE_PREFIX + "-";
		// we need to do in two situations, one is that we have the branch place, but we shouldn't forget 
		// that the previous node decides the choices here and different choice should be made!!! 
		transitionName += parentXOR.getLabel() + sBranchName.split("-", 2)[1] + tBranch.getLabel();
		Transition sTransition = addTransitionWithTest(transitionName);
		addArcWithTest(parentPlace, sTransition);
		// one problem here is the branch label and single label, difference, so goes back to purebranch
		String branchName = ProcessConfiguration.PLACE_PRE_PREFIX + "-" + tBranch.getLabel();
		Place tBranchPlace = addPlaceWithTest(branchName);
		addArcWithTest(sTransition, tBranchPlace);

	}

	private PetrinetNode getSourcePlace(XORCluster<ProcessTreeElement> parentXOR, String sBranchName) {
		// TODO if they are pure branch, we need to 
		String placeName = parentXOR.getLabel();

		for (String keyName : pnNodeMap.keySet())
			if (keyName.contains(placeName) && keyName.contains(ProcessConfiguration.PLACE_PRE_PREFIX)
					&& keyName.contains(sBranchName.split("-", 2)[1]))
				return pnNodeMap.get(keyName);

		// if we can't find the parentXOR place, it is then in the real branch
		return null;
	}

	// if they are pure branch, there are types: [leaf node, seq, parallel] * [leaf node, seq, parallel]
	// divide them into those types are better to understand codes, right now it is ok??? 
	private void addLTOnPureBranch(XORClusterPair<ProcessTreeElement> branchPair) {
		// add lt on pure branch, we need to test the structure of target
		XORCluster<ProcessTreeElement> sBranch, tBranch;
		sBranch = branchPair.getSourceXORCluster();
		tBranch = branchPair.getTargetXORCluster();

		if (branchPair.isConnected()) {
			// if this branch is connected, we need to add them self for it and keep into place list
			List<LTRule<XORCluster<ProcessTreeElement>>> connRules = branchPair.getLtConnections();
			// those rules only has size 1, just one rule
			ruleSet.addAll(connRules);

			List<PetrinetNode> endNodeList = transform2PNNodes(sBranch.getEndNodeList());
			List<PetrinetNode> beginNodeList = transform2PNNodes(tBranch.getBeginNodeList());

			// we need to deal with the multiple nodes at first
			// create a silent transition and a place for this branch at first
			// and give out the representative
			Transition sTransition = null;
			if (endNodeList.size() > 1) {

				// add one silent transition to combine all the source nodes
				String transtionName = ProcessConfiguration.TRANSITION_POST_PREFIX + "-" + sBranch.getLabel();
				sTransition = addTransitionWithTest(transtionName);

				// create places after each node
				for (PetrinetNode endNode : endNodeList) {
					String keyName = ProcessConfiguration.PLACE_POST_PREFIX + "-" + endNode.getLabel();
					Place postNode = addPlaceWithTest(keyName);
					// here to connect the transition with post place
					addArcWithTest(endNode, postNode);
					addArcWithTest(postNode, sTransition);
				}
			} else {
				// this is equal to thie end node List element
				sTransition = (Transition) endNodeList.get(0);
			}
			// if they are single, then we use branch to represent it 
			String sBranchName = ProcessConfiguration.PLACE_POST_PREFIX + "-" + sBranch.getLabel();
			Place sBranchPlace = addPlaceWithTest(sBranchName);
			addArcWithTest(sTransition, sBranchPlace);

			// create pre places before branch
			Transition tTransition = null;
			if (beginNodeList.size() > 1) {
				// add one silent transition to combine all the target nodes
				String transtionName = ProcessConfiguration.TRANSITION_PRE_PREFIX + "-" + tBranch.getLabel();
				tTransition = addTransitionWithTest(transtionName);
				for (PetrinetNode beginNode : beginNodeList) {
					String keyName = ProcessConfiguration.PLACE_PRE_PREFIX + "-" + beginNode.getLabel();
					Place preNode = addPlaceWithTest(keyName);

					addArcWithTest(preNode, beginNode);
					addArcWithTest(tTransition, preNode);
				}

			} else {
				tTransition = (Transition) beginNodeList.get(0);
			}

			String tBranchName = ProcessConfiguration.PLACE_PRE_PREFIX + "-" + tTransition.getLabel();
			Place tBranchPlace = addPlaceWithTest(tBranchName);
			addArcWithTest(tBranchPlace, tTransition);
		}

	}

	// create one method to add place into net with test
	private Place addPlaceWithTest(String keyName) {
		Place placeNode;
		if (!pnNodeMap.containsKey(keyName)) {
			placeNode = net.addPlace(keyName);
			// we need to add the silent transition after it

			pnNodeMap.put(keyName, placeNode);
		} else
			placeNode = (Place) pnNodeMap.get(keyName);

		return placeNode;
	}

	// create method to add transition into net with test
	private Transition addTransitionWithTest(String transtionName) {
		Transition tTransition = null;
		if (!pnNodeMap.containsKey(transtionName)) {
			tTransition = net.addTransition(transtionName);
			tTransition.setInvisible(true);

			pnNodeMap.put(transtionName, tTransition);
		} else {
			tTransition = (Transition) pnNodeMap.get(transtionName);
		}
		return tTransition;
	}

	private void addArcWithTest(PetrinetNode src, PetrinetNode tgt) {
		// TODO we need to add arc into net but not change the weight on them
		// test the type of src and tgt
		if (src instanceof Place) {
			if (tgt instanceof Transition)
				if (net.getArc(src, tgt) == null)
					net.addArc((Place) src, (Transition) tgt);
		} else if (src instanceof Transition) {
			if (tgt instanceof Place) {
				if (net.getArc(src, tgt) == null)
					net.addArc((Transition) src, (Place) tgt);
			}
		}
	}

	private void addLTOnParallel(XORClusterPair<ProcessTreeElement> pair) {
		// TODO should we return the new added places ??
		XORCluster<ProcessTreeElement> sourceCluster;
		sourceCluster = pair.getSourceXORCluster();
		// should we try to keep only the current targetCluster use??? 

		for (XORClusterPair<ProcessTreeElement> branchPair : pair.getLtBranchClusterPair()) {
			addLTOnPair(branchPair);
		}
		// here we need to check the rules they have, we put rules into ruleSet, image now we only have one pure branch target
		// how many xor branch it has
		List<XORCluster<ProcessTreeElement>> endXORList = sourceCluster.getEndXORList();
		if (endXORList.size() > 1) {
			// here we can finish them on by one, if we use if visited mark?? 
			// use hashMap to group rule set with same target
			HashMap<XORCluster<ProcessTreeElement>, List<LTRule<XORCluster<ProcessTreeElement>>>> ruleGroup = new HashMap();
			for (LTRule<XORCluster<ProcessTreeElement>> rule : ruleSet) {

				XORCluster<ProcessTreeElement> target = rule.getTargets().get(0);
				if (!ruleGroup.containsKey(target)) {
					List<LTRule<XORCluster<ProcessTreeElement>>> tmpRuleList = new ArrayList<>();
					tmpRuleList.add(rule);

					ruleGroup.put(target, tmpRuleList);
				} else {
					ruleGroup.get(target).add(rule);
				}
			}
			//after dealing with one parallel with xor to one branch, consider they come together.. then??
			// we have new added rule with multiple sources and orginal rules have less ones,
			// it's normal, I need to say..

			// after grouping rule later, gather them together, also add new rule into ruleSet
			// for each target, consider the relation of sources of each ruleSet.. 
			for (XORCluster<ProcessTreeElement> target : ruleGroup.keySet()) {
				// get all the rule with same target
				List<LTRule<XORCluster<ProcessTreeElement>>> ruleList = ruleGroup.get(target);

				// for each xor, we find rules of tmpRuleList in them in a List
				// after those we have EndXORList.size list of rules 
				HashMap<XORCluster<ProcessTreeElement>, List<LTRule<XORCluster<ProcessTreeElement>>>> endXORGroup = new HashMap();
				for (LTRule<XORCluster<ProcessTreeElement>> tmpRule : ruleList) {
					XORCluster<ProcessTreeElement> endXOR = getXOR(tmpRule, endXORList);
					if (endXOR != null)
						if (!endXORGroup.containsKey(endXOR)) {
							List<LTRule<XORCluster<ProcessTreeElement>>> tmpRuleList = new ArrayList<>();
							tmpRuleList.add(tmpRule);

							endXORGroup.put(endXOR, tmpRuleList);
						} else {
							endXORGroup.get(endXOR).add(tmpRule);
						}
				}
				if (endXORGroup.isEmpty())
					continue;

				// then we traverse them and combine them together into new rules
				// how to get the list of LTRule from n endXORGroup !! we need to mark the idx of each of them
				// we can't delete them!! Permutation of them but unknown 
				int endXORSize = endXORList.size();
				// create indx for them, but why add them 
				int[] indexes = new int[endXORSize];

				while (true) {
					// construct the new rule here, but at first to merge them together
					List<LTRule<XORCluster<ProcessTreeElement>>> rulesToMerge = new ArrayList<LTRule<XORCluster<ProcessTreeElement>>>();
					for (int i = 0; i < endXORSize; i++) {
						// get all the lists there
						rulesToMerge.add(endXORGroup.get(endXORList.get(i)).get(indexes[i]));
					}

					mergeRules(rulesToMerge);

					// change indexes of them, change from the last one 
					int incrementIdx = endXORSize - 1;
					while (incrementIdx >= 0
							&& (++indexes[incrementIdx]) >= endXORGroup.get(endXORList.get(incrementIdx)).size()) {
						indexes[incrementIdx] = 0;
						incrementIdx--;
					}
					if (incrementIdx < 0)
						break;
				}

				// delete the old rule
				deleteOldRules(ruleList);
			}
		}

	}

	private void deleteOldRules(List<LTRule<XORCluster<ProcessTreeElement>>> ruleList) {
		ruleSet.removeAll(ruleList);
	}

	private void mergeRules(List<LTRule<XORCluster<ProcessTreeElement>>> rulesToMerge) {
		// first to merge places
		// for all sources we find the places from them, we then combine with places there

		List<Place> postPlaces = new ArrayList<Place>();
		// second to create a new rule with it 
		LTRule<XORCluster<ProcessTreeElement>> newRule = new LTRule<XORCluster<ProcessTreeElement>>();
		for (LTRule<XORCluster<ProcessTreeElement>> tmpRule : rulesToMerge) {
			newRule.addRuleSourceList(tmpRule.getSources());

			Place sPlace = (Place) getSourcePlace(tmpRule.getSources());
			postPlaces.add(sPlace);
		}

		combinePlaces(postPlaces);

		newRule.addRuleTargetList(rulesToMerge.get(0).getTargets());
		ruleSet.add(newRule);
	}

	private XORCluster<ProcessTreeElement> getXOR(LTRule<XORCluster<ProcessTreeElement>> tmpRule,
			List<XORCluster<ProcessTreeElement>> endXORList) {
		// TODO get the endXOR in endXORList which includes tmpRule sources
		// sources are in a list, if we can visit all the sources in endXOR in tree
		// we return this xor 
		boolean inXOR = true;
		for (XORCluster<ProcessTreeElement> endXOR : endXORList) {
			// if we just stop at branch, not really, because we have multiple sources
			inXOR = true;
			for (XORCluster<ProcessTreeElement> source : tmpRule.getSources()) {
				if (!isInclude(endXOR, source)) {
					// if endXOR include source
					inXOR = false;
					break;
				}
			}
			// after all of those, we return endXOR
			if (inXOR) {
				return endXOR;
			}
		}
		return null;
	}

	private boolean isInclude(XORCluster<ProcessTreeElement> endXOR, XORCluster<ProcessTreeElement> source) {
		// TODO how to check if this endXOR include this source element
		// they can be a long history
		Node xor = (Node) endXOR.getKeyNode();
		Node child = (Node) source.getKeyNode();
		List<Node> ancestorList = getAncestors(child);
		if (ancestorList.contains(xor))
			return true;

		return false;
	}

	private List<Place> splitPlaces(Place splitPlace, List<XORCluster<ProcessTreeElement>> xorInAnd) {
		// after this splitPlace, we xor list happen in parallel

		List<Place> placeList = new ArrayList<Place>();
		String transitionName = ProcessConfiguration.TRANSITION_POST_PREFIX + "-"
				+ splitPlace.getLabel().split("-", 2)[1];

		// we give it the name of target label, but because it connects to source, we need to add the source effect
		for (XORCluster<ProcessTreeElement> xor : xorInAnd) {
			transitionName += xor.getLabel();
		}

		Transition sTransition = addTransitionWithTest(transitionName);
		// connect them together
		addArcWithTest(splitPlace, sTransition);

		// here we need to mark it they are from split place here
		String placeName = ProcessConfiguration.PLACE_PRE_PREFIX + "-" + splitPlace.getLabel().split("-", 2)[1];
		for (XORCluster<ProcessTreeElement> xor : xorInAnd) {
			// generate the place for it 
			String branchName = placeName + xor.getLabel();
			Place sBranchPlace = addPlaceWithTest(branchName);
			addArcWithTest(sTransition, sBranchPlace);

			placeList.add(sBranchPlace);
		}
		return placeList;
	}

	private Place combinePlaces(List<Place> postPlaces) {
		// TODO combine places origPlace, addedPlace
		// we need to give them a name, we could use the combine name in them or we give them a name
		String combineName = ProcessConfiguration.TRANSITION_POST_PREFIX + "-";
		String branchName = ProcessConfiguration.PLACE_POST_PREFIX + "-";
		for (Place p : postPlaces) {
			String tmpName = p.getLabel();
			combineName += tmpName.split("-", 2)[1];
			branchName += tmpName.split("-", 2)[1];
		}

		Transition sTransition = addTransitionWithTest(combineName);

		for (Place p : postPlaces)
			addArcWithTest(p, sTransition);

		// also one place for silent transition
		Place sBranchPlace = addPlaceWithTest(branchName);
		addArcWithTest(sTransition, sBranchPlace);
		return sBranchPlace;
	}

	private PetrinetNode getSourcePlace(List<XORCluster<ProcessTreeElement>> sources) {
		// TODO we search them into the pnMap
		String placeName = ProcessConfiguration.PLACE_POST_PREFIX + "-";
		for (XORCluster<ProcessTreeElement> s : sources)
			placeName += s.getLabel();

		for (String keyName : pnNodeMap.keySet())
			if (keyName.equals(placeName))
				return pnNodeMap.get(keyName);

		return null;
	}

	private List<XORCluster<ProcessTreeElement>> divideTargetsInXOR(
			List<XORCluster<ProcessTreeElement>> targetWithSource) {

		// we need to get the place number by target xor
		List<XORCluster<ProcessTreeElement>> targetXOR = new ArrayList<XORCluster<ProcessTreeElement>>();

		for (XORCluster<ProcessTreeElement> tBranch : targetWithSource) {
			// target, we need to get the parent of it 
			XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
			// we need to save the parent xor here 
			if (!targetXOR.contains(parentXOR))
				targetXOR.add(parentXOR);
		}

		return targetXOR;
	}

	private List<XORCluster<ProcessTreeElement>> getTargetInAnd(XORCluster<ProcessTreeElement> tBranch,
			List<XORCluster<ProcessTreeElement>> targetXOR) {
		// first get the parent xor and then get the prallel xor
		XORCluster<ProcessTreeElement> parentXOR = tBranch.getParent();
		List<XORCluster<ProcessTreeElement>> xorInAnd = new ArrayList<XORCluster<ProcessTreeElement>>();

		for (XORCluster<ProcessTreeElement> tXOR : targetXOR) {
			if (!xorInAnd.contains(tXOR))
				if (parentXOR.equals(tXOR) || isXORInAnd(parentXOR, tXOR))
					xorInAnd.add(tXOR);
		}

		return xorInAnd;
	}

	private boolean isXORInAnd(XORCluster<ProcessTreeElement> parentXOR, XORCluster<ProcessTreeElement> tXOR) {
		// check if those two xor in parallel, if they are in parallel
		// the least common ancestor is parallel
		Node pNode = (Node) parentXOR.getKeyNode();
		Node tNode = (Node) tXOR.getKeyNode();

		// get the least common ancestor
		Block ltAncestor = (Block) getLeastCommonAncestor(pNode, tNode);

		if (isParallel(ltAncestor)) {
			return true;
		}
		return false;
	}

	private boolean isParallel(Block block) {
		// TODO 
		if (block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}

	private Node getLeastCommonAncestor(Node pNode, Node tNode) {
		// TODO how to get the least common ancestor of two node?? 
		// if one is the ancestor of the other, we should include element itself
		List<Node> pParent = getAncestors(pNode);
		List<Node> tParent = getAncestors(tNode);
		// we need to find them from the end to the start
		int pIdx = pParent.size() - 1, tIdx = tParent.size() - 1;
		while (pParent.get(pIdx) == tParent.get(tIdx)) {
			pIdx--;
			tIdx--;
			if (pIdx < 0 || tIdx < 0)
				break;
		}
		pIdx++;

		// it stops when they are not equal, we can put the pIdx and tIdx ++;
		return pParent.get(pIdx);

	}

	private List<Node> getAncestors(Node pNode) {
		// here we only have one parent for each node, we can simply it by one
		List<Node> parentList = new ArrayList<Node>();
		parentList.add(pNode);
		Collection<Block> parent = pNode.getParents();
		while (!parentList.containsAll(parent)) {
			parentList.addAll(parent);

			List<Block> tmpParents = new ArrayList<Block>();
			for (Block block : parent) {
				tmpParents.addAll(block.getParents());
			}
			parent = tmpParents;

			if (parent.isEmpty())
				break;
		}

		return parentList;
	}

	private List<XORCluster<ProcessTreeElement>> getTargetWithSource(
			List<XORCluster<ProcessTreeElement>> sourceBranches) {
		// TODO we need to get the target with the same sourceBranches
		List<XORCluster<ProcessTreeElement>> targetList = new ArrayList<XORCluster<ProcessTreeElement>>();
		for (LTRule<XORCluster<ProcessTreeElement>> rule : ruleSet) {
			if (rule.getSources().containsAll(sourceBranches)) {
				rule.setLtVisited(true);
				targetList.addAll(rule.getTargets());
			}
		}
		return targetList;
	}

	private Set<XORCluster<ProcessTreeElement>> getTargetNodeSet(
			Set<LTRule<XORCluster<ProcessTreeElement>>> ruleSetList) {

		Set<XORCluster<ProcessTreeElement>> targetNodes = new HashSet<XORCluster<ProcessTreeElement>>();
		// something not so well, because here the targets are only one branch !
		for (LTRule<XORCluster<ProcessTreeElement>> conn : ruleSetList) {
			targetNodes.addAll(conn.getTargets());
		}

		return targetNodes;
	}

	public  List<PetrinetNode> transform2PNNodes(List<ProcessTreeElement> nodeList) {
		// TODO Auto-generated method stub
		List<PetrinetNode> pnNodes = new ArrayList<PetrinetNode>();
		for (ProcessTreeElement ptNode : nodeList) {
			pnNodes.add(tnMap.get(ptNode));
		}
		return pnNodes;
	}

}
