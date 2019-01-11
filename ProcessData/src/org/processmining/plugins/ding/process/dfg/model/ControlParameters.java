package org.processmining.plugins.ding.process.dfg.model;

import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration.ActionType;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration.ViewType;
import org.processmining.processtree.ProcessTreeElement;

/**
 * this class is used to create the control parameters, it includes:
 *   -- existing model weight;
 *   -- pos event log weight;
 *   -- neg event log weight;
 *   
 *   -- view Type :
 *     ++ dfg
 *     ++ Process Tree
 *     ++ Petri net
 *     
 *     ** somehow we can show the existing and modified model together in one view.
 *     ** or create the floating window
 * @author dkf
 *
 */
public class ControlParameters {

	double existWeight;
	double posWeight;
	double negWeight ;
	ViewType type ;
	ActionType action;
	// choose xor pair by manually or add them all
	boolean addAllPair; // default value is true, but can be decided by the addPairPanel
	// do we need to know the pair information?? 
	// we could have the pair and then check the information
	XORCluster<ProcessTreeElement> source;
	XORCluster<ProcessTreeElement> target;

	// but to show the choice what to do then?? we need to pass by using the generate.
	
	public ControlParameters() {
		existWeight =1.0 ;
		posWeight = 1.0;
		negWeight = 1.0;
		type = ViewType.ProcessTree;
		
		addAllPair = true;
	}
	
	public void cloneValues(ControlParameters paras) {
		existWeight =paras.getExistWeight();
		posWeight = paras.getPosWeight();
		negWeight = paras.getNegWeight();
		type = paras.getType();
		
		action = paras.getAction();
		addAllPair = paras.isAddAllPair();
	}
	
	public boolean isAddAllPair() {
		return addAllPair;
	}

	public void setAddAllPair(boolean addAllPair) {
		this.addAllPair = addAllPair;
	}
	
	public void setAction(ActionType action) {
		this.action = action;
	}
	
	public ActionType getAction() {
		return action;
	}

	public ViewType getType() {
		return type;
	}

	public void setType(ViewType type) {
		this.type = type;
	}
	
	public double getExistWeight() {
		return existWeight;
	}

	public void setExistWeight(double existWeight) {
		this.existWeight = existWeight;
	}

	public double getPosWeight() {
		return posWeight;
	}

	public void setPosWeight(double posWeight) {
		this.posWeight = posWeight;
	}

	public double getNegWeight() {
		return negWeight;
	}

	public void setNegWeight(double negWeight) {
		this.negWeight = negWeight;
	}

	public void resetValue() {
		type = ViewType.ProcessTree;
		existWeight = 1.0;
		posWeight = 1.0;
		negWeight = 1.0;
		
		addAllPair = true;
	}
	
	public XORCluster<ProcessTreeElement> getSource() {
		return source;
	}

	public void setSource(XORCluster<ProcessTreeElement> source) {
		this.source = source;
	}

	public XORCluster<ProcessTreeElement> getTarget() {
		return target;
	}

	public void setTarget(XORCluster<ProcessTreeElement> target) {
		this.target = target;
	}
	
}
