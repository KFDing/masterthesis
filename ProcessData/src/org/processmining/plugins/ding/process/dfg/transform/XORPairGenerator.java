package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
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
 * -- we use the preorder DFS to traverse it and generate such pair
 *   ++ one thing to remember that the branches can be in different xor structure
 *   
 * @author dkf
 *
 */
public class XORPairGenerator {

	List<XORPair<ProcessTreeElement>> pairList;
	Stack<String> stateStack;
	int numOfXORInStack = 0;
	
	// we use it record current xor stack and current xorBranch objects
	List<XORStructure<ProcessTreeElement>> xorList;
	List<XORBranch<ProcessTreeElement>> xorBranchList;
	// cluster to store the information to it , and also previous one
	
	List<XORCluster<ProcessTreeElement>> clusterList;
	
	public List<XORPair<ProcessTreeElement>> generateXORPairs(ProcessTree tree) {
		pairList = new ArrayList<XORPair<ProcessTreeElement>>();
	
		stateStack = new Stack<String>();
		xorList = new ArrayList<XORStructure<ProcessTreeElement>>();
		xorBranchList = new ArrayList<XORBranch<ProcessTreeElement>>();
		clusterList =  new ArrayList<XORCluster<ProcessTreeElement>>();
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		if(tree.size() < 1) {
			System.out.println("The tree is empty");
			return null; 
		}
		// addXORPairByDFS(tree.getRoot());
		analyzeXORByDFS(tree.getRoot());

		return pairList;
	}
	
