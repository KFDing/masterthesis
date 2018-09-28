package org.processmining.plugins.ding.model;

import org.processmining.plugins.ding.train.Configuration.ViewType;

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
	
	public ControlParameters() {
		existWeight =1.0 ;
		posWeight = 1.0;
		negWeight = 1.0;
		type = ViewType.Dfg;
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
		type = ViewType.Dfg;
		existWeight = 1.0;
		posWeight = 1.0;
		negWeight = 1.0;
		
	};
	
}
