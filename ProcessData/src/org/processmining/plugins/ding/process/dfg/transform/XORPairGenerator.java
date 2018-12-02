package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.plugins.ding.process.dfg.model.XORPair;
import org.processmining.plugins.ding.process.dfg.model.XORStructure;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;

/**
 * this class is used to analyse the Process Tree structure, generate the XOR Pair of Process Tree
 * to do it, we need to have a stack to store all those data, and structure
 * maybe we could use the string to represent it 
 * Of course, later we could use it in Petri net analysis. 
 * -- its attributes:
 *    ++ XOR Pair List to use
 *    ++ current state
 * -- 
 *  
 *   
 * @author dkf
 *  02 Dec 2018 modified
 *  -- we use more general structure to deal with parallel situations, but we need to change all the strucuture,
 *    then I think I could try to use the generic type and see what I can get
 */
public class XORPairGenerator {

	List<XORPair<ProcessTreeElement>> pairList;
	
	List<XORClusterPair<ProcessTreeElement>> clusterPairList;
	
	Stack<String> stateStack;
	int numOfXORInStack = 0;
	
	// we use it record current xor stack and current xorBranch objects
	List<XORStructure<ProcessTreeElement>> xorList;
	List<XORBranch<ProcessTreeElement>> xorBranchList;
	// cluster to store the information to it , and also previous one
	
	List<XORCluster<ProcessTreeElement>> clusterList;
	
	public void generatePairs(ProcessTree tree) {
		pairList = new ArrayList<XORPair<ProcessTreeElement>>();
	    clusterPairList = new ArrayList<XORClusterPair<ProcessTreeElement>>();
		stateStack = new Stack<String>();
		xorList = new ArrayList<XORStructure<ProcessTreeElement>>();
		xorBranchList = new ArrayList<XORBranch<ProcessTreeElement>>();
		clusterList =  new ArrayList<XORCluster<ProcessTreeElement>>();
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		if(tree.size() < 1) {
			System.out.println("The tree is empty");
			
		}
		// addXORPairByDFS(tree.getRoot());
		analyzeXORByDFS(tree.getRoot());
		
		// a test to save energy
		if(xorList.isEmpty() || xorList.size() < 2) {
			System.out.println("There is no xor pair for long-term dependency check");
			return;
		}

		// after we organize the xor structure, we create cluster
		Set<Node> aSet = getAllAncestors();
		buildCluster(tree.getRoot(), aSet);
		buildPair((Block)tree.getRoot());
		
	}
	
	public List<XORClusterPair<ProcessTreeElement>> getClusterPair(){
		return clusterPairList;
	}
	
	public List<XORPair<ProcessTreeElement>> getXORPair(){
		return pairList;
	}
	
