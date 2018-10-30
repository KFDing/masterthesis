package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

import org.processmining.processtree.Node;


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
	private List<XORStructure<T>> beginXORList;
	private List<XORStructure<T>> endXORList;
	
	// to record the previous structure, or we only to remember the parents, it's already fine?? 
	public XORCluster<T> previousCluster;
	// here we see it has only one parent 
	private XORCluster<T> parentCluster;
	private List<XORCluster<T>> childrenCluster;

	private boolean hasXOR = false;
	
	// if it has no XOR structure, we need to remember the last and end node of this branch
	// or make them into a sequence
	private T beginNode;
	private T endNode;
	private T sBeginNode;
	private T sEndNode;
	
	public XORCluster(T key){
		keyNode = key;
		xorList =  new ArrayList<XORStructure<T>>();
		beginXORList = new ArrayList<XORStructure<T>>();
		endXORList = new ArrayList<XORStructure<T>>();
		childrenCluster = new ArrayList<XORCluster<T>>();
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
		if(xorList.isEmpty())
			return beginXORList;
		
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
			// we will set the first one in xorList as the begin
			beginXORList.add(xorList.get(0));
		}else if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for(XORCluster<T> cluster: childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				beginXORList.addAll(cluster.getBeginXORList());
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
		if(xorList.isEmpty())
			return endXORList;
		
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
			// we will set the first one in xorList as the begin
			endXORList.add(xorList.get(xorList.size() - 1));
		}else if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
			// beginXORList can be inferred from the xorList?? Right?? 
			// we really need to consider about the sequence and other stuffs here!! 
			// ok they can be across of them selfs, what we need is to get it from the childen parts
			for(XORCluster<T> cluster: childrenCluster) {
				// if there are some branches form it, what to do it ?? Nana, we need recursive run!!
				endXORList.addAll(cluster.getEndXORList());
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
		
		childrenCluster.add(cluster);
		
	}
	
	public void addXORStructure(XORStructure<T> xorS) {
		xorList.add(xorS);
		
		
	}
	// one method to generate pair into pairList from current information, 
	// but here are some other information, how to find them and combine them together?? 
	// their tree structure how to build them??? 
	// we have children structure or parents structure ?? 
	// if they are not, what to do ?? ancestor, still , somehow ?? The point is how to combine them together?? 
	// if we only use one of them to represent it , how long it lasts?
	// we could add the parent link, or children link

	public boolean hasXOR() {
		return hasXOR;
	}

	public void setHasXOR(boolean hasXOR) {
		this.hasXOR = hasXOR;
	}

	// what I'm writing is the direct relation of them, if not, what to do ??
	public List<XORPair<T>> createSpecialPair() {
		getBeginXORList();
		getEndXORList();
		// if it is sequence, then we need to create the in between xor list
		List<XORPair<T>> specialPairs =  new ArrayList<XORPair<T>>();
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE)) {
			for(int i=0; i< xorList.size(); i++) {
				if(!beginXORList.contains(xorList.get(i))) {
					specialPairs.addAll(createPairWithBeginXOR(xorList.get(i), false));
				}
				if(!endXORList.contains(xorList.get(i))) {
					specialPairs.addAll(createPairWithEndXOR(xorList.get(i), true));
				}
				// what if they belong to both the begin and end??? if only one, we don't create special list of them
			}	
		}else if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL)) {
			// if they are parallel, also, we don't consider about the branch situations, of it .. 
			// but could do it later, we just have the direct relation of it!! 
			// it is much interesting, we need to build the relation with themselves, haha 
			for(int i=0; i< beginXORList.size(); i++) {
				// we need to create special structure from it, but for pair, it's already fine
				// after this if there are still some childenBranches added to this parts, it's totally fine
				specialPairs.addAll(createPairWithBeginXOR(xorList.get(i), false));
				
			}
			
			for(int i=0; i< endXORList.size(); i++) {
				// we need to create special structure from it, but for pair, it's already fine
				// after this if there are still some childenBranches added to this parts, it's totally fine
				specialPairs.addAll(createPairWithEndXOR(xorList.get(i), false));
			}
		}
		return specialPairs;
	}
	/**
	 * it will mean the other structure to create with it 
	 * @param xorS, pre:: if the xorS is before the beginList
	 * @return
	 */
	public List<XORPair<T>> createPairWithBeginXOR(XORStructure<T> xorS, boolean pre) {
		List<XORPair<T>> bPairs =  new ArrayList<XORPair<T>>();
		for(int i=0; i< beginXORList.size(); i++) {
			if(xorS != beginXORList.get(i) ) {
				XORPair<T> pair = new XORPair<T>();
				if(pre) {
					pair.setSourceXOR(xorS);
					pair.setTargetXOR(beginXORList.get(i));
				}else {
					pair.setSourceXOR(beginXORList.get(i));
					pair.setTargetXOR(xorS);
				}
				bPairs.add(pair);
			}
		}
		return bPairs;
	}
	/**
	 * 
	 * @param xorS
	 * @param pre if the xorS is before the EndList
	 * @return
	 */
	public List<XORPair<T>> createPairWithEndXOR(XORStructure<T> xorS, boolean pre) {
		List<XORPair<T>> ePairs =  new ArrayList<XORPair<T>>();
		for(int i=0; i< endXORList.size(); i++) {
			if(xorS != endXORList.get(i) ) {
				XORPair<T> pair = new XORPair<T>();
				if(pre) {
					pair.setSourceXOR(xorS);
					pair.setTargetXOR(endXORList.get(i));
				}else {
					pair.setSourceXOR(endXORList.get(i));
					pair.setTargetXOR(xorS);
				}
				ePairs.add(pair);
			}
		}
		return ePairs;
	}

	public T getBeginNode() {
		// we need to create it whatever..
		return beginNode;
	}

	public void setBeginNode(T beginNode) {
		this.beginNode = beginNode;
	}

	public T getEndNode() {
		return endNode;
	}

	public void setEndNode(T endNode) {
		this.endNode = endNode;
	}

	public T getSBeginNode() {
		return sBeginNode;
	}

	public void setSBeginNode(T sbeginNode) {
		this.sBeginNode = sbeginNode;
	}

	public T getSEndNode() {
		return sEndNode;
	}

	public void setSEndNode(T sendNode) {
		this.sEndNode = sendNode;
	}

	public XORStructure<T> getXOR(Node subNode) {
		// TODO Auto-generated method stub
		for(XORStructure<T> xorS: xorList) {
			if(subNode.equals(xorS.getKeyNode()))
				return xorS;
		}
		return null;
	}
}
