package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is aaded to create a transition layer to add more functions on the programs.
 * We hope it could work like it expected, but sure, there should be a uniform here. 
 * could we create a general class for them both, one is for cluster, one is for the structure, somehow?? 
 * @author dkf
 *
 */
public class XORClusterPair<T> {
	// pair composites  source and target cluster
	XORCluster<T> sourceXORCluster;
	XORCluster<T> targetXORCluster;
	
	// but it can be also the children cluster
	List<XORClusterPair<T>> childrenClusterPair;
	// this is for the real concrete structure...
	boolean available;
	
	// why there is a list of pairList but not the normal pair of it ??
	List<XORPair<T>> pairList, ltPairList;
	// this cluster pair is complete only if all the branches are complete 
	boolean complete;
	
	// if there is no children cluster ,we check it is availabel, else, we need to look for children
	public boolean isAvailable() {
		if(childrenClusterPair.isEmpty())
			available =true;
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public XORClusterPair(XORCluster<T> source, XORCluster<T> target){
		sourceXORCluster = source;
		targetXORCluster = target;
		
		childrenClusterPair = new ArrayList<XORClusterPair<T>>();
		pairList =  new ArrayList<XORPair<T>>();
	}
	
	public XORCluster<T> getSourceXORCluster() {
		return sourceXORCluster;
	}
	public void setSourceXORCluster(XORCluster<T> sourceXORCluster) {
		this.sourceXORCluster = sourceXORCluster;
	}
	public XORCluster<T> getTargetXORCluster() {
		return targetXORCluster;
	}
	public void setTargetXORCluster(XORCluster<T> targetXORCluster) {
		this.targetXORCluster = targetXORCluster;
	}
	
	public void addChildClusterPair(XORClusterPair<T> clusterPair) {
		childrenClusterPair.add(clusterPair);
	}
	
	public void addAllChildClusterPair(List<XORClusterPair<T>> clusterPairList) {
		childrenClusterPair.addAll(clusterPairList);
	}
	
	public void addAllXORPair(List<XORPair<T>> pairs) {
		pairList.addAll(pairs);
	}
	
	public List<XORPair<T>> getXORPairList() {
		return pairList;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public List<XORPair<T>> getLTXORPair(){
		return ltPairList;
	}
	
	public boolean testComplete() {
		ltPairList =  new ArrayList<XORPair<T>>();
		complete = true;
		if(isAvailable()) {
			for(XORPair<T> pair: pairList) {
				if(pair.hasCompleteConnection()) {
					// we need to record it here and don't move it here, else we need to remove it, anyway..
					// should we remove it ?? Actually not, we need to record it too
					ltPairList.add(pair);
				}else {
					complete = false;
				}
			}
		}else {
			// we need to test the children cluster and find them out
			for(XORClusterPair<T> cPair : childrenClusterPair) {
				complete &= cPair.testComplete();
			}
		}
		return complete;
	}
	
	
}
