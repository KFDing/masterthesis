package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.processmining.plugins.ding.process.dfg.model.XORPair;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

/**
 * this class is used to generate the xor structure of process tree, or to extract xor structure of process tree
 * and only use them as the main bones for the next structure building... 
 * If they are in sequence, but during the generating, we need to create the pair too!!
 * so it's still an skizze for the programs
 * @author dkf
 *
 */
public class XORStructureGenerator {
	List<XORPair<ProcessTreeElement>> pairList;
	Stack<String> stateStack;
	
	public List<XORPair<ProcessTreeElement>> generateXORPairs(ProcessTree tree) {
		pairList = new ArrayList<XORPair<ProcessTreeElement>>();
	
		stateStack = new Stack<String>();
		
		// do DFS to add pair into the pairs, we also need one stack to record its current state
		if(tree.size() < 1) {
			System.out.println("The tree is empty");
			return null; 
		}
		// addXORPairByDFS(tree.getRoot());

		return pairList;
	}

}
