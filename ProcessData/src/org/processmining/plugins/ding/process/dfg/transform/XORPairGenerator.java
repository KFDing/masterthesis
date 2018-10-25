package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.model.XORPair;
import org.processmining.plugins.ding.process.dfg.model.XORStructure;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

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
	Stack<XORStructure<ProcessTreeElement>> xorStack;
	Stack<XORBranch<ProcessTreeElement>> xorBranchStack;
	// one is for process tree
	
	public List<XORPair<ProcessTreeElement>> generateXORPairs(ProcessTree tree) {
		pairList = new ArrayList<XORPair<ProcessTreeElement>>();
	
		stateStack = new Stack<String>();
		xorStack = new Stack<XORStructure<ProcessTreeElement>>();
		xorBranchStack = new Stack<XORBranch<ProcessTreeElement>>();
		
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		addXORPairByDFS(tree.getRoot());
		return pairList;
	}

	
	// -- if node is in the middle, then we need to check it types
	//  ++ if it is xor, then we need to check the stateStack,
	//      if stateStack is in xor branch, 
	//          we add stateStack with another branch, goes into then left subtree
	//      if stateStack is in sequence, we create the xor context into the stateStack..
	//         first to put them into xor context and then merge them, merge them.. 
	//         but if sequence is in xor stateStack, then we create another xor branch
	//      if closest stateStack is in parallel, we create xor context into stateStack'
	//         but if the parallel is in xor branch, we create another branch
	//           however, if in the parallel there are more than one xor structure, then we need to
	//           create new xor context, so we decide to do:: 
	//              <1> xor in parallel, we create the xor context 
	//              <2> we count the xor structure in parallel, 
	//                    if they are less than 1
	//                       then we consider if it is in an xor branch, if it is then, merge them with another xor
	//                       // function to merge the xor structure.
	//                  we create pair into it..if number is over 2, 
	//                  we still create pairs which is only binary relation, <1,2>, <2,3> 
	//                
	//      if stateStack is in loop, 
	//         we check if it is in the first part of loop
	//            we create the xor context into stateStack
	//         if it is in the second part of loop
	//            we create the xor context into stateStack
	//      
	//  ++ if it is sequence, then put sequence into stateStack
	//  ++ if it is parallel, then put parallel into stateStack
	//  ++ if it is parallel, then put parallel into stateStack
	
	// others we need to pay attention to is how to combine them into an pair!!!! 
	//  we get from the closest structure of stateStack, but under which situation we do it??? 
	//  -- if we meet 3rd xor structure in the stateStack, we put the pair before it into pair
	//  -- but how to really count them in the same level??? 
	//  -- it depends on how far we could go.. after creating one pair, does that mean
	//   we could remove them from stateStack?? I guess no!!! except for sequence
	//   if we record the relation of first, we don't use it anymore, so we can how to say that 
	//    we can remove it form the stateStack, 
	//   in parallel, except the global dependency in itself, it also need to relation to before
	
	// visit the left subtree
	// addXORPairByDFS(leftSubTree);
	
	// visit the right subtree
	// addXORPairByDFS(rightSubTree);
	
	public void addXORPairByDFS(Node node ) {
		if(node == null) {
			System.out.println("empty tree node");
			return ;
		}
		// -- if node is a leaf, we can stop it here, actually not check if it is null
		//  ++ check the current stack if it is in an xor branch, 
		//    check if there is already a start node, we don't do anything;; else, we assign it to the begin
		//     but we just assign it to the branch end, if only if it is still in the xor branch,  
		
		if(node.isLeaf()) {
			System.out.println("visit node : " + node.getName());
			if(getCurrentState().equals(ProcessConfiguration.XOR_BRANCH)) {
				// should we pop it up or wait for it ??? 
				XORBranch<ProcessTreeElement> currentXORBranch = xorBranchStack.peek();
				if(currentXORBranch.getBeginNode()== null)
					currentXORBranch.setBeginNode(node);
				
				currentXORBranch.setEndNode(node);
			}
		}else { // node is a block
			Block block =  (Block) node;
			System.out.println("visit block name: " + block.getClass().getSimpleName());
			
			// for each new added we can do it, but we need on state to control it
			// also we need to give back extract the current branch into the xorPair
			extractXORBranch();
			extracXORPair();
			
			// it is an xor block
			if(block.getClass().getSimpleName().equals(ProcessConfiguration.XOR)) {
				// we just create the new xor contex intot he stack
				stateStack.push(block.getClass().getSimpleName());
				xorStack.push(new XORStructure<ProcessTreeElement>(block));
				
				List<Node> subNodes = block.getChildren();
				for(Node subNode: subNodes) {
					// change current state
					stateStack.push(ProcessConfiguration.XOR_BRANCH);
					xorBranchStack.push(new XORBranch<ProcessTreeElement>());
					addXORPairByDFS(subNode);
				}
				
			}else {
				stateStack.push(block.getClass().getSimpleName());
				/*
				if(block.getClass().getSimpleName().equals(ProcessConfiguration.SEQEUNCE)) {
					if(getCurrentState().equals(ProcessConfiguration.XOR)) {
						// if the sequence is in xor structure, we need to modify now the xor branch data
						
					}else {
						// in other cases, we don't really care about it ?? , but if we don't record it in stateStack
						// we can't know it for the last use!!!! 
					}
				}
				*/
				List<Node> subNodes = block.getChildren();
				for(Node subNode: subNodes) {
					addXORPairByDFS(subNode);
				}
				
			}
			
			
		}
		
	}
	private void extracXORPair() {
		// all the xor are current xor structure used to build the pair structure
		if(xorStack.isEmpty()) {
			System.out.println("xor Stack it's empty, not used to extrac");
			return;
		}
		XORPair<ProcessTreeElement> pair = new XORPair<ProcessTreeElement>();
		if(stateStack.pop().equals(ProcessConfiguration.XOR)) {
			pair.setSourceXOR(xorStack.pop());
		}
		if(stateStack.pop().equals(ProcessConfiguration.XOR)) {
			pair.setTargetXOR(xorStack.pop());
		}
		// only consider the last two xor structure
		pairList.add(pair);
	}


	private void extractXORBranch() {
		// here we check if one xor structure has all the branches '
		// if it is then we transfer those branches into the xor structure
		// if we could make sure that all the branches there belong to the same xor structure, it's fine
		// but we can't , so we need to think of another methods to change it..
		// we put all the same branches into it, so we extract it.. That's fine
		// but before we check our states
		// if we have finished all the xor branch traverse, we could release it
		if(xorBranchStack.isEmpty()) {
			System.out.println("xor branches it's empty, not used to extrac");
			return;
		}
		int count = 0;
		XORStructure<ProcessTreeElement> currentXOR =  xorStack.peek();
		while(stateStack.pop().equals(ProcessConfiguration.XOR_BRANCH)) {
			count ++ ;
			// here we might not remove all the branches here, still left some for the later xor structure..
			currentXOR.addBranch(xorBranchStack.pop());
		}

		if(count != currentXOR.getNumOfBranches()) {
			System.out.println("not all the branches are direct nodes");
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