	/**
	 * try to simplify the codes !! We only need to test what we meet, 
	 * if it is a leaf,  or it is a block; 
	 * 
	 * we can only create one xor structure list and then combine them together, 
	 * but we need to consider different structure effect.. Like if it is in an parallel structure 
	 * we need to create the special pairs already, but the others, do we need to some order to create them?? 
	 * like in sequence, we need to have queue to deal with it ?? 
	 * we need to get the first and then the next, so we need the Queue to have it!!
	 * 
	 * if it's a leaf, what to?? Should we put it into one xor branch?? or not?? 
	 * if current state is in a branch , we should put it into one xor branch, 
	 * if not, but the current xor branch is still open
	 *    -- sequence :: 
	 *         if the  only pure structure, then we only decide to use the first and last node
	 *         but if the sequence includes an xor structure
	 *         should we delete the branch created before somehow?
	 *         no !!! it's much complex.. 
	 *         // we have parent node, so we can have the test, if the branch is still 
	 *    -- parallel ::
	 *    -- loop:: 
	 * 
	 * if it is a block, what to do next??  after each block, we need to clean it up
	 * clean means:: we need to pop the states from it;;  after each block we need to do it
	 * if the block is xor structure, we create the xor structure into the list and create list from it
	 *   we also need to test this xor structure to check where it is when we try to extract it 
	 *     -- xor:: if it is in xor structure, then we create xor context, 
	 *              it we merge the new xor strucuture into current xor structure
	 *     -- sequence:: if it is in sequence, anyway, [we should set one xor branch somehow, 
	 *              to check if one xor structure is closed or not, if it is closed; get it out;
	 *              if it is still open, then we need to merge the xor structure below it;; which is actually 
	 *              some iteration, needs work
	 *     -- parallel:: we need to check it during we visit subNodes ;
	 *              if we have only one xor branch in the parallel, then we treat it as usual
	 *              if we have more, then we need to build the relation of them, which we count the number 
	 *              of all branches, then try to extracePair from them after the checking 
	 *     -- loop:: consider it later
	 * if it is not an xor structure, we need to test,
	 *  if it is in an xor branch, then we need to check the structure it has ::
	 *     -- sequence::  we visit it and find the fist and last node as the begin and end branach node
	 *     -- parallel::  we should add the tau nodes in the begin and end of parallel, 
	 *                   it means that we create new structure of it !!  
	 *                   change parallel into sequence and then add two tau in and after it
	 *     -- loop:: we add same into it, one sequence with two silents transitions into it
	 *
	 * @param node
	 */
	public void addXORPairByDFS(Node node ) {
		
		if(node.isLeaf()) {
			System.out.println("visit node : " + node.getName());
			// here we need to add some judgement to test if the last block has already ended. 
			Block parent = (Block) getParent(node);
			
			// it is in an xor structure , get the current last index
			if(parent.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
				// it need to add set the begin and end node into it 
				if(currentXORBranch.getBeginNode() == null) {
					currentXORBranch.setBeginNode(node);
					currentXORBranch.setEndNode(node);
				}
				
			}else {
				// if the leaf node is in sequence
				if(parent.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
					XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
					// it can be after the first creation
					if(currentXORBranch.getBeginNode() == null)
						currentXORBranch.setBeginNode(node);
					
					if(currentXORBranch.isOpen()) {
						currentXORBranch.setEndNode(node);
						// we can only close this branch, until we reach the end to sequence structure 
						// but if we meet some thing else but is not the node... we check it later in sequence
					}
				}else if(parent.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
					// in parallel structure, we do nothing.. right?? 
					System.out.println("parallel : left to add parallel structure into the process tree");
					// if the parallel is in an xor branch, what to do??? we need to add one silent transition 
					// before and after it !!! Remember it is in a parallel structure directly beginning node!!! 
					XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
					// this parent parallel in an xor branch !! we need to do what ??? 
					// it is directly, but if not direcly what to do ?? But check later!! 
					if(currentXORBranch.getBeginNode() == null) {
						// add silent transition before it and then connect to it 
						Block sBlock = addSilentNode(parent);
						// after we change the parallel structure, we need to go back to the parent structure
						// but we can't use this again as one condition to add it, because 
						// we have actually seq in it but still not close, then we need to ??? 
						addXORPairByDFS(sBlock);
					}
					
				}else if(parent.getClass().getSimpleName().equals(ProcessConfiguration.LOOP)){
					System.out.println("loop : left to add another structure into the process tree in loop");
					
					XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
					
					if(currentXORBranch.getBeginNode() == null) {
						// add silent transition before it and then connect to it 
						Block sBlock = addSilentNode(parent);
						// after we change the parallel structure, we need to go back to the parent structure
						// but we can't use this again as one condition to add it, because 
						// we have actually seq in it but still not close, then we need to ??? 
						addXORPairByDFS(sBlock);
					}
				}
			}
			
		}else { // node is a block
			Block block =  (Block) node;
			System.out.println("visit block name: " + block.getClass().getSimpleName());
			stateStack.push(block.getClass().getSimpleName());
			// it is an xor block
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				// if the block is in an xor structure 
				Block parent = (Block) getParent(block);
				// we get the parent cluster and then assign to it 
				XORCluster<ProcessTreeElement> currentCluster =  getCluster(parent);
				
				XORStructure<ProcessTreeElement> xorStructure = new XORStructure<ProcessTreeElement>(block);
				xorList.add(xorStructure);
				// get it later, maybe better
				currentCluster.addXORStructure(xorStructure);
				
				// xor direct parent can't be xor structure !!! Else they could merge together automatically!! 
				// xor in a sequence structure 
				if(parent.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
					
					List<Node> subNodes = block.getChildren();
					for(Node subNode : subNodes) {
						XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>();
						xorBranchList.add(branch);
						stateStack.push(ProcessConfiguration.XOR_BRANCH);
						addXORPairByDFS(subNode);
						branch.setOpen(false);
					}
					// put all the branch into the xorstructure, we need to change it!!! 
					extractXORBranch(xorStructure); // to fill it, it's all right!!
					
				}else if(parent.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)){ 
					xorStructure.setParent(parent);
					
					// we set the same type here tp give it
					List<Node> subNodes = block.getChildren();
					for(Node subNode : subNodes) {
						XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>();
						xorBranchList.add(branch);
						
						addXORPairByDFS(subNode);
						
						branch.setOpen(false);
					}
					// put all the branch into the xorstructure, we need to change it!!! 
					extractXORBranch(xorStructure);
					
				}
			}else  {
				// 
				// we see the pure, maybe the sequence structure
				// we see sequence here, we need to set one xorList for it to store all xor structures
				// but how?? we could use the stateSteak to push and pop them, but then later
				// but how to combiine it with other structure it's a question, so we need XORCluster here!!! 
				// we use the seqCluster to store the xor in current cluster 
				if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
					XORCluster<ProcessTreeElement> seqCluster = new XORCluster<ProcessTreeElement>(block);
					clusterList.add(seqCluster);
					
					List<Node> subNodes = block.getChildren();
					for(Node subNode : subNodes) {
						// here we get the parent of last node, or somehow.. But better what I think is to do it here, so we can save codes
						// for the judges and add the elements.. But current Cluster, we need anyway.. 
						// we judge it here, if it is an xor subNode, if it is, we create the xor node structure for it
						// maybe too much, now only concentrate on it 
						addXORPairByDFS(subNode);
					}
				
				}
				
			}
		}
		
	}
	/**
	 * the same thing like in the methods before but to organize it
	 */
	public void analyzeXORByDFS(Node node) {
		if(node.isLeaf()) {
			System.out.println("visit node : " + node.getName());
			
			XORBranch<ProcessTreeElement> currentXORBranch =  xorBranchList.get(xorBranchList.size() - 1);
			// it need to add set the begin and end node into it 
			if(currentXORBranch.getBeginNode() == null) {
				currentXORBranch.setBeginNode(node);
				
			}
			currentXORBranch.setEndNode(node);
			
		}else {
			Block block =  (Block) node;
			System.out.println("visit block name: " + block.getClass().getSimpleName());
			
			// if the block is xor 
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				XORStructure<ProcessTreeElement> xorStructure = new XORStructure<ProcessTreeElement>(block);
				// add xor in the xorList, we need to consider when we need it
				xorList.add(xorStructure);
				// we have all its subNodes
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					if(subNode.isLeaf()) {
						// here we are doing node self working here, it's actually not well!!!! 
						// if we borrow the power from the parent it's also not so nice
						XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>();
						xorBranchList.add(branch);
						analyzeXORByDFS(subNode);
						branch.setOpen(false);
						// add some branch here
						xorStructure.addBranch(branch);
					}else {
						xorStructure.setPure(false);
						// so here we need to get the new world somehow.. ok
						if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
							subNode = addSilentNode((Block)subNode);
						}
						// then the subnode is in seq not in xor anymore
						analyzeXORByDFS(subNode);
						// if they have something, we could build cluster of them
						XORCluster<ProcessTreeElement> cluster = (XORCluster<ProcessTreeElement>) getLastElement(clusterList);
						
						// after this analysis, we check the cluster if it has xor in it
						// if not with xor, so we create another branches on it 
						if(! cluster.hasXOR()) {
							// if it is seq, then we use the first and end of it, 
							// if it is a parallel, we need to create something else
							// if it is loop, consider later
							// we could set in later and only to get it easier her
							XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>();
							branch.setBeginNode(cluster.getBeginNode());
							branch.setEndNode(cluster.getEndNode());
							branch.setOpen(false);
							
							xorStructure.addBranch(branch);
						}else {
							// if it has cluster, what to do ??, do nothing, I guess 
							// we only keep it somehow here, or we should remove it ?? THis way
							
						}
					}
				}
				
			}else if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
				// we only consider the sequence with xor structure... anyway
				XORCluster<ProcessTreeElement> cluster = new XORCluster<ProcessTreeElement>(block);
				clusterList.add(cluster);
				
				// we need to set the begin and end node, if it has no xor structure, we need to get from the children cluster
				List<Node> subNodes = block.getChildren();
				int i = 0;
				Node subNode = null;
				
				XORCluster<ProcessTreeElement> currentCluster = null ;
				for( ; i < subNodes.size() ; i++) {
					// we don't really consider the begin and end node of them
					subNode = subNodes.get(i);
					
					if(subNode.isLeaf())
						continue;
					if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
						cluster.setHasXOR(true);
						analyzeXORByDFS(subNode);
						cluster.addXORStructure((XORStructure<ProcessTreeElement>) getLastElement(xorList));
					}else {
						if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL) && 
								! block.getName().contains(ProcessConfiguration.NEW_SEQUENCE)) {
							// it's already the new generated sequence, so we don't need something special about it
							// we don't need to do it, else we might need it
							subNode = addSilentNode((Block)subNode);
						}
						
						analyzeXORByDFS(subNode);
						// we could ge the cluster, but how to set that it has something to du with xor ?? 
						// it's not pure either!!! 
						currentCluster =  (XORCluster<ProcessTreeElement>) getLastElement(clusterList);
						// it's on the top of such structure
						cluster.setHasXOR(currentCluster.hasXOR());

					}
					
				}
				// at end we assign the begin and node 
				if(!cluster.hasXOR()) {
					Node beginNode = subNodes.get(0);
					if(beginNode.isLeaf())
						cluster.setBeginNode(subNodes.get(0));
					else {
						// find the cluster with it this node
						XORCluster<ProcessTreeElement> firstSubCluster = getCluster(subNodes.get(0));
						cluster.setBeginNode(firstSubCluster.getBeginNode());
					}
					Node endNode = (Node)getLastElement(subNodes);
					if(endNode.isLeaf()) {
						// if it is a leave, if not we need to find another way.
						cluster.setEndNode(subNode);
					}else {
						XORCluster<ProcessTreeElement> endSubCluster = getCluster(subNodes.get(0));
						cluster.setEndNode(endSubCluster.getEndNode());
					}
					
				}	
				
				
			}else if(block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
				// if we need to add silent transiton on the parallel, then we add silent transition on it 
				// in which situation, we need it ??? When we need the begin and end node of parallel, we need to set it 
				// only if we need it, then we create it, else we don't really need to care about it
				XORCluster<ProcessTreeElement> cluster = new XORCluster<ProcessTreeElement>(block);
				clusterList.add(cluster);
				
				// here we see differently.. we need one control parameter to use it 
				// we can delete the silen transition later, so we can create it here.. 
				// if it has xor structure, do we need to create the begin adn endNode ?? 
				List<Node> subNodes = block.getChildren();
				int i = 0;
				Node subNode = subNodes.get(i);
				
				XORCluster<ProcessTreeElement> currentCluster = null ;
				for( ; i < subNodes.size() - 1; i++) {
					// we don't really consider the begin and end node of them
					if(subNode.isLeaf())
						continue;
					if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
						cluster.setHasXOR(true);
						analyzeXORByDFS(subNode);
						cluster.addXORStructure((XORStructure<ProcessTreeElement>) getLastElement(xorList));
					}else {
						// if it is leave or if it it seq or parallel, we need it here
						analyzeXORByDFS(subNode);
						// we could ge the cluster, but how to set that it has something to du with xor ?? 
						// it's not pure either!!! 
						currentCluster =  (XORCluster<ProcessTreeElement>) getLastElement(clusterList);
						// it's on the top of such structure
						if(currentCluster.hasXOR()) {
							// it the currentCluster has xor and belongs to children of cluster
							cluster.addChilrenCluster(currentCluster);
							cluster.setHasXOR(true);
						// we make parallel without begin and end node
						}
					}
				}
			}else {
				System.out.println("case fort the loop");
			}
		}
	}


	private Object getLastElement(List xlist) {
		// TODO Auto-generated method stub
		return xlist.get(xlist.size() - 1);
	}

	private XORCluster<ProcessTreeElement> getCluster(Node node) {
		// we check the list of cluster and find the parent
		for(XORCluster<ProcessTreeElement> cluster: clusterList) {
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
		Block seqWithSilent = new AbstractBlock.Seq(label +"_New_Seq");
		seqWithSilent.setProcessTree(block.getProcessTree());
		block.getProcessTree().addNode(seqWithSilent);
		
		List<Edge> inEdges = block.getIncomingEdges();
        
		for(int i=0; i< inEdges.size();i++) {
			Edge edge = inEdges.get(i);
			// change the target solves it 
			edge.setTarget(seqWithSilent);
		}
		
		Node btau = new AbstractTask.Automatic(label+"_Begin_Tau");
		Node etau = new AbstractTask.Automatic(label+"_End_Tau");
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

	private Node getParent(Node node) {
		// return the same context parent
		Collection<Block> parents = node.getParents();
		
		return parents.iterator().next();
	}

	private List<XORStructure<ProcessTreeElement>> sourceXORForPair;
	// this return pairList of xor structures
	private void buildXORPairbyDFS(Block block) {
		
		// we need to revise the tree again, until we find the parts which we need, but in process tree
		// or the new cluster,we still need the node into it, but now we could somehow simpler
		XORCluster<ProcessTreeElement> cluster = getCluster(block);
		if(!cluster.hasXOR()) {
			// cluster has no xor, we continue
		}else {
			// whatever here it is about the sequence, but whatever, we could get the begin and then later stuff, about it
			// one thing is not so clear is the begin substructure makes it annoying, so we drop this methods 
			// to consider this one!!
			// until we reach the pure structure of them
			
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
			
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					if(!subNode.isLeaf()) {
						// only consider the middle node
						if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
							// we meet the xor subNode, then we need to consider in which situation it is
							if(sourceXORForPair.isEmpty()) {
								sourceXORForPair.add(cluster.getXOR(subNode));// how to get the xor structure of this one?? 
							}
							// here we create pair with sourceXORForPair?? 
							// we are in the sequence list, so we can do it 
							XORStructure<ProcessTreeElement> targetXOR = null;
							for(XORStructure<ProcessTreeElement> sourceXOR: sourceXORForPair) {
									targetXOR = cluster.getXOR(subNode);
									if(sourceXOR != targetXOR )
										pairList.add(createXORPair(sourceXOR, targetXOR));
							}
							// after this, we set the sourceXORForPair into something else.. 
							sourceXORForPair.clear();
							sourceXORForPair.add(targetXOR);
							
						}else {
							XORCluster<ProcessTreeElement> child = getCluster(subNode);
							if(!child.hasXOR()) {
								continue;
							}else {
								// if the subNode has xor node, it is parallel or loop with xor 
								buildXORPairbyDFS((Block)subNode);
							}
						}
					}
					
				}
			}
			
		}
		
		
		
		
		if(cluster.getBeginXORList().size() > 0) {
			// what if the begin is an cluster with xor, so we can't use the beginXORList
			// it's not effecitve
			if(sourceXORForPair == null)
				sourceXORForPair = cluster.getBeginXORList();
			else {
				for(XORStructure<ProcessTreeElement> sourceXOR: sourceXORForPair) {
					for(XORStructure<ProcessTreeElement> targetXOR: cluster.getBeginXORList() )
						pairList.add(createXORPair(sourceXOR, targetXOR));
				}
				
			}
		
		}
		// we could have some xor in the children cluster, so we can't avoid it
		
		
		
		if(cluster.getXorList().size()>1 && cluster.getChildrenCluster().size() < 1) {
			pairList.addAll(cluster.createSpecialPair());
		}
		// we get the current xor list to make pair with the current cluster, they have the same level
		// we use it and then we delete it from sourceXORForPair, until we meet another one
		
		// after this, we need to set the sourceXORForPair by using the endXORList
		
		// if it's not
		
		// if it has chilrenCluster and also the xorList > 1, so what to do ??
		// we will set the pairList with not finishes parts here, but how to regonize 
		// which parts to which?? Like parallel includes the xor 
		if(cluster.hasXOR()) {
			
			if(cluster.getXorList().size() < 1) {
				// if its children has the subcluster
				List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
				for(XORCluster<ProcessTreeElement> child: childrenCluster) {
					buildXORPairbyDFS((Block)child.getKeyNode());
				}
				
			}else {
				// it includes at least one xor structure
				// so we can create an pair to connect to the next xor structure
				List<XORStructure<ProcessTreeElement>> beginXorList = cluster.getBeginXORList();
				for(XORStructure<ProcessTreeElement> beginXOR : beginXorList) {
					// we could get the current not complete pair and then assign them together?? 
					// or we get the xorList?? // better to get xor list
				}
				
				
			}
			 
			// we check the childenCluster, if we only have the xor structure
			// we have parellel in seq, and then we check it, we see th
			pairList.addAll(cluster.createSpecialPair());
			// after this we need to check if it direct includes the xor, or just the subnode?? 
			
		}
		// or we make the pair w.r.t. the xorList 
		// we still visit the node but we begin from the root node. 
		
	}

	private XORPair<ProcessTreeElement> createXORPair(XORStructure<ProcessTreeElement> xorSource,
			XORStructure<ProcessTreeElement> xorTarget) {
		XORPair<ProcessTreeElement> pair = new XORPair<ProcessTreeElement>();
		pair.setSourceXOR(xorSource);
		pair.setTargetXOR(xorTarget);
		return pair;
	}

	private void extractXORBranch(XORStructure<ProcessTreeElement> xorStructure) {
		// here we check if one xor structure has all the branches '
		// if it is then we transfer those branches into the xor structure
		// if we could make sure that all the branches there belong to the same xor structure, it's fine
		// but we can't , so we need to think of another methods to change it..
		// we put all the same branches into it, so we extract it.. That's fine
		// but before we check our states
		// if we have finished all the xor branch traverse, we could release it
		if(xorBranchList.isEmpty()) {
			System.out.println("xor branches it's empty, not used to extrac");
			return;
		}
		int i = xorBranchList.size() ;
		// for the queue, we need to do something more, we need more flexible structure
		// so we change the Queue to list 
		while(i > 0) {
			// we get the closed node belongs to the same xor structure.. Anyway, only after the visit 
			// but the xorBranchList is only used for one xor structure, else, we can't do it 
			// if it has branch from another, we can't differ them.. Bad
			// do we need the stateStack to store the information to only one xor structure ??  
			// I think we need it !!
			i--;
			if(stateStack.peek().equals(ProcessConfiguration.XOR_BRANCH)) {
				xorStructure.addBranch(xorBranchList.get(i));
				stateStack.pop();
			}else {
				break;
			}
		}
		// we have reached the top of xor structure and then we need to go back 
		xorStructure.setOpen(false);
	}

	// get the state before the current state
	private String getSecondState() {
		if(stateStack.peek().equals(ProcessConfiguration.XOR_BRANCH) || 
				stateStack.peek().equals(ProcessConfiguration.XOR)) {
			// it means that we have met one xor before, now we need to get the structure
			// before the xor structure
			int i = stateStack.size();
			while( i>0) {
				i--;
				if(stateStack.get(i).equals(ProcessConfiguration.XOR_BRANCH) ||
						stateStack.get(i).equals(ProcessConfiguration.XOR))
					continue;
				else
					break;
			}
			return stateStack.get(i);
			
		}else {
			// do we need to get the second state, yes, we still need it 
			int i = stateStack.size() - 2;
			if(i<0) {
				System.out.println("index of state stack is below 0, visiting time bad");
				return "";
			}
			if(!(stateStack.get(i).equals(ProcessConfiguration.XOR) ||
					stateStack.get(i).equals(ProcessConfiguration.XOR_BRANCH))) {
				return stateStack.get(i);
			}else {
				return ProcessConfiguration.XOR_BRANCH;
			}
				
		}
	}

	// check the current state
	private String getCurrentState() {
		// but we only put the block structure into it 
		// how could we mark the branch stuff?? Or it is ok that we only use String to keep the track of it ??
		// one thing is that, if we pop it out, we can't use it again, but this is not what we think??
		// we need to search until next and then change it, so now we only get get value
		if(stateStack.peek().equals(ProcessConfiguration.XOR_BRANCH)) {
			return stateStack.peek();
		} else if(stateStack.peek().equals(ProcessConfiguration.XOR))
			return stateStack.peek();
		else if(!stateStack.peek().equals(ProcessConfiguration.LOOP)){
			// if it is in another situation, we need to do what ???  
			// we search the most closest xor structure and check if it is in such one structure
			boolean inXOR = false;
			int i = stateStack.size();
			while(!inXOR && i>0) {
				i--;
				if(stateStack.get(i).equals(ProcessConfiguration.XOR_BRANCH))
					inXOR = true;
			}
			
			
			return stateStack.peek();
		}else {
			// it is in loop, now we need to see it it in the first part or last part...
			return stateStack.peek();
		}
		
		// here we need to check the states of it 
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
	@Plugin(name = "Traverse the Process Tree", level = PluginLevel.Regular, returnLabels = {"Process Tree" }, returnTypes = {ProcessTree.class
			}, parameterLabels = { "Process Tree"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Traverse Process Tree",  requiredParameterLabels = { 0})
	 public static ProcessTree testMain(PluginContext context, ProcessTree tree) 
	{ 
	        XORPairGenerator generator = new XORPairGenerator();
	        List<XORPair<ProcessTreeElement>> pairs = generator.generateXORPairs(tree);
	        System.out.println(pairs.size());
	        return tree;
	 } 
	
	
}
