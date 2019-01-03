package org.processmining.plugins.ding.process.dfg.model;
/**
 * this is used to record the long-term dependency rule in petri net 
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.List;

public class LTRule<E>{
	
	
	List<E> sourceNodes;
	List<E> targetNodes;
	
	// we should also have a set to mark the original and newAddded nodes
	// specially for source
	List<E> newSourceNodes;
	
	int posIdx = ProcessConfiguration.LT_POS_IDX;
	int negIdx = ProcessConfiguration.LT_NEG_IDX;
	int num = ProcessConfiguration.LT_IDX_NUM * 2;
	List<Double> connectionValues;
	boolean supportConnection = false;
	// mark if this rule is visited, if visited, then we don't need to consider it anymore
	boolean ltVisited = false;

	// we have same sourceNode, and we can have different target leading to
	public LTRule() {
		sourceNodes = new ArrayList<E>();
		targetNodes = new ArrayList<E>();
		newSourceNodes = new ArrayList<E>();
		
		connectionValues = new ArrayList<Double>();
		for(int i=0; i< num;i++)
			connectionValues.add(0.0);
	}
	
	public LTRule(E source, E target) {
		// TODO Auto-generated constructor stub
		sourceNodes = new ArrayList<E>();
		targetNodes = new ArrayList<E>();
		sourceNodes.add(source);
		targetNodes.add(target);
		
		connectionValues = new ArrayList<Double>();
		for(int i=0; i< num;i++)
			connectionValues.add(0.0);
	}

	public void addRule(E source, E target) {
		if(!sourceNodes.contains(source)) {
			sourceNodes.add(source);
		}
		if(!targetNodes.contains(target)) {
			targetNodes.add(target);
		}
		
	}
	
	public void addRuleList(List<E> sourceList, List<E> targetList) {
		for(E source: sourceList)
			addRuleSource(source);
		for(E target: targetList)
			addRuleTarget(target);
	}

	public void addRuleSource(E source) {
		// TODO Auto-generated method stub
		if(!sourceNodes.contains(source) && !newSourceNodes.contains(source)) {
			// give them back there, make sure they are not there before.
			sourceNodes.addAll(newSourceNodes);
			newSourceNodes.clear();
			newSourceNodes.add(source);
		}
		
	}
	public void addRuleTarget(E target) {
		// TODO Auto-generated method stub
		if(!targetNodes.contains(target)) {
			targetNodes.add(target);
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

	// if we add test support connection, we should have the connection index
	// the cardinality of this connection, which is what we can get from the LTConnection
	// also, if we change the supported value, what to do then?? 
	// basically it is just the cardinality, we need to adapt it!! 
	public void testSupportConnection() {
		if(connectionValues.get(posIdx)> connectionValues.get(negIdx)) 
			supportConnection = true;
		else
			supportConnection = false;
		
	}
	
	
	public boolean isSupportConnection() {
		return supportConnection;
	}
	
	public void addConnectionValues(List<Double> values) {
		// add values fro each variant and get the total values
		for(int i=0; i< num;i++) {
			double tmp =  connectionValues.get(i) + values.get(i);
			connectionValues.set(i, tmp);
		}
	}

	public void adaptValue(int colIdx, double weight) {
		// TODO adpat value according to colIdx and weight, but we have it only according 
		connectionValues.set(colIdx, weight * connectionValues.get(colIdx - ProcessConfiguration.LT_IDX_NUM));
	}
	
	public boolean isLtVisited() {
		return ltVisited;
	}

	public void setLtVisited(boolean ltVisited) {
		this.ltVisited = ltVisited;
	}
}
