package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
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
		if(tree.size() < 1) {
			System.out.println("The tree is empty");
			
		}
		
		Set<Node> aSet = getAllXORAncestors(tree);
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
						if(!sourceCluster.equals(targetCluster)) {
							clusterPairList.add(createClusterXORPair(sourceCluster, targetCluster));
						}
						sourceCluster = targetCluster;
						i++;
					}
				}
			}
			cluster.setAvailable(true);
		}
	}

	
	private XORClusterPair<T> createClusterXORPair(XORCluster<T> sourceCluster, XORCluster<T> targetCluster) {
		// we create one pair cluster for it.. 
		XORClusterPair<T> pair = new XORClusterPair<T>(sourceCluster, targetCluster);
		// after this creation, we need to get initialize ??? 
		// but initialize the pair needs a lot, like?? 
		// pair.initialize();
		
		// here we can add the list of NewLTConnection
		connList.addAll(pair.getLTConnection());
		
		return pair;
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
			System.out.println("visit Node name: " + node.getClass().getSimpleName());
			XORCluster<T> cluster =  new XORCluster<T>((T) node);
			cluster.setIsLeaf(true);
			clusterList.add(cluster);
			return cluster;
		}
		
		Block block =(Block) node;
		System.out.println("visit block name: " + block.getClass().getSimpleName());
		
		
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
				// for another cluster which is not in the xor branch
				// here are the structure in xor branch, how to add them then??? 
				// we need to add all the childrenCluster there until reach the end.. not so nice
				// but do we really need to go so far?? to record all the things here.. Nana, 
				// right now, it's an easy way to do
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
	
	public Set<Node> getAllXORAncestors(ProcessTree tree) {
		// TODO get all xor ancestors in process tree including the xor structure?
		// no, so we can distinguish nested and not nested xor block. 
		
		
		Set<Node> aSet = new HashSet<Node>();
		// 2. get the ancestors of them 
		for(Node xorNode: getAllXORs(tree)) {
			aSet.addAll(xorNode.getParents());
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

	public XORClusterPair<T> findClusterPair(XORCluster<T> sourceCluster,
			XORCluster<T> targetCluster) {
		for(XORClusterPair<T> pair: clusterPairList) {
			if(pair.getSourceXORCluster().equals(sourceCluster) && pair.getTargetXORCluster().equals(targetCluster))
				return pair;
		}
		
		return null;
	}
	
	
	
}
