package org.processmining.plugins.ding.process.dfg.model;
/**
 * this is used to record the long-term dependency rule in petri net 
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.List;

public class LTRule<E>{
	
	//List<E> originalSourceNodes;
	//List<E> newSourceNodes;
	List<E> sourceNodes;
	List<E> targetNodes;
	
	// we have same sourceNode, and we can have different target leading to
	public LTRule() {
		sourceNodes = new ArrayList<E>();
		targetNodes = new ArrayList<E>();
	}
	
	public void addRule(E source, E target) {
		if(!sourceNodes.contains(source)) {
			sourceNodes.add(source);
		}
		if(!targetNodes.contains(source)) {
			targetNodes.add(source);
		}
		
	}
	
	public void addRuleList(List<E> sourceList, List<E> targetList) {
		for(E source: sourceList)
			for(E target: targetList)
				addRule(source, target);
	}

	public void addRuleSource(E source) {
		// TODO Auto-generated method stub
		if(!sourceNodes.contains(source)) {
			sourceNodes.add(source);
		}
	}

	public List<E> getSources() {
		// TODO Auto-generated method stub
		return sourceNodes;
	}
	
	public List<E> getTargets() {
		// TODO Auto-generated method stub
		return targetNodes;
	}
	
}
