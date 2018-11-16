package org.processmining.plugins.ding.process.dfg.model;

public class XORBranch<T>{
	private T parentNode = null;
	// the keyNode is :: leaf node, or the operator
	private T keyNode = null;
	private T beginNode = null;
	private T endNode = null;
	private boolean open = true;
	
	public XORBranch(T node){
		keyNode = node;
	}
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
	public T getParentNode() {
		return parentNode;
	}
	public void setParentNode(T parentNode) {
		this.parentNode = parentNode;
	}
	public T getKeyNode() {
		return keyNode;
	}
	public void setKeyNode(T keyNode) {
		this.keyNode = keyNode;
	}
	
}
