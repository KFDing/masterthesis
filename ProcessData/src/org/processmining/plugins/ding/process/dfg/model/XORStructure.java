package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

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
}