	/**
	 * the same thing like in the methods before but to organize it, and simplify it only to get the xor structure list
	 * in such situations:
	 *   -- pure or impure xor structure
	 *   -- only sequence relation, we consider it
	 * :: but now we just need to list of xor structure
	 */
	public void analyzeXORByDFS(Node node) {
		if(node.isLeaf()) {
			// System.out.println("visit node : " + node.getName());
			// if there is first node to meet without the xor structure
			if(xorBranchList.isEmpty())
				return;
			
			XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
			// it need to add set the begin and end node into it 
			if(currentXORBranch.getBeginNode() == null) {
				currentXORBranch.setBeginNode(node);
			}
			if(currentXORBranch.isOpen())
				currentXORBranch.setEndNode(node);
		}else {
			Block block =  (Block) node;
			// to record the visit order but only for block..
			// stateStack.push(block.getClass().getSimpleName());
			// System.out.println("visit block name: " + block.getClass().getSimpleName());
			
			// if the block is xor 
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				// if it is in one branch, what to do ?? we need to set the branch somehow special..
				if(!xorBranchList.isEmpty() && xorBranchList.get(xorBranchList.size() - 1).isOpen()) {
					XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
					// it need to add set the begin and end node into it 
					if(currentXORBranch.getBeginNode() == null) {
						currentXORBranch.setBeginNode(node);
					}
					// but after we set it, it has reference in sequence, so we don't need to do this!! 
					// but such branches, we need special care.. At first, we see it 
					if(currentXORBranch.isOpen()) 
						currentXORBranch.setEndNode(node);
				}
				
				XORStructure<ProcessTreeElement> xorStructure = new XORStructure<ProcessTreeElement>(block);
				// add xor in the xorList, we need to consider when we need it
				xorList.add(xorStructure);
				
				// we have all its subNodes
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					// for this branches, we gave different branches, it's true..
					// but one thing to consider is the nested xor structure
					XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>(subNode);
					xorBranchList.add(branch);
					branch.setParentNode(block);
					
					if(subNode.isLeaf()) {
						analyzeXORByDFS(subNode);
						branch.setOpen(false);
						// add some branch here
						xorStructure.addBranch(branch);
					}else {
						xorStructure.setPure(false);
						// so here we need to get the new world somehow.. or loop, we can also use it 
						
						if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
							subNode = addSilentNode((Block)subNode);
						}
						
						analyzeXORByDFS(subNode);
						// we close branch here.. so what to do ?? // if we know the subnode has xor structure?? 
						// it means that we need to get the ancestors at first, but it's not right.. DO we need to assign
						// some special symbol there to make it special??
						branch.setOpen(false);
						// add some branch here
						xorStructure.addBranch(branch);
					}
				}
			}else if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
				// sequence in those situations:
				// -- not in a branch, then it is normal, but how to judge it ?? 
				// -- in a branch, normal or new created
				List<Node> subNodes = block.getChildren();
				if(xorBranchList.isEmpty() || ! xorBranchList.get(xorBranchList.size() - 1).isOpen()) {
					// not in a branch
					for(Node subNode : subNodes) {
						analyzeXORByDFS(subNode);
					}
				}else {
					// in a branch 
					XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
					if(block.getName().contains(ProcessConfiguration.NEW_SEQUENCE)) {
						// in a new sequence but we wait to get more things here
						for(Node subNode : subNodes) {
							analyzeXORByDFS(subNode);
						}
						
					}else {
						// normal situation, but we need to check if we should add silent transitions
						// consider the begin and end for parallel or loop.. 
						// if in sequence, we have xor structure, at begin and end, what to do?? It's in a branch 
						if(subNodes.get(0).getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
							addSilentNode((Block)subNodes.get(0));
						if(subNodes.get(subNodes.size() -1 ).getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) 
							addSilentNode((Block)subNodes.get(subNodes.size() -1 ));
						// for the other situation like xor and loop, we just visit them?? I think we should also give the loop one silent 
						// transitions at begin and end
						for(Node subNode : subNodes) {
							analyzeXORByDFS(subNode);
						}
					}
					currentXORBranch.setOpen(false);
				}
				
			}else { //  if(block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
				// even it's in parallel, it can be a branch or not, if it is in an branch, we need to set the begin and end node of it 
				// but I think, we can transfer it into the parallel
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					analyzeXORByDFS(subNode);
				}
			}
		}
	}


	private XORCluster<ProcessTreeElement> getCluster(Node node) {
		// we check the list of cluster and find the parent
		for(XORCluster<ProcessTreeElement> cluster: clusterList) {
			// here we nned to use another method to check the existence 
			if(cluster.getKeyNode().equals(node))
					return cluster;
			
		}
        System.out.println("couldn't find the xor cluster");
		return null;
	}

	private Block addSilentNode(Block block) {
		// we have such node in the process tree, and we need to return the new created block maybe?? 
		// all the relations from parent, we should keep it
		// we need to keep all the information of it
		String label = block.getClass().getSimpleName();
		Block seqWithSilent = new AbstractBlock.Seq(label +ProcessConfiguration.NEW_SEQUENCE);
		seqWithSilent.setProcessTree(block.getProcessTree());
		block.getProcessTree().addNode(seqWithSilent);
		
		List<Edge> inEdges = block.getIncomingEdges();
        
		for(int i=0; i< inEdges.size();i++) {
			Edge edge = inEdges.get(i);
			// change the target solves it 
			edge.setTarget(seqWithSilent);
			block.removeIncomingEdge(edge);
		}
		// with automatic it means the silent transition
		Node btau = new AbstractTask.Automatic(label+"_Begin_Tau");
		Node etau = new AbstractTask.Automatic(label+"_End_Tau");
		btau.setProcessTree(seqWithSilent.getProcessTree());
		etau.setProcessTree(seqWithSilent.getProcessTree());
		
		seqWithSilent.getProcessTree().addNode(btau);
		seqWithSilent.getProcessTree().addNode(etau);
		seqWithSilent.addChild(btau);
		seqWithSilent.addChild(block);
		seqWithSilent.addChild(etau);
		
		return seqWithSilent;
	}

	private XORStructure<ProcessTreeElement> getXORStructure(Node node) {
		Iterator<XORStructure<ProcessTreeElement>> iter = xorList.iterator();
		while(iter.hasNext()) {
			XORStructure<ProcessTreeElement> xorS =  iter.next();
			if(xorS.getKeyNode() == node)
				return xorS;
		}
		System.out.println("can't find the XOR Structure");
		return null;
	}


	private List<Node> getAncestors(Node currentNode) {
		// TODO Auto-generated method stub
		List<Node> ancestors = new ArrayList<Node>();
		
		while(!currentNode.isRoot()) {
			Block parent =  getParent(currentNode);
			ancestors.add(parent);
			currentNode = parent;
		}
		return ancestors;
	}


	private Set<Node> getAllAncestors(){
		Set<Node> aSet = new HashSet<Node>();
		for(XORStructure<ProcessTreeElement> xorStructure : xorList) {
			aSet.addAll(getAncestors((Node) xorStructure.getKeyNode()));
		}
		return aSet;
	}

	/**
	 * after the last steps, we have all the xor list, now we need to 
	 * @param node
	 * @param aSet
	 * @return
	 */
	private XORCluster<ProcessTreeElement> buildCluster(Node node, Set<Node> aSet) {
		// but now how to generate the pair from clusterList?? 
		Block block = (Block) node;
		// System.out.println("visit block name: " + block.getClass().getSimpleName());
		// we create cluster at beginning of root, which is always the situation, if pair is greater than 1
		XORCluster<ProcessTreeElement> cluster =  new XORCluster<ProcessTreeElement>(block);
		clusterList.add(cluster);
		if(aSet.contains(node)) {
			// ancestors only above the xor structure, not include the xor structure itself. 
			cluster.setHasXOR(true);
		}
		
		List<Node> subNodes = block.getChildren();
		for(Node subNode : subNodes) {
			// at first for the xorCluster
			if(aSet.contains(subNode)) {
				// ancestors with xor structure.. which avoid the xor branches generation.
				// we go deep part, one thing we need to notice is that the ancestors not include itself
				XORCluster<ProcessTreeElement> subCluster = buildCluster(subNode, aSet);
				// here is not enough for us... Because we need to get the relation of children cluster
				subCluster.parentCluster = cluster;
				cluster.addChilrenCluster(subCluster);
			}else if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				// it we meet directly the xor structure, we need to test it
				// we get to the directly xor structure
				XORCluster<ProcessTreeElement> xorCluster = createXORCluster(subNode);
				clusterList.add(xorCluster);
				// but how to know if we should do it again or not?? 
				xorCluster.parentCluster = cluster;
				cluster.addChilrenCluster(xorCluster);
				
			}else if(cluster.isNXORCluster() ){ // if(subNode.isLeaf() && cluster.isXORCluster()) 
				// if this cluster is the xor cluster and it has one branch with leaf node, what to do??
				// we are not sure about the other branches
				// how to know it is branch cluster?? we need to set one mark
				XORCluster<ProcessTreeElement> xorBranchCluster = createXORBranchCluster(subNode);
				clusterList.add(xorBranchCluster);
				// but how to know if we should do it again or not?? 
				xorBranchCluster.parentCluster = cluster;
				cluster.addChilrenCluster(xorBranchCluster);
			}
		}
		return cluster;
	}
	
	private XORCluster<ProcessTreeElement> createXORBranchCluster(Node node) {
		// TODO create one cluster for xor branch, the most important thing is to keep the branches structure of it
		// but to keep the code uniform. we need to create one xor structure to it
		// one to notice is that XORBranchCluster has no xor 
		XORCluster<ProcessTreeElement> xorBranchCluster =  new XORCluster<ProcessTreeElement>(node);
		xorBranchCluster.setBranchCluster(true);
		xorBranchCluster.setHasXOR(false);
		// here there is no xor structure, only the branch, so we need to find out the branch for it 
		// due to it is the branch, so we need to mark it ???
		XORStructure<ProcessTreeElement> xorBranchStructure = new XORStructure<ProcessTreeElement>(node);
		// we need to create the branch of it and then create branch of it
		// which should be in the xorBranchList
		// if it one sequence, then what to do ?? for sequence, how to find it ??
		// we need to give the keyNode,too
		XORBranch<ProcessTreeElement> xorBranch = getXORBranch(node);
		xorBranchStructure.addBranch(xorBranch);
		
		xorBranchCluster.addXORStructure(xorBranchStructure);
		// here we can't make the children cluster null
		// xorCluster.childrenCluster =null;
		return xorBranchCluster;
	}

	private XORBranch<ProcessTreeElement> getXORBranch(Node node) {
		// TODO get the branch due to the xor branches, if like this, what to do then?? 
		Iterator<XORBranch<ProcessTreeElement>> iter = xorBranchList.iterator();
		while(iter.hasNext()) {
			XORBranch<ProcessTreeElement> xorB =  iter.next();
			if(xorB.getKeyNode().equals(node)) {
				return xorB;
			}
		}
		System.out.println("can't find the XOR Structure");
		return null;
	}

	/**
	 * here makes difference if xor is nested, and we need to think about the parallel structure.. 
	 * so we create XORClusterPair here, but still needs to check the availability for it
	 * @param node
	 */
	private void buildPair(Node node) {
		
		XORCluster<ProcessTreeElement> cluster = getCluster(node);
		if(cluster!=null) {
			
			List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<ProcessTreeElement> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				if(!child.isAvailable()) {
					buildPair((Node) child.getKeyNode());
				}
				
			}
			// we need to go the deep down level and get the begin and end xor list from them
			// why can't we goes here directly?? Because the child.isAvailable, why can't it go here?? 
			
			if(cluster.isSeqCluster()) { // all the elements are available, so we just do use it 
				// we get the begin and end list of xor of them 
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
					/*
					List<XORStructure<ProcessTreeElement>> sourceXORList = null;
					List<XORStructure<ProcessTreeElement>> targetXORList = null;
					// should we move to the next element ?? Not really
					XORCluster<ProcessTreeElement> child ;
					sourceXORList = childrenCluster.get(0).getEndXORList();
					int i=1;
					// if there is only one, then what to do ??? we need to keep actually the last node to the next one!!!
					while(i< childrenCluster.size()) {
						child = childrenCluster.get(i);
						targetXORList = child.getBeginXORList();
						 if(!sourceXORList.equals(targetXORList)) {
							 // create from the two xor list
							 pairList.addAll(createPair(sourceXORList, targetXORList));
						 }
						 
						sourceXORList = child.getEndXORList();
						i++;
					}*/
					XORCluster<ProcessTreeElement> sourceCluster, targetCluster;
					sourceCluster = childrenCluster.get(0);
					int i=1;
					// if there is only one, then what to do ??? we need to keep actually the last node to the next one!!!
					while(i< childrenCluster.size()) {
						targetCluster = childrenCluster.get(i);
						if(!sourceCluster.equals(targetCluster)) {
							clusterPairList.add(createClusterXORPair(sourceCluster, targetCluster));
						}
						sourceCluster = targetCluster;
						i++;
					}
				}
			
			}
			// here we check if all of them is available
			cluster.setAvailable(true);	
		}
		
	}
	/**
	 * this creates cluster xor pair which makes it more general, now we need to test if the connection if well.
	 * Do we need to record XORPair in one ClusterPair, if we record it, we can test if this one is complete, 
	 * if one is complete, then we set the completeness in it.
	 * @param sourceCluster
	 * @param targetCluster
	 * @return
	 */
	private XORClusterPair<ProcessTreeElement> createClusterXORPair(
			XORCluster<ProcessTreeElement> sourceCluster, XORCluster<ProcessTreeElement> targetCluster) {
		// TODO Auto-generated method stub
		// we create one pair cluster for it.. But we need to create more of them, I think, if we want to create the children parts of it
		XORClusterPair<ProcessTreeElement> pair = new XORClusterPair<ProcessTreeElement>(sourceCluster, targetCluster);
		// after creating cluster pair, how about the childrenCluster ?? 
		// should we also have many childrenClusterPair ?? If we have, but we need maybe more structure..
		List<XORStructure<ProcessTreeElement>> sourceXORList = null;
		List<XORStructure<ProcessTreeElement>> targetXORList = null;
		if(sourceCluster.isNotNXORCluster() ) {
			// 1.1 if target is not nested cluster 
			if(targetCluster.isNotNXORCluster()) {
				// create the xor pair for them and test the connection of them..
				
				sourceXORList = sourceCluster.getEndXORList();
				targetXORList = targetCluster.getBeginXORList();
				//pair.addAllXORPair(createPair(sourceXORList, targetXORList));
				// pairList.addAll(pair.getXORPairList());
	
				// 1.2 target is parallel 
			}else if(targetCluster.isParallelCluster()) {
				// we get the children cluster and test the number of them??? Acutally, we don't need to do it 
				// We make sure that we only store information of xor, so another branches without xor is ignored
				for(XORCluster<ProcessTreeElement> child: targetCluster.getChildrenCluster()) {
					// we need to create the pair of them 
					pair.addBranchClusterPair(createClusterXORPair(sourceCluster, child));
				}
				
				// 1.3 target is nested xor cluster
			}else if(targetCluster.isNXORCluster()) {
				// even if it's nested xor, so we can have childrencluster for it, so same solution like before
				for(XORCluster<ProcessTreeElement> child: targetCluster.getChildrenCluster()) {
					pair.addBranchClusterPair(createClusterXORPair(sourceCluster, child));
				}
			}
		}else {
			for(XORCluster<ProcessTreeElement> childSource: sourceCluster.getChildrenCluster()) 
				for(XORCluster<ProcessTreeElement> childTarget: sourceCluster.getChildrenCluster()) {
				// we need to create the pair of them 
					pair.addBranchClusterPair(createClusterXORPair(childSource, childTarget));
			}
			
		}
		return pair;
	}

	private List<XORPair<ProcessTreeElement>> createPair(List<XORStructure<ProcessTreeElement>> sourceXORList,
			List<XORStructure<ProcessTreeElement>> targetXORList) {
		
		List<XORPair<ProcessTreeElement>> pList = new ArrayList<XORPair<ProcessTreeElement>>();
		
		for(XORStructure<ProcessTreeElement> xorSource : sourceXORList)
			for(XORStructure<ProcessTreeElement> xorTarget: targetXORList) {
				XORPair<ProcessTreeElement> pair = new XORPair<ProcessTreeElement>();
				pair.setSourceXOR(xorSource);
				pair.setTargetXOR(xorTarget);
				pList.add(pair);
				
			}
			
		return pList;	
	}
	
	private XORCluster<ProcessTreeElement> createXORCluster(Node node) {
		// TODO create the xor cluster to store xor structure
		XORCluster<ProcessTreeElement> xorCluster =  new XORCluster<ProcessTreeElement>(node);
		// here we just get the half structure
		XORStructure<ProcessTreeElement> xorStructure = getXORStructure(node);
		// List<XORStructure<ProcessTreeElement>> tmpXORList = new ArrayList<XORStructure<ProcessTreeElement>>();
		xorCluster.addXORStructure(xorStructure);
		// here we can't make the children cluster null
		// xorCluster.childrenCluster =null;
		return xorCluster;
	}

	private Block getParent(Node currentNode) {
		// TODO Auto-generated method stub
		return currentNode.getParents().iterator().next();
		
	}

	/**
	 * this method is only used to first traverse the process tree and make it work
	 */
	// we need to assign it like in a plugin and read it then do it 
	public static void dfsPreOrder(Node node ) {
		if(node == null) 
			return ;
		if(node.isLeaf()) {
			System.out.println("visit node : " + node.getName());
		}else {
			System.out.println("visit block type name: " + ((Block) node).getClass().getTypeName() );
			System.out.println("visit block name: " + ((Block) node).getClass().getName());
			System.out.println("visit block toString: " + ((Block) node).getClass().toString());
			List<Node> subNodes = ((Block) node).getChildren();
			for(Node subNode: subNodes) {
				dfsPreOrder(subNode);
			}
		}
		
	}

	// after this we need to connect the process tree with petri net
	// after we use the dfg structure, we have it and then we use it to get the pair, what would you like to have here??
	// input the process tree, and petri net ?
	// output, transfered xor pair list?? So input should also have the xor pair??   
	// it is too much to create some transfer, so I just do it at end, 
	// at first to get the LTDepenency from event log and then do it 
	public List<XORPair<PetrinetNode>> transferXORPairFormat(ProcessTree tree, Petrinet net, List<XORPair<ProcessTreeElement>> treePairs){
		List<XORPair<PetrinetNode>> netPairs =  new ArrayList<XORPair<PetrinetNode>>();
		for(XORPair<ProcessTreeElement> tPair: treePairs) {
			
			
		}
		
		return null;
	}
	
}
