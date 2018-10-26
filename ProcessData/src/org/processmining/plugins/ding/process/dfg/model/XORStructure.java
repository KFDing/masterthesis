package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

import org.processmining.processtree.Node;

/**
 * this class is used to create the xor class for process tree and also for Petri net
 * its attributes are: 
 * -- xor kay node:: process tree is the subtree node
 * -- branches: it can be a list:
 *    ++ create an inner class for branch but public 
 *    ++ one branch includes : begin node and end node [but we need to take care 
 *           we choose the right begin and end to build global dependency in it]
 * its methods: 
 * ??? should we just use the Element or we use the Process Tree with in it ?? 
 * But anyway there should be some connection of process tree and petri net
 * it can be Petrinet Node and also ProcessTreeElement
 * @author dkf
 *
 */
public class XORStructure<T> {
	private T keyNode;
	private List<XORBranch<T>> branches;
	
	private int numOfBranches;
	// to test if it get all the xor branches
	private boolean open;
	
	// information to record the order of xorstructure
	public XORStructure<T> next = null;

	public XORStructure<T> previous = null;
	
	// in the same big structure, like in sequence or parallel, should we also record such information?? 
	// if we record it means 
	public Node parent;
	public XORStructure<T> nextInSameLevel = null;
	public XORStructure<T> previousInSameLevel = null;
	
	
	public XORStructure(T key){
		setKeyNode(key);
		branches = new ArrayList<XORBranch<T>>();
	}
	
	public int getNumOfBranches() {
		numOfBranches = branches.size();
		return numOfBranches;
	}
	// set the expected value of num of branches
	public void setNumOfBranches(int num) {
		numOfBranches = num;
	}
	
	public T getKeyNode() {
		return keyNode;
	}
	public void setKeyNode(T keyNode) {
		this.keyNode = keyNode;
	}

	public List<XORBranch<T>> getBranches() {
		return branches;
	}

	public void addBranch(XORBranch<T> branch) {
		branches.add(branch);
	}
	
	public void addBranchList(List<XORBranch<T>> branches) {
		this.branches.addAll(branches);
	}

	public void mergeXORStructure(XORStructure<T> toBeMerged) {
		// put the branches into the main xor structure 
		addBranchList(toBeMerged.getBranches());
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public XORStructure<T> getNext() {
		return next;
	}

	public void setNext(XORStructure<T> next) {
		this.next = next;
	}

	public XORStructure<T> getPrevious() {
		return previous;
	}

	public void setPrevious(XORStructure<T> previous) {
		this.previous = previous;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node p) {
		parent = p;
	}

	public XORStructure<T> getNextInSameLevel() {
		return nextInSameLevel;
	}

	public void setNextInSameLevel(XORStructure<T> nextInSameLevel) {
		this.nextInSameLevel = nextInSameLevel;
	}

	public XORStructure<T> getPreviousInSameLevel() {
		return previousInSameLevel;
	}

	public void setPreviousInSameLevel(XORStructure<T> previousInSameLevel) {
		this.previousInSameLevel = previousInSameLevel;
	}

	public void setBranches(List<XORBranch<T>> branches) {
		this.branches = branches;
	}
}


