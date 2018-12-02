package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;


/**
 * this class is used to represent the structure of seq, parallel, loop for xor structure, 
 * not sure about the xor big branch, what to do ?? I need to organize it !! 
 * attributes::
 *   -- keyNode:  to store the element from process tree
 *   -- xor structure list :: 
 *   -- the begin and end xor structure for it
 *   
 *      ++ begin and end xor structure::  
 *         -- seq: the first and end xor structure
 *         -- parallel: the xor from all branches 
 *         -- xor: we need to merge it and still we need to have the begin and end parts
 *   -- the previous process element structure of it 
 *   
 * methods:: 
 *   -- assign and get the xor begin and end,set them
 *   -- create pair combinations of it 
 *   -- if it is xor, we need to merge them, or we create the xor structure to use them??
 *      [consider it later]   
 * @author dkf
 *
 */
public class XORCluster<T> {
	private T keyNode;
	// in its branches it have it, like the branches?? Not sure, but we could use it, that's true
	private List<XORStructure<T>> xorList;

	// if in seq or parallel, there are a lot, then we need to use list to store them 
	// but anyway, we could use the branches of one list, but lost the structure information
	/**
	 * if it is one nested xor, then to to define its beginXORList and endXORList?? 
	 * for one leaf node, we can only create its branch and put them in parallel, 
	 * if we use xor structure, we can have different xor list, which is possible..That's true..
	 * but xor pair is one xor structure S to T, if we use the e, what to do ?? 
	 * if we have branch list here, but how do we give them an order here?? 
	 */
	private List<XORStructure<T>> beginXORList;
	private List<XORStructure<T>> endXORList;
	
	// to record the previous structure, or we only to remember the parents, it's already fine?? 
	public XORCluster<T> previousCluster;
	// here we see it has only one parent 
	public XORCluster<T> parentCluster;
	public List<XORCluster<T>> childrenCluster;

	// this represents if this cluster or its children cluster has xor structure
	// if it's in xor cluster, it means this is a nested xor cluster.
	private boolean hasXOR = false;
	// the available implies if we need to visit children cluster to get the xor list..
	private boolean available = false;
	// if this cluster is branch cluster
	private boolean branchCluster = false;
	
	public XORCluster(T key){
		keyNode = key;
		xorList =  new ArrayList<XORStructure<T>>();
		beginXORList = new ArrayList<XORStructure<T>>();
		endXORList = new ArrayList<XORStructure<T>>();
	}
	
	public T getKeyNode() {
		return keyNode;
	}

	public void setKeyNode(T keyNode) {
		this.keyNode = keyNode;
	}

	public List<XORStructure<T>> getXorList() {
		return xorList;
	}
	
