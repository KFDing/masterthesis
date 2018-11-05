package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
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
	 * the same thing like in the methods before but to organize it, and simplify it only to get the xor structure list
	 * in such situations:
	 *   -- pure or impure xor structure
	 *   -- only sequence relation, we consider it
	 * :: but now we just need to list of xor structure
	 */
	public void analyzeXORByDFS(Node node) {
		if(node.isLeaf()) {
			System.out.println("visit node : " + node.getName());
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
			System.out.println("visit block name: " + block.getClass().getSimpleName());
			
			// if the block is xor 
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				XORStructure<ProcessTreeElement> xorStructure = new XORStructure<ProcessTreeElement>(block);
				// add xor in the xorList, we need to consider when we need it
				xorList.add(xorStructure);
				// we have all its subNodes
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					
					XORBranch<ProcessTreeElement> branch = new XORBranch<ProcessTreeElement>();
					xorBranchList.add(branch);
					
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
						// new created
						if(currentXORBranch.getBeginNode() == null) {
							currentXORBranch.setBeginNode(subNodes.get(0));
						}
						currentXORBranch.setEndNode((ProcessTreeElement) getLastElement(subNodes));
					}else {
						// normal situation, but we need to check if we should add silent transitions
						// consider the begin and end for parallel or loop.. Both works, somehow
						if(subNodes.get(0).getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
							addSilentNode((Block)subNodes.get(0));
						if(subNodes.get(subNodes.size() -1 ).getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) 
							addSilentNode((Block)subNodes.get(subNodes.size() -1 ));
						for(Node subNode : subNodes) {
							analyzeXORByDFS(subNode);
						}
					}
					currentXORBranch.setOpen(false);
				}
				
			}else { //  if(block.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
				// what to do here?? Actually we don't have much difference of those two structures now.
				List<Node> subNodes = block.getChildren();
				for(Node subNode : subNodes) {
					analyzeXORByDFS(subNode);
				}
			}
		}
	}


	private Object getLastElement(List xlist) {
		// TODO Auto-generated method stub
		return xlist.get(xlist.size() - 1);
	}

	private XORCluster<ProcessTreeElement> getCluster(Block block) {
		// we check the list of cluster and find the parent
		for(XORCluster<ProcessTreeElement> cluster: clusterList) {
			// here we nned to use another method to check the existence 
			List<Node> subNodes = block.getChildren();
			for(Node subNode : subNodes) {
				if(subNode.equals(block))
					return cluster;
			}
			
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

	
	private List<XORStructure<ProcessTreeElement>> sourceXORForPair;
	/**
	 * this mehtod is used to create XORPair from XORList, but still we need to consider the structure of process tree
	 * after visit the process tree, we have xorList
	 *  now we need to create the xor pair
	 *   -- for i and j to visit the xorlist
	 *   for the current xor i and j structure, we need to know if they are direct connected
		// if they are, then we will create a pair of it
		// but during the retrieve of them, we can set the current and previous list of xor to use
		// and then create the current and previous list pair from them
		//   but how to create the 
	 * @param block
	 */
	
	// the easiest way to do is creat all combinations of them, but, we need to find the direct following node
	// now we only consider to find the direct following xor of current xor 
	// we visit the process tree 
	// and put the structure of xor in stateStack
	// then we visit the stateStack of them 
	private void buildXORPairs() { 
		// we need to create the cluster at first, only consider no-nested xor structure
		if(xorList.isEmpty() || xorList.size() < 2) {
			System.out.println("There is no xor pair for long-term dependency check");
			return ;
		}
		
		// if there is xor structure for pair >=2
		// we use them as an stack, and push and pop
		
		List<Integer> nextXORList = new ArrayList<Integer>();
		int currentIdx = 0, nextIdx;
		
		while(currentIdx< xorList.size() - 1) {
			
			nextIdx = currentIdx +1;
			while(nextIdx < xorList.size()) {
				if(inDirectNextOrder(currentIdx, nextIdx)) {
					nextXORList.add(nextIdx);
					nextIdx ++;
				}
			}
			// after we have it, we create pairs of them
			pairList.addAll(createXORPair(currentIdx, nextXORList));
			currentIdx ++;
		}
		
		
	}
	
	private List<XORPair<ProcessTreeElement>> createXORPair(int sourceIdx,
			List<Integer> nextXORList) {
		
		List<XORPair<ProcessTreeElement>> pList = new ArrayList<XORPair<ProcessTreeElement>>();
		XORStructure<ProcessTreeElement> xorSource, xorTarget;
		xorSource = xorList.get(sourceIdx);
		
		for( int i: nextXORList) {
			xorTarget = xorList.get(i);
			
			XORPair<ProcessTreeElement> pair = new XORPair<ProcessTreeElement>();
			pair.setSourceXOR(xorSource);
			pair.setTargetXOR(xorTarget);
			pList.add(pair);
		}
		
		return pList;
	}
	
	private boolean inDirectNextOrder(int currentIdx, int nextIdx) {
		// to check if the currentXOR and nextXOR are the next
		// how to define the next order relation? 
		// if they are in seq, so we can define it good
		if(currentIdx >= nextIdx)
			return false;
		// we need to get the currentIdx and nextIdx
		// if they have the same parent and parent is what ?? 
		Node currentNode = (Node) xorList.get(currentIdx).getKeyNode();
		Node nextNode = (Node) xorList.get(nextIdx).getKeyNode();
		Block cparent = getParent(currentNode);
		Block nparent = getParent(nextNode);
		if(cparent.equals(nparent)) {
			if(cparent.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
				// so they are the direct next one
				if(currentIdx + 1 == nextIdx)
					return true;
				else
					return false;
			}else { // if(cparent.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) 
				return false; // they don't have direct order, only happen in sequence
			}
		}else {
			// they don't have the same parent, so we need to figure in which situation they are direct next
			// if they are not in the same order, not in parallel, we can be sure 
			
		}
		
		return false;
	}

	private List<Integer> getSameOrderXOR(int currentIdx){
		List<Integer> sameIdxs = new ArrayList<Integer>();
		
		// we check from the current to later 
		
		Node currentNode = (Node) xorList.get(currentIdx).getKeyNode();
		List<Node> cancestors = getParallelAncestors(currentNode);
		if( cancestors.isEmpty()) {
			// there is no parallel ancestors, so we set it none
			return null;
		}
		
		int nextIdx = currentIdx + 1;
		Node nextNode = (Node) xorList.get(nextIdx).getKeyNode();
		
		Block cparent = getParent(currentNode);
		Block nparent = getParent(nextNode);
		
		while(nextIdx < xorList.size()) {
			// if currentNode and nextNode in the same order 
			if(cparent.equals(nparent) && cparent.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
				sameIdxs.add(nextIdx);
				
			}else {
				// if they don't have same parent 
				// check if one of the xor has parallel parent.. 
				List<Node> nancestors = getParallelAncestors(nextNode);
				if( nancestors.isEmpty()) {
					// there is no parallel ancestors for the nextNode, what to do ?? So we
					continue;
				}else {
					// nextNode has parallel ancestors;; check the relation of them.. 
					// get the common parallel ancestors of current and nextNode
					for(Node cp : cancestors) {
						if(nancestors.contains(cp)) {
							// if cp is the common parallel ancestor
							// in same order, if they are first visited in the parallel branches
							// or last visited
							if(firstXORVisited(currentNode, cp) && firstXORVisited(nextNode, cp) || 
									(lastXORVisited(currentNode, cp) && lastXORVisited(nextNode, cp))) {
								sameIdxs.add(nextIdx);
							}
						}
					}
					
					
					
				}
			}
			nextIdx++;
		}
		
		return sameIdxs;
	}

	private boolean lastXORVisited(Node currentNode, Node cp) {
		// last visited node in the parallel branch 
		return false;
	}

	private boolean firstXORVisited(Node currentNode, Node pancestor) {
		// to check if currentNde is firstly visted by the pp on its own branch
		List<Node> cancestor = getAncestors(currentNode);
		
		Block block = (Block) pancestor;
		List<Node> subNodes = block.getChildren();
		
		for(Node subNode : subNodes) {
			if(cancestor.contains(subNode)) {
				// we go to the branch of currentNode
				
				
			}
		}
		
		return false;
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

	private List<Node> getParallelAncestors(Node currentNode) {
		List<Node> ancestors = new ArrayList<Node>();
		
		while(!currentNode.isRoot()) {
			Block parent =  getParent(currentNode);
			if(parent.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
				// what if they are not in the same ancestor?? So we need to return the list of ancestors
				ancestors.add(parent);
			}
			currentNode = parent;
		}
		// but if currentNode is root, what to do ?? 
		if(currentNode.isRoot()) {
			if(currentNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
				ancestors.add(currentNode);
		}
		
		return ancestors;
	}

	// I want to generate the xor cluster for each xor and then check the relation of 
	private void buildXORCluster() {
		if(xorList.isEmpty() || xorList.size() < 2) {
			System.out.println("There is no xor pair for long-term dependency check");
			return ;
		}
		int i=0;
		
		while(i<xorList.size()) {
			
		}
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
