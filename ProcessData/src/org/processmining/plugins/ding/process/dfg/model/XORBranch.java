package org.processmining.plugins.ding.process.dfg.model;

public class XORBranch<T>{
	private T beginNode = null;
	private T endNode = null;
	
	public T getBeginNode() {
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
	
}
