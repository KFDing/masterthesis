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
	boolean completeConnection = false;
	
	
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
	
	// if we generate places, we need to get the branch at first and at last element but it is later
	// a global dependency we need to judge for all the data if they complete, it means that all the branches has
	// connection then we can do it 
	public void checkConnection() {
		// to decide if completeConnection 
	}
	
	// if not complete connection we need to generate the places for Petri net
	// at first we can put them into some rules, I guess
	public void generateLTDependency() {
		
	}
	
}