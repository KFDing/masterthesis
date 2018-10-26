package org.processmining.plugins.ding.process.dfg.model;

public class XORBranch<T>{
	private T beginNode = null;
	private T endNode = null;
	private boolean open = true;
	
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
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	
}
