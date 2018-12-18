package org.processmining.plugins.ding.process.dfg.transform;
/**
 * this class is used to generate the xor branch into one table of the whole nodes. 
 * Input: 
 *    ++ process tree
 *    ++ event log but later  
 * Output: 
 *    ++ all xor branches which have the choices;; (if it is one parallel branch, what to do then?? It happens in both 
 *    situations, then we put them together, so we just use the pure branch cluster as item)
 *    ++ if we meet nested parallel but still goes into this step then.
 *    
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class NewXORBranchGenerator<T> {
	ProcessTree tree;
	// do we need to define a new structure for pure branch cluster?? Not really, we can use it again
	List<XORBranch<T>> branchList;
	/*
	public NewXORBranchGenerator(ProcessTree pTree) {
		tree = pTree;
		branchList = new ArrayList<XORBranch<T>>();
		
	}
	*/
	
	// use one plugin in to test its correctness
	
	public void buildBranches(ProcessTree tree) {
		Set<Node> xorSet = getAllXORs(tree);
		
		if(tree.size() < 1 || xorSet.size() <2) {
			System.out.println("The tree is empty or not enough xors");
			return ;
		}
		
		Set<Node> aSet = getAllXORAncestors(xorSet);
		
		branchList = new ArrayList<XORBranch<T>>();
		buildXORBranches(tree.getRoot(), aSet, false);
		
	}
	
	public List<XORBranch<T>> getBranchList(){
		return branchList;
	}
	
	public XORBranch<T> buildXORBranches(Node node, Set<Node> aSet, boolean inXOR) {
		
		// for leaf node, we only visit in pure branch ones
		if(inXOR) {
			XORBranch<T> branch =  new XORBranch<T>((T) node);
			// so we need to have childrencluster, but we can set branch cluster specially..
			if(node.isLeaf()) {
				branch.setIsLeaf(true);
				return branch;
			}else {
				List<Node> subNodes = ((Block)node).getChildren();
				for(Node subNode : subNodes) {
					XORBranch<T> subBranch = buildXORBranches(subNode, aSet, true);
					branch.addChildren(subBranch);
				}
				
			}
			return 	branch;
		}
		
		Block block = (Block)node;
		if(isXORBlocck(block)) {
			// if we meet one xor block
			List<Node> subNodes = block.getChildren();
			for(Node subNode : subNodes) {
				if(aSet.contains(subNode))
					buildXORBranches(subNode, aSet, false);
				else {
					XORBranch<T> subBranch = buildXORBranches(subNode, aSet, true);
					branchList.add(subBranch);
				}
			}
		}else if(aSet.contains(node)) {
			// not xor but contains xor we visit its subnodes
			List<Node> subNodes = block.getChildren();
			for(Node subNode : subNodes) {
				if(aSet.contains(subNode)|| isXORBlocck(subNode))
					buildXORBranches(subNode, aSet, false);
			}
			
		}
		
		return null;
	}
	// get all the xor block nodes
	public Set<Node> getAllXORs(ProcessTree tree) {
		Set<Node> xorSet = new HashSet<Node>();
		for(Node node: tree.getNodes()) {
			if(isXORBlocck(node)) {
				xorSet.add(node);
			}
		}
		return xorSet;
	}
	
	// get all ancestors from one node to the root
	public Set<Block> getAncestors(Node node){
		Set<Block> ancestors = new HashSet<Block>();
	
		Collection<Block>  parents = node.getParents();
		List<Block> currentParents = new ArrayList<Block>();
		if(parents.isEmpty())
			return ancestors;
		
		// when to stop it until we reach the root, we stop it 
		while(!ancestors.containsAll(parents)) {
			ancestors.addAll(parents);
			currentParents.clear();
			
			for(Block pBlock: parents) {
				currentParents.addAll(pBlock.getParents());
			}
			parents = currentParents;
		}
		
		return ancestors;
	}
 	
	public Set<Node> getAllXORAncestors(Set<Node> xorSet) {
		// TODO get all xor ancestors in process tree including the xor structure?
		// no, so we can distinguish nested and not nested xor block. 
		
		Set<Node> aSet = new HashSet<Node>();
		// 2. get the ancestors of them, here is one problem, 
		// if we don't go for it, only the parents node, what we can do later??  
		for(Node xorNode: xorSet) {
			Set<Block> ancestors = getAncestors(xorNode);
			if(ancestors.isEmpty())
				continue;
			
			aSet.addAll(ancestors);
		}
		
		return aSet;
	}

	private boolean isXORBlocck(Node node) {
		// TODO check if this node is xor 
		if(node.getClass().getSimpleName().equals(ProcessConfiguration.XOR))
			return true;
		return false;
	}
	
}
