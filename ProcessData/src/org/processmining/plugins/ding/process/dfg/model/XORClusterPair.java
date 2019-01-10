package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is only for storing the cluster pair with xor block, it means we only
 * create pair of [Not NXor | NXor]* [Not NXor | NXor] .
 * for Not NXor * NOt NXor, we treate them like before, but with mark its kind,
 * but if it is NXor, we need to create more relation on it..
 * @author dkf
 *
 */
public class XORClusterPair<T> {
	// pair composites  source and target cluster
	XORCluster<T> sourceXORCluster;
	XORCluster<T> targetXORCluster;
	boolean inBranch = false;
	

	// we can create branchClusterPair by using SBranch * TBranch
	// not add them here, but we need to make sure, it has an end	 
	List<XORClusterPair<T>> branchClusterPair = null, ltBranchClusterPair;

	List<LTRule<XORCluster<T>>> connections, ltConnections;
	// for Not NXor * NOt NXor, should we go deeper, more general, we need to go to branchCluster and check it 
	boolean available;
	
	// this cluster pair is complete only if all the branches are complete 
	boolean complete;
	
	// it is concrete Branch * concrete Branch!! It is available 
	// but even it is like this.. We don't need to worry about in the deep part
	public boolean isAvailable() {
		if(branchClusterPair ==null || branchClusterPair.isEmpty())
			available =true;
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public XORClusterPair(){
		// don't create it here and maybe we should create special branchcluster to it?? 
		// or we need to 
	}
	
	public boolean isInBranch() {
		return inBranch;
	}

	public void setInBranch(boolean inBranch) {
		this.inBranch = inBranch;
	}
	
	public XORClusterPair(XORCluster<T> source, XORCluster<T> target, boolean inBranch){
		sourceXORCluster = source;
		targetXORCluster = target;
		this.inBranch = inBranch;
		
		initialize();
	}
	
	
	/**
	 * this initialize is used to get the branchPair from source and target cluster
	 * but when the sBranch and tBranch changes its form, and with xor, we need to take care of this
	 * the most important thing is if it has xor and then get the xorList of it then
	 */
	public void initialize() {
		// a way to organize them, only create connection when they are pure
		// if xor using childrenCluster, else use xor cluster for both source and target
		if(sourceXORCluster.isPureBranchCluster() && targetXORCluster.isPureBranchCluster()) {
			// create connection only with condition like this
			connections =  new ArrayList<LTRule<XORCluster<T>>>();
			available = true;
			// we deal with the situation of seq and parallel, but we need to do consider the 
			LTRule<XORCluster<T>> conn = new LTRule<XORCluster<T>>(sourceXORCluster, targetXORCluster);
			connections.add(conn);
		}else {
			List<XORCluster<T>> sclusterList = new ArrayList<XORCluster<T>>();
			if(sourceXORCluster.isPureBranchCluster()) {
				sclusterList.add(sourceXORCluster);
			}else if(sourceXORCluster.isXORCluster()) {
				sclusterList.addAll(sourceXORCluster.getChildrenCluster());
			}else {
				// here are something wrong, because ?? if we have seq with parallel, then back to
				// endxor list is deeper than before, so we need to goes back into another structure
				// if seq or parallel with xor structure, one : directly includes xor 
				// or nested with xor:: if nested, then we need to go deeper!! 
				// how to check if the last experiment includes it?? 
				
				// seq and with xor
				if(sourceXORCluster.isSeqCluster()) {
					sclusterList.add(sourceXORCluster.getChildrenCluster().get(
							sourceXORCluster.getChildrenCluster().size()-1));
					
				}else if(sourceXORCluster.isParallelCluster()) {
					// add all the children cluster into it, still the children cluster
					sclusterList.addAll(sourceXORCluster.getChildrenCluster());
				}
				
				
			}
			
			List<XORCluster<T>> tclusterList = new ArrayList<XORCluster<T>>();
			if(targetXORCluster.isPureBranchCluster()) {
				tclusterList.add(targetXORCluster);
			}else if(targetXORCluster.isXORCluster()) {
				tclusterList.addAll(targetXORCluster.getChildrenCluster());
			}else {
				// we can't use the end xor list and another list, only this one is fine
				// seq and with xor
				if(targetXORCluster.isSeqCluster()) {
					tclusterList.add(targetXORCluster.getChildrenCluster().get(0));
					
				}else if(targetXORCluster.isParallelCluster()) {
					// add all the children cluster into it, still the children cluster
					tclusterList.addAll(targetXORCluster.getChildrenCluster());
				}
				
				// tclusterList.addAll(targetXORCluster.getBeginXORList());
			}
			
			// here to use the sclusterList and tClusterList to create new branch pair
			branchClusterPair = new ArrayList<XORClusterPair<T>>();
			for(XORCluster<T> scluster: sclusterList)
				for(XORCluster<T> tcluster: tclusterList){
					branchClusterPair.add(new XORClusterPair(scluster, tcluster, true));
				}
					
			}
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
	
	public void addBranchClusterPair(XORClusterPair<T> clusterPair) {
		branchClusterPair.add(clusterPair);
	}
	
	public void addAllBranchClusterPair(List<XORClusterPair<T>> clusterPairList) {
		branchClusterPair.addAll(clusterPairList);
	}
	
	public List<XORClusterPair<T>> getBranchClusterPair() {
		return branchClusterPair;
	}
	
	public XORClusterPair<T> findLTBranchClusterPair(XORCluster<T> sourceCluster,
			XORCluster<T> targetCluster) {
		
		for(XORClusterPair<T> pair: ltBranchClusterPair) {
			if(pair.getSourceXORCluster().equals(sourceCluster) && pair.getTargetXORCluster().equals(targetCluster))
				return pair;
		}
		
		return null;
	}


	public List<XORClusterPair<T>> getLtBranchClusterPair() {
		return ltBranchClusterPair;
	}

	public List<LTRule<XORCluster<T>>> getLtConnections() {
		return ltConnections;
	}

	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	
	public boolean testConnected() {
		connected = false;
		complete = true;
		if(available) {
			// only pure branch to pure branch, we can say it, now check their connection
			if(!connections.isEmpty()) {

				ltConnections =  new ArrayList<LTRule<XORCluster<T>>>();
				for(LTRule<XORCluster<T>> conn: connections) {
					conn.testSupportConnection();
					if(conn.isSupportConnection()) {
						// if it support the connection, then we return the true
						ltConnections.add(conn);
					}
				}
				
				if(ltConnections.size() < connections.size())
					complete = false;
				if(!ltConnections.isEmpty())
					connected = true;
			}
			// suddenly, I realize defining the target, it is already fixing the source, and we also choose the situation 
			// when they can happen together, so no worry.
		}else {
			// we need to test the children cluster and find them out, but we need to record the complete branch
			ltBranchClusterPair =  new ArrayList<XORClusterPair<T>>();
			for(XORClusterPair<T> cPair : branchClusterPair) {
				cPair.testConnected();
				if(cPair.isConnected()) {
					complete &= cPair.isComplete();
					ltBranchClusterPair.add(cPair);
				}else {
					// not connected,so what to do then??
					complete &= false;
				}
			}
			
			// if there is some connection with ltClusterPair, if there is, still we need to add them here 
			// it is just not complete
			if(ltBranchClusterPair.size() > 0)
				connected = true;
			
		}
		return connected;
	}

	public List<LTRule<XORCluster<T>>> getConnection() {
		// TODO return connections of this pair
		// what if we use it later, so we still need to keep reference to it, but anyway by later use
		if(connections!= null)
			return connections;
		else { // if it is not pure then we need to get the branchPaiur
			connections = new ArrayList<LTRule<XORCluster<T>>>();
			
			for(XORClusterPair<T> cPair : branchClusterPair) {
				connections.addAll(cPair.getConnection());
			}
			return connections;
		}
	}

	boolean connected = true;
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return connected;
	}
	
	
}
