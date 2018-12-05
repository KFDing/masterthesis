package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.Collection;
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

	List<NewLTConnection<T>> connections, ltConnections;
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
		if(sourceXORCluster.isPureBranchCluster() && targetXORCluster.isPureBranchCluster()) {
			// we can have first and last children in Cluster, and then we need to use them 
			// create the LTConnection of those, we don't need XORBranch again, only the nodes on it.
			connections =  new ArrayList<NewLTConnection<T>>();
			available = true;
			// we deal with the situation of seq and parallel, but we need to do consider the 
			for(T sEndNode: sourceXORCluster.getEndNodeList()) {
				for(T tBeginNode : targetXORCluster.getBeginNodeList()) {		
					connections.add(new NewLTConnection(sEndNode,tBeginNode));
				}
			}
			
		}else if(sourceXORCluster.isXORCluster() && targetXORCluster.isXORCluster()) {
			branchClusterPair = new ArrayList<XORClusterPair<T>>();
			for(XORCluster<T> scluster: sourceXORCluster.getChildrenCluster())
				for(XORCluster<T> tcluster: targetXORCluster.getChildrenCluster()){
					branchClusterPair.add(new XORClusterPair(scluster, tcluster, true));
				}
			
		}else {
			branchClusterPair = new ArrayList<XORClusterPair<T>>();
			for(XORCluster<T> scluster: sourceXORCluster.getEndXORList())
				for(XORCluster<T> tcluster: targetXORCluster.getBeginXORList()){
					
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

	public List<NewLTConnection<T>> getLtConnections() {
		return ltConnections;
	}

	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	
	public boolean testComplete() {
		complete = true;
		if(available) {
			// only pure branch to pure branch, we can say it, now check their connection
			if(!connections.isEmpty()) {

				ltConnections =  new ArrayList<NewLTConnection<T>>();
				for(NewLTConnection<T> conn: connections) {
					if(conn.testSupportConnection()) {
						// if it support the connection, then we return the true
						ltConnections.add(conn);
					}
				}
				
				if(ltConnections.size() < connections.size())
					complete = false;
				if(ltBranchClusterPair.isEmpty())
					connected = false;
			}
			
		}else {
			// we need to test the children cluster and find them out, but we need to record the complete branch
			ltBranchClusterPair =  new ArrayList<XORClusterPair<T>>();
			for(XORClusterPair<T> cPair : branchClusterPair) {
				if(cPair.isConnected()) {
					complete &= cPair.testComplete();
					ltBranchClusterPair.add(cPair);
				}
			}
			
			// if there is some connection with ltClusterPair, if there is, still we need to add them here 
			// it is just not complete
			if(ltBranchClusterPair.size() > 0)
				connected = true;
			
		}
		return complete;
	}

	public Collection<? extends NewLTConnection<T>> getLTConnection() {
		// TODO return connections of this pair
		// what if we use it later, so we still need to keep reference to it, but anyway by later use
		if(connections!= null)
			return connections;
		else { // if it is not pure then we need to get the branchPaiur
			connections = new ArrayList<NewLTConnection<T>>();
			
			for(XORClusterPair<T> cPair : branchClusterPair) {
				connections.addAll(cPair.getLTConnection());
			}
			return connections;
		}
	}

	boolean connected = false;
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return connected;
	}
	
	
}
