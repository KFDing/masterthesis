package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to store the same level xor and then generate the global dependency
 * pair includes 2 xor structure in the same level and we only need to assign the pointer to it
 * 
 * Methods: 
 *  -- to assign the data into the XORPair
 *  -- check if they have global dependency
 *  -- which branch to which branch they have global dependency
 *     ++ here we have different branches from it but at first we need to create them all
 *     ++ then we can check if they have LTDependencyConnection.. Maybe we need 
 *     ++ to change the names into anther names, because they are just potential 
 * @author dkf
 *
 * @param <T>
 */
public class XORPair<T>{
	// by using first and second, we already define some order, I guess
	// but we can easily ignore them and by using numbers to explain it 
	XORStructure<T> sourceXOR;
	XORStructure<T> targetXOR;
	
	List<LTConnection<T>> connections;
	boolean completeConnection = true;
	List<LTConnection<T>> ltDep ;
	
	public XORStructure<T> getSourceXOR() {
		return sourceXOR;
	}

	public void setSourceXOR(XORStructure<T> sourceXOR) {
		this.sourceXOR = sourceXOR;
	}

	public XORStructure<T> getTargetXOR() {
		return targetXOR;
	}

	public void setTargetXOR(XORStructure<T> tagetXOR) {
		this.targetXOR = tagetXOR;
	}
	// one attributes to store the branch to branch global dependency
	// how could we define them?? the branch from firstXOR, it have connection with
	// branches from the secondXOR structure
	// we define the connection of such thing, like we did before 
	// ( branch, branches from secondXOR)  then <one branch, second branch > :: [existing, pos, neg]
	
	// we create a list for each branch in firstXOR to get the relation of secondXOR 
	// it depends one the number of branches, so we also need to consider it, 
	// also it is the branch relation of it, then how to combine them together??
	// we could create a list to store them all
	//  -- branch 0.0 from first branch to create LTConnection with all branches from second one 
	//  -- branch 0.1 to create the second structure of it 
	// not. let us to have the structure at first and then organize them maybe later
	public void createLTConnection() {
		connections =  new ArrayList<LTConnection<T>>();
		for(int i=0; i< sourceXOR.getNumOfBranches();i++) {
			for(int j=0;j<targetXOR.getNumOfBranches();j++) {
				LTConnection<T> conn = new LTConnection<T>(sourceXOR.getBranches().get(i), targetXOR.getBranches().get(j));
				connections.add(conn);
			}
		}
		
	}
	
	public List<LTConnection<T>> getLTConnections(){
		if(connections == null){
			createLTConnection();
		}
		return connections;
	}
	
	// if we generate places, we need to get the branch at first and at last element but it is later
	// a global dependency we need to judge for all the data if they complete, it means that all the branches has
	// connection then we can do it 
	public List<LTConnection<T>> checkBranchConnection(List<LTConnection<T>> sourceConns) {
		
		for(int i=0; i< sourceConns.size(); i++) {
			LTConnection<T> conn = sourceConns.get(i);
			// if it doesn't support the connection, so we don't use it 
			if(!conn.testSupportConnection()) 
				sourceConns.remove(i);
				
		}
		return sourceConns;
	}
	
	private List<LTConnection<T>> getBranchConnection(XORBranch<T> branch) {
		// to decide if completeConnection for one branch in source XOR 
		List<LTConnection<T>> sourceConns = new ArrayList<LTConnection<T>>();
		for(LTConnection<T> conn : connections) {
			// if it is complete, or not!!! // so we need to create the list of each source and target
			if(conn.getFirstBranch().equals(branch))
				sourceConns.add(conn);
		}
		return sourceConns;
	}
	
	
	// if not complete connection we need to generate the places for Petri net
	// at first we can put them into some rules, I guess
	public List<LTConnection<T>> generateLTDependency() {
		ltDep =  new ArrayList<LTConnection<T>>();
		// because they relative the whole xor structure, so we generate it here 
		for(XORBranch<T> branch : sourceXOR.getBranches()) {
			List<LTConnection<T>> branchConns = getBranchConnection(branch);
			int num = branchConns.size();
			
			if(checkBranchConnection(branchConns).size() < num) {
				completeConnection = false;
				ltDep.addAll(branchConns);
			}
		}
		// there is no lt dependency
		if(completeConnection) {
			return null;
		}else {
			return ltDep;
		}
	}
	
	public boolean hasCompleteConnection() {
		int num = connections.size();
		ltDep =  new ArrayList<LTConnection<T>>();
		
		for(int i=0; i< connections.size(); i++) {
			LTConnection<T> conn = connections.get(i);
			// if it doesn't support the connection, so we don't use it 
			if(conn.testSupportConnection()) 
				ltDep.add(conn);
		}
		
		if(num > ltDep.size() && ltDep.size() > 0) {
			completeConnection = false;
		}
		return completeConnection;
	}
	
	public List<LTConnection<T>> getLTDependency() {
		return ltDep;
	}

	public void adaptLTConnection(int colIdx, double weight) {
		// TODO adapt connection values according to the weight and column
		for(int i=0; i< connections.size(); i++) {
			LTConnection<T> conn = connections.get(i);
			conn.adaptValue(colIdx, weight);
		}
		
	}
	
	
}