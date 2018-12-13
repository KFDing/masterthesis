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
	// we test the source and target kinds and then create corresponding elements
	// should we return it, not really!! but we need to remember its connection
	// 
	public void initialize() {
		
		// I need to test the branch situations, one is the branch and has no XOR, 
		if(sourceXORCluster.isPureBranchCluster() && targetXORCluster.isPureBranchCluster()) {
			available = true;
			
			// we can have first and last children in Cluster, and then we need to use them 
			// create the LTConnection of those, we don't need XORBranch again, only the nodes on it.
			connections =  new ArrayList<NewLTConnection<T>>();
			
			// we deal with the situation of seq and parallel, but we need to do consider the 
			for(T sEndNode: sourceXORCluster.getEndNodeList()) {
				for(T tBeginNode : targetXORCluster.getBeginNodeList()) {		
					// we separate them.. But how to add them back, by checking the type 
					// of cluster, if they are just seq, what to do then?? If they are parallel
					// then we need to fgenerate two plaes ??? Like this, 
					// but they are just branch to branch, then about the or structuer, we need something more
					connections.add(new NewLTConnection(sEndNode,tBeginNode));
				}
			}
			
		}else if(sourceXORCluster.isXORCluster()){ 
			// should we generate cluster specialy for source and also special for the target?? Somehow??
			// I think it's alreday fine, until now
			branchClusterPair = new ArrayList<XORClusterPair<T>>();
			
			if(targetXORCluster.isXORCluster()) {
				// two are both xor cluster and not nested, so generate the branchClusterPair
				for(XORCluster<T> scluster: sourceXORCluster.getEndXORList())
					// even if we get the end xor list, we need to make sure that the single
					// cluster can also be listed here to combine the childrenCluster.. 
					// so and so... change code and see how to make it
					for(XORCluster<T> tcluster: targetXORCluster.getBeginXORList()){
						// if the source XORCluster is seq, and target is parallel, then we need begin and end 
						// because in each branch, it can be different 
						// it stops if it meets one xor cluster; but then if we check the children, it keeps going.
						branchClusterPair.add(new XORClusterPair(scluster, tcluster, true));
					}
				
			}
		
		}else {
			// because we have change the codes, or we don't change them, but according to the cluster
			// situation?? 
			// if we do this, at that step, we should actually add something there, else, what to do ??
			// if we meet parallel cluster, it is in nested xor, but if there is not nested xor,
			// its structure can't be recorded. Then we need to find a way to remember it
			// 
			
			System.out.println("Could this situation happens?? Not really, because of what ?? But it can happen");
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
		if(ltConnections!= null)
			return ltConnections;
		else { // if it is not pure then we need to get the branchPaiur
			ltConnections = new ArrayList<NewLTConnection<T>>();
			
			for(XORClusterPair<T> cPair : ltBranchClusterPair) {
				ltConnections.addAll(cPair.getLtConnections());
			}
			return ltConnections;
		}

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
			}
			
		}else {
			// we need to test the children cluster and find them out, but we need to record the complete branch
			ltBranchClusterPair =  new ArrayList<XORClusterPair<T>>();
			for(XORClusterPair<T> cPair : branchClusterPair) {
				if(cPair.testComplete()) {
					ltBranchClusterPair.add(cPair);
				}
			}
			
			if(ltBranchClusterPair.size() < branchClusterPair.size() && ltBranchClusterPair.size() > 0)
				complete = false;
			
		}
		return complete;
	}

	public List< NewLTConnection<T>> getConnection() {
		// TODO return connections of this pair
		// what if we use it later, so we still need to keep reference to it, but anyway by later use
		if(connections!= null)
			return connections;
		else { // if it is not pure then we need to get the branchPaiur
			connections = new ArrayList<NewLTConnection<T>>();
			
			for(XORClusterPair<T> cPair : branchClusterPair) {
				connections.addAll(cPair.getConnection());
			}
			return connections;
		}
	}
	
	
}
