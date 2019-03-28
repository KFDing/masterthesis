package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.incorporatenegativeinformation.models.LTRule;
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.incorporatenegativeinformation.models.XORClusterPair;
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
	List<LTRule<XORCluster<T>>> connList;
	
	List<XORCluster<T>> branchList;
	ProcessTree tree;
	// here we can test the cluster pair, the size of it, if we don't have anyone, then
	// we don't need to do it then.. But do you really think there is a need for this?? 
	// if we abandon the xor block structure, but how to remark them?? 
	// we need to get the cluster from the begin,
	// 1. first to check all the nodes of process tree, and find the nodes which is the xor 
	// 2. then generate all the xor ancestors
	// 3. create xor cluster with all we need to do.. 
	List<XORCluster<T>> cpList;
	
	public void initialize(ProcessTree pTree) {
		
		tree = pTree;
		clusterPairList = new ArrayList<XORClusterPair<T>>();
		clusterList =  new ArrayList<XORCluster<T>>();
		connList =  new ArrayList<LTRule<XORCluster<T>>>();
		cpList = new ArrayList<XORCluster<T>>();
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		Set<Node> xorSet = getAllXORs(tree);
		
		if(tree.size() < 1 || xorSet.size() <2) {
			System.out.println("The tree is empty or not enough xors");
			return ;
		}
		
		Set<Node> aSet = getAllXORAncestors(xorSet);

		buildCluster(tree.getRoot(), aSet, false);
		buildCPList(tree.getRoot());
	}

	private static int level = 0;
	private void buildCPList(Node node) {
		// TODO Auto-generated method stub
		XORCluster<T> cluster = getCluster(node);
		
		if(cluster!=null) {
			
			List<XORCluster<T>> childrenCluster = cluster.getChildrenCluster();
			for(XORCluster<T> child : childrenCluster) {
				// still not good effect... Nanan, because one level missed it
				child.testPairAvailable();
				if(!child.isPairAvailable()) {
					buildCPList((Node) child.getKeyNode());
				}
			}
			
			if(cluster.isSeqCluster()) { // all the elements are available, so we just do use it 
				// we just have the useful childrencluster to create pair 
				if(cluster.getChildrenCluster().size() < 2) {
					System.out.println("too few xor in sequence to connect it");
				}else {
					XORCluster<T> tmpCluster ;
					int i=0;
					// if there is only one, then what to do ??? we need to keep actually the last node to the next one!!!
					while(i< childrenCluster.size()) {
						tmpCluster = childrenCluster.get(i);
						// this is left to the choices later, we don't build all pairs in order at once
						tmpCluster.setLevel(level);
						// 
						cpList.add(tmpCluster);
						
						i++;
					}
					// we only use level in seq that can be combined as pair.. In this order, actually, we have the order of oxor block
					level++;
				}
			}
			cluster.setPairAvailable(true);
			
		}
	}
	
	// we need to check if the pair is valid to build or not...
	// if they have the same level, // as for if they are used before dependent on the cpList, but not here!!!
	public boolean checkPairValid(XORCluster<T> source, XORCluster<T> target) {
		if(source.getLevel() == target.getLevel())
			if(findClusterIndex(source) < findClusterIndex(target)) {
				return true;
			}
			
		return false;
	}
	
	private int findClusterIndex(XORCluster<T> source) {
		// TODO Auto-generated method stub
		int idx=0;
		for(XORCluster<T> cluster : cpList) {
			if(cluster.equals(source))
				return idx;
			else
				idx++;
		}
		return -1;
	}

	// now we have a method to generate a pair list in order, 
	// we unify the parts to generate the data here... Firstly, we have cpList and we know the order of them
	// then we check the level of them and then generate the pair here.. That's all
	public List<XORClusterPair<T>> buildAllPairInOrder(){
		// after getting cpList, it is sorted in  Depth-first visited to have order.. 
		// to generate pair, we visit the cpList until the level is not the same, 
		// until we visit all pair
		XORCluster<T> source, target;
		int idx = 0;
		int tmpLevel = 0;
		while(idx< cpList.size()-1) {
			source = cpList.get(idx);
			target = cpList.get(idx+1);
			if(checkPairValid(source, target)) {
				// create pair
				createClusterXORPair(source, target);
			}else {
				// different level, they can combine together
				tmpLevel = target.getLevel();
			}
			
			idx++;	
		}
		return clusterPairList;
	}
	
	public XORClusterPair<T> createClusterXORPair(XORCluster<T> sourceCluster, XORCluster<T> targetCluster) {
		// here are only the xor cluster, we need only to create the branch cluster into it 
		// if it doens;t include this pair, then we create, else we just return it 
		XORClusterPair<T> pair = new XORClusterPair<T>(sourceCluster, targetCluster, false);
		clusterPairList.add(pair);
		connList.addAll(pair.getConnection());
		return pair;
	}

	public List<XORCluster<T>> getCPList(){
		return cpList;
	}
	
	public List<XORCluster<T>> getClusterList(){
		return clusterList;
	}
	
	
	public List<LTRule<XORCluster<T>>> getAllLTConnection() {
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
			cluster.setPairAvailable(true);
			cluster.setLtAvailable(true);
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
		boolean ltAvailable = true;
		
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
				branchCluster.setParent(cluster);
				ltAvailable &= branchCluster.isLtAvailable();
			}
		}
		// assign ltAvailable here with condition all childrencluster are pure branch 
		cluster.setLtAvailable(ltAvailable);
		
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
	
	public List<Node> getAncestors(Node node){
		List<Node> parentList = new ArrayList<Node>();
		// parentList.add(node);
		Collection<Block> parent = node.getParents();
		while(!parentList.containsAll(parent)) {
			parentList.addAll(parent);
			
			
			List<Block> tmpParents = new ArrayList<Block>();
			for(Block block: parent) {
				tmpParents.addAll(block.getParents());
			}
			parent = tmpParents;
			
			if(parent.isEmpty())
				break;
		}
		return parentList;
	}
 	
	public Set<Node> getAllXORAncestors(Set<Node> xorSet) {
		// TODO get all xor ancestors in process tree including the xor structure?
		// no, so we can distinguish nested and not nested xor block. 
		
		Set<Node> aSet = new HashSet<Node>();
		// 2. get the ancestors of them, here is one problem, 
		// if we don't go for it, only the parents node, what we can do later??  
		for(Node xorNode: xorSet) {
			Set<Node> ancestors = new HashSet<Node>(getAncestors(xorNode));
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
	
	// here we need to create a list available to as the source
	public List<XORCluster<T>> getAddAvailableSources(){
		List<XORCluster<T>> sourceList = new ArrayList<XORCluster<T>>();
		for(XORCluster<T> cluster : cpList) {
			if(!cluster.isAsSource()) 
				sourceList.add(cluster);
		}
		return sourceList;
	}
	
	// get the list availabel for the target given the source
	public List<XORCluster<T>> getAddAvailableTargets(XORCluster<T> source){
		List<XORCluster<T>> targetList = new ArrayList<XORCluster<T>>();
		for(XORCluster<T> cluster : cpList) {
			if(!cluster.isAsTarget() && checkPairValid(source, cluster))
				targetList.add(cluster);
		}
		return targetList;
	}
	
	// if they have the availabel source or target to remove it 
	// here we need to create a list available to as the source
	// but if we have the same
	public List<XORCluster<T>> getRMAvailableSources(){
		List<XORCluster<T>> sourceList = new ArrayList<XORCluster<T>>();
		// check the pairList and then put the source here,
		for(XORCluster<T> cluster : cpList) {
			if(cluster.isAsSource()) 
				sourceList.add(cluster);
		}
		return sourceList;
	}
	
	// get the list availabel for the target given the source
	public List<XORCluster<T>> getRMAvailableTargets(XORCluster<T> source){
		List<XORCluster<T>> targetList = new ArrayList<XORCluster<T>>();
		for(XORClusterPair<T> pair : clusterPairList) {
			if(source.equals(pair.getSourceXORCluster())) {
				targetList.add(pair.getTargetXORCluster());
				return targetList;
			}
		}
		return targetList;
	}

	public void resetSourceTargetMark() {
		// TODO Auto-generated method stub
		for(XORCluster<T> cluster : cpList) {
			cluster.setAsSource(false);
			cluster.setAsTarget(false);
		}
		clusterPairList.clear();
	}

	public XORClusterPair<T> getPairBySource(XORCluster<T> source) {
		for(XORClusterPair<T> pair : clusterPairList) {
			if(source.equals(pair.getSourceXORCluster())) {
				return pair;
			}
		}
		return null;
	}
	
	
}
