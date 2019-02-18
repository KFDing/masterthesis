package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * for code simplify, we use this method to represent xor branch for the new method
 * 
 * we have elements::
 * -- keyNode
 * -- List: beginNodes
 * -- List: endNodes
 * 
 * Methods:
 *   
 * @author dkf
 *
 */
public class XORBranch<T> {
	private T keyNode;
	
	private List<T> beginNodes;
	private List<T> endNodes;
	private List<XORBranch<T>> children;
	// we need the children node to make sure that we get the begin and end node on it 
	// but if we go deeper, it could really really deeper, so we need to change another way for it ??
	// we have a represent for it just the first leaf node is ok..
	boolean isLeaf = false;

	public XORBranch(T keyNode){
		this.setKeyNode(keyNode);
		
		beginNodes = new ArrayList<T>();
		endNodes = new ArrayList<T>();
		children = new ArrayList<XORBranch<T>>();
	}

	public T getKeyNode() {
		return keyNode;
	}

	public void setKeyNode(T keyNode) {
		this.keyNode = keyNode;
	}

	public void setIsLeaf(boolean b) {
		// TODO Auto-generated method stub
		isLeaf = b;
	}
	
	public boolean isSeq() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.SEQUENCE))
			return true;
		return false;
	}
	
	public boolean isAnd() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.PARALLEL))
			return true;
		return false;
	}
	public boolean isLoop() {
		if(keyNode.getClass().getSimpleName().equals(ProcessConfiguration.LOOP))
			return true;
		return false;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}

	public List<T> getBeginNodes() {
		if(isLeaf) {
			beginNodes.add(keyNode);
		}else if(isSeq()) {
			// the first begin of nodes in it, but we need to know goes down into it 
			XORBranch<T> branch = children.get(0);
			beginNodes.addAll(branch.getBeginNodes());
		}else if(isAnd()) {
			for(XORBranch<T> branch: children) {
				beginNodes.addAll(branch.getBeginNodes());
			}
		}else if(isLoop()) {
			XORBranch<T> branch = children.get(0);
			beginNodes.addAll(branch.getBeginNodes());
		}
		
		return beginNodes;
	}

	public List<T> getEndNodes() {
		return endNodes;
	}

	public void addChildren(XORBranch<T> subBranch) {
		// TODO Auto-generated method stub
		
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return keyNode.toString();
	}
	
	
	
}
