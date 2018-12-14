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
import java.util.List;
import java.util.Set;

import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class NewXORBranchGenerator<T> {
	ProcessTree tree;
	// do we need to define a new structure for pure branch cluster?? Not really, we can use it again
	List<XORCluster<T>> branchList;
	
	public NewXORBranchGenerator(ProcessTree pTree) {
		tree = pTree;
		branchList = new ArrayList<XORCluster<T>>();
		
	}
	
	public XORCluster<T> buildXORBranches(Node node, Set<Node> aSet, boolean isXORBranch) {
		// like we build cluster actually but now, we don't care about the xor cluster anymore
		// only the branches here
		// but we need to go into the deep node, but not matter actually, ok .. we only record the xor branch
		
		
		// for leaf node, we only visit in pure branch ones
		if(node.isLeaf()) {
			XORCluster<T> cluster =  new XORCluster<T>((T) node);
			cluster.setIsLeaf(true);
			if(!isXORBranch)
				// decide when to add cluster into branch
				branchList.add(cluster);
			return cluster;
		}
		
		Block block = (Block)node;
		// we need to return cluster, only for pure branch cluster
		if(!aSet.contains(node) && isXORBranch) {
			XORCluster<T> cluster =  new XORCluster<T>((T) block);
			branchList.add(cluster);

			List<Node> subNodes = block.getChildren();
			for(Node subNode : subNodes) {
				XORCluster<T> subCluster = buildXORBranches(subNode, aSet, true);
				cluster.addChilrenCluster(subCluster);
			}
			return cluster;
		}else{
			// this contains node, we need to test what it is
			if(aSet.contains(block)) {
				List<Node> subNodes = block.getChildren();
				
				for(Node subNode : subNodes) {
					if(aSet.contains(subNode))
						buildXORBranches(subNode, aSet, false);
				}
			}else if(isXORBlocck(block)) {
				List<Node> subNodes = block.getChildren();
				
				for(Node subNode : subNodes) {
					buildXORBranches(subNode, aSet, true);
				}
				
			}
			
		}
		
		return null;
		
	}
	
	private boolean isXORBlocck(Node node) {
		// TODO check if this node is xor 
		if(node.getClass().getSimpleName().equals(ProcessConfiguration.XOR))
			return true;
		return false;
	}
	
}
