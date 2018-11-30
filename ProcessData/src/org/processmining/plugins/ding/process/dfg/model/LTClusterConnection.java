package org.processmining.plugins.ding.process.dfg.model;

import java.util.List;

/**
 * we need an array in length of 6, we reserve one for existing, 
 * if we change relation at long-term dependency, we need to use cluster as the fisrt Cluster and second one for this
 * and also, we rename it to make better understand.
 * 
 * If we check the LTConnection, we need to go deeper for it 
 * @author dkf
 *
 * @param <T>
 */
public class LTClusterConnection<T>{
	// here we should take care of the connectionValues for it, 
	// actually it's fine for here.. If we use it to represent the connection, only true or false doesn matter, so
	// maybe for the higher connection, there is no need?? Really, let's just say, 
	// the connection of S to parallel (d1, d2), we can't not really check that?? 
	// if there are the real connection of each Cluster, we need to create a higer class for it to mark them others..
	// and for this connection, we need to say, it is about the XORCluster connection
	// we don't have any values for it, only if they have complete connection, or not 
	// we have the most highest sourceCluster and then targetCluster
	
	// for not nested cluster, we can give it directly here and test its connection. 
	// we can generate one branchCluster and then add to it..
	// so it's about the sub branch cluster
	XORCluster<T> sourceCluster;
	XORCluster<T> targetCluster;
	
	
	boolean supportConnection = false;
	

	public LTClusterConnection(XORCluster<T> first, XORCluster<T> second) {
		sourceCluster = first;
		targetCluster = second;
	}
	
	
	public LTClusterConnection(XORCluster<T> first, XORCluster<T> second, boolean support) {
		sourceCluster = first;
		targetCluster = second;
		supportConnection = support;
	}
	
	public XORCluster<T> getSourceCluster() {
		return sourceCluster;
	}

	public void setSourceCluster(XORCluster<T> sourceCluster) {
		this.sourceCluster = sourceCluster;
	}

	public XORCluster<T> getTargetCluster() {
		return targetCluster;
	}

	public void setTargetCluster(XORCluster<T> targetCluster) {
		this.targetCluster = targetCluster;
	}
	
	public boolean isSupportConnection() {
		return supportConnection;
	}

	public boolean testSupportConnection() {
		if(sourceCluster.isBranchCluster() && targetCluster.isBranchCluster()) {
			XORBranch<T> sourceBranch = sourceCluster.getBeginXORList().get(0).getBranches().get(0);
			XORBranch<T> targetBranch = targetCluster.getBeginXORList().get(0).getBranches().get(0);
			
			LTConnection<T> conn = new LTConnection<T>(sourceBranch, targetBranch);
			
			supportConnection = conn.testSupportConnection();
		}else {
			// if the sourceCluster or targetCluster is impure and contains others, what to do ?? 
			// we need to go into deeper and then get it 
			// could we also assume that sourceCluster has concrete xor cluster??
			List<XORStructure<T>> beginXORs= targetCluster.getBeginXORList();
			XORBranch<T> sourceBranch = sourceCluster.getBeginXORList().get(0).getBranches().get(0);
			if(beginXORs.size() <2) {
				// if we get only one beginXOR structure which is concrete, and we do ??
				
				for (XORBranch<T> targetBranch : beginXORs.get(0).getBranches()) {
					LTConnection<T> tempConn = new LTConnection<T>(sourceBranch, targetBranch);
					if(!tempConn.testSupportConnection()) {
						supportConnection = false;
					}
				}
			}

			// in seq, now we need to test the relation of each beginXORs with the branchCluster
			
			/// in paralell
			
			
			
		}
		return supportConnection;
	}

}
