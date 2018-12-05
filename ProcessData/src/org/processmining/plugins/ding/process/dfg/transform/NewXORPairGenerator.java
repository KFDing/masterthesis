package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.ding.process.dfg.model.NewLTConnection;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

/**
 * this class is used to analyse the Process Tree structure, generate the XOR Pair of Process Tree
 * 
 * @author dkf
 *  02 Dec 2018 modified
 *  -- we use more general structure to deal with parallel situations, but we need to change all the strucuture,
 *    then I think I could try to use the generic type and see what I can get
 */
public class NewXORPairGenerator<T> {

	
	List<XORClusterPair<T>> clusterPairList;
	
	List<XORCluster<T>> clusterList;
	Set<NewLTConnection<T>> connList;
	
	ProcessTree tree;
	// here we can test the cluster pair, the size of it, if we don't have anyone, then
	// we don't need to do it then.. But do you really think there is a need for this?? 
	// if we abandon the xor block structure, but how to remark them?? 
	// we need to get the cluster from the begin,
	// 1. first to check all the nodes of process tree, and find the nodes which is the xor 
	// 2. then generate all the xor ancestors
	// 3. create xor cluster with all we need to do.. 
	
	
	public void generatePairs(ProcessTree pTree) {
		
		tree = pTree;
	    clusterPairList = new ArrayList<XORClusterPair<T>>();
		clusterList =  new ArrayList<XORCluster<T>>();
		connList =  new HashSet<NewLTConnection<T>>();
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		Set<Node> xorSet = getAllXORs(tree);
		
		if(tree.size() < 1 || xorSet.size() <2) {
			System.out.println("The tree is empty or not enough xors");
			return ;
		}
		
		Set<Node> aSet = getAllXORAncestors(xorSet);

		buildCluster(tree.getRoot(), aSet, false);
		buildClusterPair(tree.getRoot());
		
	}

	private void buildClusterPair(Node node) {
		// TODO Auto-generated method stub
		XORCluster<T> cluster = getCluster(node);
		
		if(cluster!=null) {
			
			List<XORCluster<T>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<T> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				if(!child.isAvailable()) {
					buildClusterPair((Node) child.getKeyNode());
				}
				
			}
			
			if(cluster.isSeqCluster()) { // all the elements are available, so we just do use it 
				// we just have the useful childrencluster to create pair 
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
					XORCluster<T> sourceCluster, targetCluster;
					sourceCluster = childrenCluster.get(0);
					int i=1;
					// if there is only one, then what to do ??? we need to keep actually the last node to the next one!!!
					while(i< childrenCluster.size()) {
						targetCluster = childrenCluster.get(i);
						
						for(XORCluster<T> schild: sourceCluster.getEndXORList())
							for(XORCluster<T> tchild: targetCluster.getBeginXORList()) {
								// but what happens if they are in a branch, not matter
								clusterPairList.addAll(createClusterXORPair(schild, tchild));
							}
						
						sourceCluster = targetCluster;
						i++;
					}
				}
			}
			cluster.setAvailable(true);
		}
	}

	
	private List<XORClusterPair<T>> createClusterXORPair(XORCluster<T> sourceCluster, XORCluster<T> targetCluster) {
		// here are only the xor cluster, we need only to create the branch cluster into it 
		List<XORClusterPair<T>> pairList =  new ArrayList<XORClusterPair<T>>();
		
		XORClusterPair<T> pair = new XORClusterPair<T>(sourceCluster, targetCluster, false);
		connList.addAll(pair.getLTConnection());
		pairList.add(pair);
		
		return pairList;
	}

	public Set<NewLTConnection<T>> getAllLTConnection() {
		return connList;
	}
	
	public XORCluster<T> getCluster(Node node) {
		for(XORCluster<T> cluster: clusterList) {
			if(cluster.getKeyNode().equals(node))
					return cluster;
			
		}
        System.out.println("couldn't find the xor cluster");
		return null;
	}
	
	public XORCluster<T> buildCluster(Node node, Set<Node> aSet, boolean inXor) {
		// TODO create all cluster related to xor
		//1. as one branch in xor
		//2. including xor branch.
		if(node.isLeaf()) {
			XORCluster<T> cluster =  new XORCluster<T>((T) node);
			cluster.setIsLeaf(true);
			cluster.setAvailable(true);
			clusterList.add(cluster);
			return cluster;
		}
		
		Block block =(Block) node;
		
		XORCluster<T> cluster =  new XORCluster<T>((T) block);
		clusterList.add(cluster);
		if(aSet.contains(node)) {
			// ancestors only above the xor structure, not include the xor structure itself. 
			cluster.setHasXOR(true);
		}
		
		List<Node> subNodes = block.getChildren();
		for(Node subNode : subNodes) {
			// at first for the xorCluster
			if(aSet.contains(subNode)) {
				// including nested and not nested xor
				XORCluster<T> subCluster = buildCluster(subNode, aSet, false);
				
				cluster.addChilrenCluster(subCluster);
				
			}else if(isXORBlocck(subNode)) {
				//  xor concrete  block, not nested 
				// we need to create also the cluster to protect it, if we goes into it, we need goes back into it
				XORCluster<T> xorCluster = buildCluster(subNode, aSet, true);
				
				cluster.addChilrenCluster(xorCluster);
			}else if(inXor) {
				// until reach the leaf node, this will pass all the time, which makes it remember 
				// the choice we have made
				XORCluster<T> branchCluster = buildCluster(subNode, aSet, true);
				cluster.addChilrenCluster(branchCluster);
			}
		}
		return cluster;
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
	
	public Set<Block> getAncestors(Node node){
		Set<Block> ancestors = new HashSet<Block>();
		if(node.isRoot())
			return ancestors;
		
		Collection<Block> currentParents, parents = node.getParents();
		if(parents.isEmpty())
			return ancestors;
		
		currentParents = parents;
		while(!ancestors.containsAll(currentParents)) {
			ancestors.addAll(currentParents);
			
			for(Block pBlock: parents) {
				if(!pBlock.isRoot() && !currentParents.contains(pBlock))
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

	public List<XORClusterPair<T>> getClusterPair() {
		// TODO Auto-generated method stub
		return clusterPairList;
	}

	public XORClusterPair<T> findClusterPair(XORCluster<T> sourceCluster, XORCluster<T> targetCluster) {
		for(XORClusterPair<T> pair: clusterPairList) {
			if(pair.getSourceXORCluster().equals(sourceCluster) && pair.getTargetXORCluster().equals(targetCluster))
				return pair;
		}
		
		return null;
	}
	
	
	
}
