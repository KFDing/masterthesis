package org.processmining.incorporatenegativeinformation.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

/**
 * this class is only for storing the cluster pair with xor block, it means we
 * only create pair of [Not NXor | NXor]* [Not NXor | NXor] . for Not NXor * NOt
 * NXor, we treate them like before, but with mark its kind, but if it is NXor,
 * we need to create more relation on it..
 * 
 * @author dkf
 *
 */
public class XORClusterPair<T> {
	// pair composites  source and target cluster
	XORCluster<T> sourceXORCluster;
	XORCluster<T> targetXORCluster;
	boolean inBranch = false;

	// one method to store the names of new added petri nodde for this pair
	Map<String, PetrinetNode> pnNodeMap;

	// we can create branchPairList by using SBranch * TBranch
	// not add them here, but we need to make sure, it has an end	 
	List<XORClusterPair<T>> branchPairList = null, ltBranchPairList;

	List<LTRule<XORCluster<T>>> connections, ltConnections;
	// for Not NXor * NOt NXor, should we go deeper, more general, we need to go to branchCluster and check it 
	boolean available;

	// this cluster pair is complete only if all the branches are complete 
	boolean complete;

	// it is concrete Branch * concrete Branch!! It is available 
	// but even it is like this.. We don't need to worry about in the deep part
	public boolean isAvailable() {
		if (branchPairList == null || branchPairList.isEmpty())
			available = true;
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public XORClusterPair() {
		// don't create it here and maybe we should create special branchcluster to it?? 
		// or we need to 
	}

	public boolean isInBranch() {
		return inBranch;
	}

	public void setInBranch(boolean inBranch) {
		this.inBranch = inBranch;
	}

	public XORClusterPair(XORCluster<T> source, XORCluster<T> target, boolean inBranch) {
		sourceXORCluster = source;
		targetXORCluster = target;
		this.inBranch = inBranch;
		//		
		//		source.setAsSource(true);
		//		target.setAsTarget(true);
		initialize();
	}

	/**
	 * this initialize is used to get the branchPair from source and target
	 * cluster but when the sBranch and tBranch changes its form, and with xor,
	 * we need to take care of this the most important thing is if it has xor
	 * and then get the xorList of it then
	 */
	public void initialize() {
		// a way to organize them, only create connection when they are pure
		// if xor using childrenCluster, else use xor cluster for both source and target
		if (sourceXORCluster.isPureBranchCluster() && targetXORCluster.isPureBranchCluster()) {
			// create connection only with condition like this
			connections = new ArrayList<LTRule<XORCluster<T>>>();
			available = true;
			// we deal with the situation of seq and parallel, but we need to do consider the 
			LTRule<XORCluster<T>> conn = new LTRule<XORCluster<T>>(sourceXORCluster, targetXORCluster);
			connections.add(conn);
		} else {
			List<XORCluster<T>> sclusterList = new ArrayList<XORCluster<T>>();
			if (sourceXORCluster.isPureBranchCluster()) {
				sclusterList.add(sourceXORCluster);
			} else if (sourceXORCluster.isXORCluster()) {
				sclusterList.addAll(sourceXORCluster.getChildrenCluster());
			} else {
				// here are something wrong, because ?? if we have seq with parallel, then back to
				// endxor list is deeper than before, so we need to goes back into another structure
				// if seq or parallel with xor structure, one : directly includes xor 
				// or nested with xor:: if nested, then we need to go deeper!! 
				// how to check if the last experiment includes it?? 

				// seq and with xor
				if (sourceXORCluster.isSeqCluster()) {
					sclusterList.add(sourceXORCluster.getChildrenCluster()
							.get(sourceXORCluster.getChildrenCluster().size() - 1));

				} else if (sourceXORCluster.isParallelCluster()) {
					// add all the children cluster into it, still the children cluster
					for(XORCluster<T> sCluster: sourceXORCluster.getChildrenCluster()) {
						// we need to get the xor cluster directly from each branch
						sclusterList.addAll(sCluster.getEndXORList());
					}
					
				}

			}

			List<XORCluster<T>> tclusterList = new ArrayList<XORCluster<T>>();
			if (targetXORCluster.isPureBranchCluster()) {
				tclusterList.add(targetXORCluster);
			} else if (targetXORCluster.isXORCluster()) {
				tclusterList.addAll(targetXORCluster.getChildrenCluster());
			} else {
				// we can't use the end xor list and another list, only this one is fine
				// seq and with xor
				if (targetXORCluster.isSeqCluster()) {
					tclusterList.add(targetXORCluster.getChildrenCluster().get(0));

				} else if (targetXORCluster.isParallelCluster()) {
					// add all the children cluster into it, still the children cluster
					for(XORCluster<T> tCluster: targetXORCluster.getChildrenCluster()) {
						tclusterList.addAll(tCluster.getBeginXORList());
					}
				}
			}

			// here to use the sclusterList and tClusterList to create new branch pair
			branchPairList = new ArrayList<XORClusterPair<T>>();
			for (XORCluster<T> scluster : sclusterList)
				for (XORCluster<T> tcluster : tclusterList) {
					branchPairList.add(new XORClusterPair(scluster, tcluster, true));
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
		branchPairList.add(clusterPair);
	}

	public void addAllBranchClusterPair(List<XORClusterPair<T>> clusterPairList) {
		branchPairList.addAll(clusterPairList);
	}

	public List<XORClusterPair<T>> getBranchClusterPair() {
		return branchPairList;
	}

	public List<XORClusterPair<T>> getLtBranchClusterPair() {
		return ltBranchPairList;
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
		if (available) {
			// only pure branch to pure branch, we can say it, now check their connection
			if (!connections.isEmpty()) {

				ltConnections = new ArrayList<LTRule<XORCluster<T>>>();
				for (LTRule<XORCluster<T>> conn : connections) {
					conn.testSupportConnection();
					if (conn.isSupportConnection()) {
						// if it support the connection, then we return the true
						ltConnections.add(conn);
					}
				}

				if (ltConnections.size() < connections.size())
					complete = false;
				if (!ltConnections.isEmpty())
					connected = true;
			}
			// suddenly, I realize defining the target, it is already fixing the source, and we also choose the situation 
			// when they can happen together, so no worry.
		} else {
			// we need to test the children cluster and find them out, but we need to record the complete branch
			ltBranchPairList = new ArrayList<XORClusterPair<T>>();
			for (XORClusterPair<T> cPair : branchPairList) {
				cPair.testConnected();
				if (cPair.isConnected()) {
					complete &= cPair.isComplete();
					ltBranchPairList.add(cPair);
				} else {
					// not connected,so what to do then??
					complete &= false;
				}
			}

			// check the lt-connection in pair, check if S = LT_S, T=LT_T; 
			if (ltBranchPairList.size() > 0) {
				// 1. for xor block S and xor block T; get the xor branches from it..
				//  tBranchCluster 
				connected = true;
			}
		}
		return connected;
	}
	
	public boolean isSoundConnection() {
		
		Set<XORCluster<T>> ltSources = new HashSet<>();
		Set<XORCluster<T>> ltTargets = new HashSet<>();
		if(ltConnections == null) {
			// get the ltConnection values
			ltConnections = new ArrayList<LTRule<XORCluster<T>>>();
			for(LTRule<XORCluster<T>> conn: connections) {
				if(conn.isSupportConnection())
					ltConnections.add(conn);
				
			}
		}
		for(LTRule<XORCluster<T>> ltConn: ltConnections) { // here we check all the xor
			// branch connection, not the children ones
			ltSources.addAll(ltConn.getSources());
			ltTargets.addAll(ltConn.getTargets());
		}
		Set<XORCluster<T>> xorSources = new HashSet<>();
		Set<XORCluster<T>> xorTargets = new HashSet<>();
		// here we need to check all the xor branches, not the high level structure
		for(LTRule<XORCluster<T>> conn: connections) {
			xorSources.addAll(conn.getSources());
			xorTargets.addAll(conn.getTargets());
		}
			
		if(xorSources.containsAll(ltSources) && ltSources.containsAll(xorSources) &&
				xorTargets.containsAll(ltTargets) && ltTargets.containsAll(xorTargets))
		{ 
			return true;
		}
		
		return false;
	}
	
	public List<LTRule<XORCluster<T>>> getConnection() {
		// TODO return connections of this pair
		// what if we use it later, so we still need to keep reference to it, but anyway by later use
		if (connections != null)
			return connections;
		else { // if it is not pure then we need to get the branchPaiur
			connections = new ArrayList<LTRule<XORCluster<T>>>();

			for (XORClusterPair<T> cPair : branchPairList) {
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

	public void setPNMap(Map<String, PetrinetNode> pnNodeMap) {
		// TODO Auto-generated method stub
		this.pnNodeMap = pnNodeMap;
	}

	public Map<String, PetrinetNode> getPNMap() {
		return pnNodeMap;
	}

}
