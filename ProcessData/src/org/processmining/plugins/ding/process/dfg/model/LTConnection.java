package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;

public class LTConnection<T>{
	
	XORBranch<T> firstBranch ;
	XORBranch<T> secondBranch;
	// here need to fix the Value using double problems.. But we can get it later, I think..
	int posIdx = ProcessConfiguration.RULESET_POS_IDX;
	int negIdx = ProcessConfiguration.RULESET_NEG_IDX;
	List<Double> connectionValues;
	
	boolean supportConnection = false;
	
	public boolean isSupportConnection() {
		return supportConnection;
	}

	public void testSupportConnection() {
		// this method test if supportConnection of such branches, we only need to compare the 
		// values of connectionValues, and give the value to it
		if(connectionValues.get(posIdx)> connectionValues.get(negIdx)) 
			supportConnection = true;
		else
			supportConnection = false;
	}

	public LTConnection(XORBranch<T> first, XORBranch<T> second) {
		firstBranch = first;
		secondBranch = second;
		connectionValues = new ArrayList<Double>();
	}
	
	public LTConnection(XORBranch<T> first, XORBranch<T> second, List<Double> values) {
		firstBranch = first;
		secondBranch = second;
		connectionValues = values;
	}
	
	public XORBranch<T> getFirstBranch() {
		return firstBranch;
	}

	public void setFirstBranch(XORBranch<T> firstBranch) {
		this.firstBranch = firstBranch;
	}

	public XORBranch<T> getSecondBranch() {
		return secondBranch;
	}

	public void setSecondBranch(XORBranch<T> secondBranch) {
		this.secondBranch = secondBranch;
	}

	
	public List<Double> getConnectionValues() {
		return connectionValues;
	}

	public void setConnectionValues(List<Double> connectionValues) {
		this.connectionValues = connectionValues;
	}
	
	public void addConnectionValues(List<Double> values) {
		for(int i=0; i< values.size();i++) {
			double tmp =  connectionValues.get(i) + values.get(i);
			connectionValues.set(i, tmp);
		}
	}

}