	public List<XORStructure<T>> getBeginXORList() {
		// here maybe some problem, actually 
		if(!beginXORList.isEmpty())
			return beginXORList;
		
		
		if(isSeqCluster()) {
			// even if there are some elements in seq, but we're not sure about the sequence, so we can't do it 
			// we just go to the first childrenCluster
			XORCluster<T> cluster = childrenCluster.get(0);
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
			beginXORList.addAll(cluster.getBeginXORList());
			
		}else if(isParallelCluster()) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for(XORCluster<T> cluster: childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				beginXORList.addAll(cluster.getBeginXORList());
			}
			
		}else {
			// here we need to divide into two situations, one is nested xor cluster, one it not
			/**
			 * without xor block in xorcluster, hasXOR is false;
			 *     we do like this
			 * if hasXOR is true; 
			 *   and then we check the children cluster?? Should we check here, or we need to check outside
			 *   if it has the branches, what we need to do ? with another nested ones.. 
			 *   they just store all the parts here..
			 *   
			 *   we get all the parallel branches into one xor structure
			 */
			if(branchCluster || isNotNXORCluster())
				beginXORList.add(xorList.get(0));
			else if(isNXORCluster()){
				// it's nested xor 
				XORStructure<T> XORResult =  new XORStructure<T>(keyNode);
				for(XORCluster<T> cluster: childrenCluster) {
					// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
					beginXORList.addAll(cluster.getBeginXORList());
				}
				
				for(XORStructure<T> beginXOR: beginXORList) {
					// if they have same branches, then don't use them
					XORResult.mergeXORStructure(beginXOR);
				}
				beginXORList.clear();
				beginXORList.add(XORResult);
			}
		}
		return beginXORList;
	}

	
	public void setBeginXORList(List<XORStructure<T>> beginXORList) {
		this.beginXORList = beginXORList;
	}

	public void addBeginXORList(XORStructure<T> xorS) {
		beginXORList.add(xorS);
	}
	
	public List<XORStructure<T>> getEndXORList() {
		if(!endXORList.isEmpty())
			return endXORList;
		// then how to check if it is really empty or not?? OR we need to assign already the xor to it ?? 
		// I don't think so, we need to get it from the children cluster;; There is no cluster without the begin and end
		if(isSeqCluster()) {
			XORCluster<T> cluster = childrenCluster.get(childrenCluster.size() - 1);
			// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
			endXORList.addAll(cluster.getEndXORList());
			
		}else if(isParallelCluster()) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for(XORCluster<T> cluster: childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				endXORList.addAll(cluster.getEndXORList());
			}
			
		}else{
			
			if(branchCluster || !hasXOR())
				endXORList.add(xorList.get(xorList.size() - 1));
			else {
				// it's nested xor 
				XORStructure<T> XORResult = new XORStructure<T>(keyNode);
				for(XORCluster<T> cluster: childrenCluster) {
					// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
					endXORList.addAll(cluster.getEndXORList());
				}
				
				for(XORStructure<T> endXOR: endXORList) {
					// if they have same branches, then don't use them
					XORResult.mergeXORStructure(endXOR);
				}
				endXORList.clear();
				endXORList.add(XORResult);
			}
		}
		return endXORList;
	}

	public void setEndXORList(List<XORStructure<T>> endXORList) {
		this.endXORList = endXORList;
	}
	// we will see if this method is in need
	public void addEndXORList(XORStructure<T> xorS) {
		endXORList.add(xorS);
	}

	public XORCluster<T> getParentCluster() {
		return parentCluster;
	}

	public void setParentCluster(XORCluster<T> parentCluster) {
		this.parentCluster = parentCluster;
	}

	public List<XORCluster<T>> getChildrenCluster() {
		return childrenCluster;
	}

	public void setChildrenCluster(List<XORCluster<T>> childrenCluster) {
		this.childrenCluster = childrenCluster;
	}
	
	public void addChilrenCluster(XORCluster<T> cluster) {
		if(childrenCluster == null)
			childrenCluster = new ArrayList<XORCluster<T>>();
		childrenCluster.add(cluster);
	}
	
	public void addXORStructure(XORStructure<T> xorS) {
		xorList.add(xorS);
	}

	/**
	 * check if it has xor structure.
	 *  if it's un nested xor structure, it means, the children cluster has no XOR cluster
	 * @return
	 */
	public boolean hasXOR() {
		return hasXOR;
	}

	public void setHasXOR(boolean hasXOR) {
		this.hasXOR = hasXOR;
	}

	
	public boolean isSeqCluster() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE))
			return true;
		return false;
	}
	public boolean isParallelCluster() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}

	// we need to set the available into true or false
	// situation is that, if it's xor cluster, then return true
	// if not, we check the children, direct available, I mean 
	public void setAvailable(boolean value) {
		available = value;
	}
	
	public boolean isAvailable() {
		if(isNotNXORCluster() || branchCluster) // if it is xor, then ok, else check each child to see if they are available
			available = true;
		
		return available;
	}

	

	public void setBranchCluster(boolean branchCluster) {
		this.branchCluster = branchCluster;
	}

	public boolean isNotNXORCluster() {
		// TODO test if this cluster is not nested xor structure
		// if it is not nested xor cluster, then return true, 
		// nested xor cluster, then return false
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR) && !hasXOR())
			return true;
		
		return false;
	}
	
	// if it is nested xor cluster
	public boolean isNXORCluster() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR) && hasXOR())
			return true;
		return false;
	}
	// here we mean a cluster without xor and not xor 
		public boolean isPureBranchCluster() {
			if(!hasXOR && !isXORCluster()) 
				return true;
			return false;
		}
		
	// only for pure branch, we have this
	List<T> beginNodeList;
	public List<T> getBeginNodeList() {
		// TODO Auto-generated method stub, so should we also record its childrenCluster, 
		// yap, we need to do it 
		beginNodeList = new ArrayList<T>();
		// if it is just one single node, so what to to then?? 
		if(isLeafCluster()) {
			beginNodeList.add(keyNode);
		}else if(isSeqCluster()) {
			beginNodeList.addAll(childrenCluster.get(0).getBeginNodeList());
		}else if(isParallelCluster()) {
			for(XORCluster<T> child: childrenCluster) {
				beginNodeList.addAll(child.getBeginNodeList());
			}
		}
		
		return beginNodeList;
	}

	// but after we do this, it means that we need to record all the childrenCluster of this branch
	// Could we stop them here?? No, we can't because we need data for parallel
	// assign their children cluster but assign them if they are leaf node
	// we should record that we are in a branch, and then record the cluster of them .'
	// if we meet one parallel, then we record its seq branch, but until we reach the leaf node, that's how it works
	// yes, we need to remember all things relative to xor structure
	
	// for pure branch, there is the End node list
	List<T> endNodeList;
	public List<T> getEndNodeList() {
		// TODO Auto-generated method stub
		endNodeList = new ArrayList<T>();
		if(isLeafCluster()) {
			endNodeList.add(keyNode);
		}else if(isSeqCluster()) {
			endNodeList.addAll(childrenCluster.get(childrenCluster.size() -1).getEndNodeList());
		}else if(isParallelCluster()) {
			for(XORCluster<T> child: childrenCluster) {
				endNodeList.addAll(child.getEndNodeList());
			}
		}
		return endNodeList;
	}

	public boolean isXORCluster() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.XOR))
			return true;
		
		return false;
	}
	
	boolean isLeaf =false;
	public boolean isLeafCluster() {
		return isLeaf;
	}
	public void setIsLeaf(boolean value) {
		isLeaf = value;
	}
}
