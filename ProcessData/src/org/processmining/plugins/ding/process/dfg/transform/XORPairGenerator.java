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
		addXORPairByDFS(tree.getRoot());

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
						
						addXORPairByDFS(subNode);
					}
					// if the cluster has no xor 
					if(seqCluster.hasXOR()) {
						// what if the sequence in xor branch structure??  
						if(stateStack.peek().equals(ProcessConfiguration.XOR_BRANCH)) {
							// if we add the branches of xor,then later we use the extract branches from current branches..
							// we need to get the begin and end!! 
							// but now it's not the branch, anyway, they are xor structure but in one xor branches,
							// we need to list all the branches of it?? 
							for(XORStructure<ProcessTreeElement> bxor: seqCluster.getBeginXORList()) {
								List<XORBranch<ProcessTreeElement>> branches = bxor.getBranches();
								addBeginBranches(branches);
							}
						}
						// but we can just put the branches in it and then use extract branches
					}
				}
				
			}
		}
		
	}
	private void addBeginBranches(List<XORBranch<ProcessTreeElement>> branches) {
		// TODO Auto-generated method stub
		
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

	// this return pairList of xor structures
	private void buildXORPairbyDFS(Node node) {
		// we need to the clusterList
		// we need to revise the tree again, until we find the parts which we need, but in process tree
		// or the new cluster,we still need the node into it, but now we could somehow simpler
		XORCluster<ProcessTreeElement> cluster = getCluster(node);
		if(cluster.hasXOR()) {
			cluster.createSpecialPair();
			
			// except the special in structure pair, we need to create the cross structure pair on it 
			// then it relates to the node, I think !! int the same level?? Not in the same cluster?? 
			//  --- in the same cluster :: sequence, we do it combine them into the pairList but only for the sequence
			// maybe it can't build it because of the deep branches.. But anyway, we need to find 
			
		}
		
	}
	private void extractXORPair(Node node) {
		// all the xor are current xor structure used to build the pair structure
		// all xor is in an list on order, 
		// if we want to check if they are should create special result
		// we check them in parallel, or in the same loop structure.. Anyway, else, if they have some order..
		// could we put some order to them?? 
		// like to link the before and next 
		// and also the same structure the next one?? pointer to solver the problem? 
		// if we have such information, we can create the pair only by using xorList
		// 
		if(xorList.isEmpty() || xorList.size() <2) {
			//here we didn't consider the effect of parallel structure, if we use this,
			// we need to make sure that it can be done in this way. 
			System.out.println("xor List it does not have enough elements used to extrac");
			return;
		}
		
		
		// for the queue, we need to do something more, we need more flexible structure
		// so we change the Queue to list 
		if(node.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
			// we are here in the list of sequence
			int i = xorList.size() , count = 0;
			while(i > 0) {
				i--;
				if(stateStack.peek().equals(ProcessConfiguration.XOR)) {
					// create special for parallel, but need to make sure that they belong to same
					count ++;
					stateStack.pop(); // what if the xor in another branch of parallel?? too complex, I can only say!! 
					// but I will leave it to another cases test. 
					// if it is sequence it doesn't matter, so we don't care about it
					// what we have in the xor,
				}else if(stateStack.peek().equals(ProcessConfiguration.PARALLEL)){
					// go back to the parallel begining, then we stop here
					break;
				}// but what if we meet another structure?? if they are only one what to do?
				else {
					stateStack.pop();
				}
			}
			// after this we have the get the count of xor in parallel
			if(count>1) {
				
				for(int increment = 0; increment < count; increment++) {
					pairList.add(createXORPair(xorList.get(i + increment), xorList.get(i+ increment + 1)));
					
				}
				
			}
		}
		
		
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
