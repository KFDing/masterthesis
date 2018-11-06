package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
		
		// a test to save energy
		if(xorList.isEmpty() || xorList.size() < 2) {
			System.out.println("There is no xor pair for long-term dependency check");
			return null;
		}

		// after we organize the xor structure, we create cluster
		Set<Node> aSet = getAllAncestors();
		buildCluster(tree.getRoot(), aSet);
		buildPair((Block)tree.getRoot());
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
			if(cluster.getKeyNode().equals(block))
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
	private Set<Node> getAllAncestors(){
		Set<Node> aSet = new HashSet<Node>();
		for(XORStructure<ProcessTreeElement> xorStructure : xorList) {
			aSet.addAll(getAncestors((Node) xorStructure.getKeyNode()));
		}
		return aSet;
	}

	// I want to generate the xor cluster for each xor and then check the relation of 
	private XORCluster<ProcessTreeElement> buildCluster(Node node, Set<Node> aSet) {
		// but now how to generate the pair from clusterList?? 
		Block block = (Block) node;
		System.out.println("visit block name: " + block.getClass().getSimpleName());
		
		XORCluster<ProcessTreeElement> cluster =  new XORCluster<ProcessTreeElement>(block);
		clusterList.add(cluster);
		
		List<Node> subNodes = block.getChildren();
		for(Node subNode : subNodes) {
			if(subNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				// it we meet directly the xor structure, what to do ?? 
				// we should create one cluster and then we can add it 
				// do we need to do it ?? Yes, we need to do it !! 
				XORCluster<ProcessTreeElement> xorCluster = createXORCluster(subNode);
				clusterList.add(xorCluster);
				// but how to know if we should do it again or not?? 
				xorCluster.parentCluster = cluster;
				cluster.addChilrenCluster(xorCluster);
			}else if(aSet.contains(subNode)) {
				// we go deep part, one thing we need to notice is that the ancestors not include itself
				XORCluster<ProcessTreeElement> subCluster = buildCluster(subNode, aSet);
				// here is not enough for us... Because we need to get the relation of children cluster
				subCluster.parentCluster = cluster;
				cluster.addChilrenCluster(subCluster);
			}
		}
		return cluster;
	}
	
	private void buildPair(Block block) {
		
		// we also need to visit the process tree to get the xor pair
		System.out.println("visit block name: " + block.getClass().getSimpleName());
		
		XORCluster<ProcessTreeElement> cluster = getCluster(block);
		if(cluster!=null) {
			// stateStack.push(block.getClass().getSimpleName()); // whatever it needs
			List<XORCluster<ProcessTreeElement>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<ProcessTreeElement> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				if(!child.isAvailable()) {
					buildPair((Block)child.getKeyNode());
				}
				
			}
			// we need to go the deep down level and get the begin and end xor list from them
			// why can't we goes here directly?? Because the child.isAvailable, why can't it go here?? 
			
			if(cluster.isSeqCluster()) { // all the elements are available, so we just do use it 
				// we get the begin and end list of xor of them 
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
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
					}
				}// if it is parallel, what to do ?? it seems nothing?? 
			
			}
			cluster.setAvailable(true);	
		}
		
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
		XORStructure<ProcessTreeElement> xorStructure = getXORStructure(node);
		// List<XORStructure<ProcessTreeElement>> tmpXORList = new ArrayList<XORStructure<ProcessTreeElement>>();
		xorCluster.addXORStructure(xorStructure);
		
		xorCluster.childrenCluster =null;
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
	@Plugin(name = "Traverse the Process Tree", level = PluginLevel.Regular, returnLabels = {"Process Tree" }, returnTypes = {ProcessTree.class
			}, parameterLabels = { "Process Tree"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Traverse Process Tree",  requiredParameterLabels = { 0})
	 public static ProcessTree testMain(PluginContext context, ProcessTree tree) 
	{ 
	        XORPairGenerator generator = new XORPairGenerator();
	        List<XORPair<ProcessTreeElement>> pairs = generator.generateXORPairs(tree);
	        System.out.println(pairs.size());
	        for(XORPair<ProcessTreeElement> p: pairs) {
	        	System.out.println(p.getSourceXOR().getKeyNode());
	        	System.out.println(p.getTargetXOR().getKeyNode());
	        }
	        
	        return tree;
	 } 
	
	
}
