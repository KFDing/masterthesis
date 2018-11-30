package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.List;
/**
 * this is for the real nodes in process tree connection. 
 * 
 * @author dkf
 *
 * @param <T>
 */
public class LTConnection<T>{
	
	XORBranch<T> sourceBranch ;
	XORBranch<T> targetBranch;
	// here need to fix the Value using double problems.. But we can get it later, I think..
	int posIdx = ProcessConfiguration.LT_POS_IDX;
	int negIdx = ProcessConfiguration.LT_NEG_IDX;
	int num = ProcessConfiguration.LT_IDX_NUM * 2;
	List<Double> connectionValues;
	
	boolean supportConnection = false;
	
	public boolean isSupportConnection() {
		return supportConnection;
	}

	public boolean testSupportConnection() {
		// this method test if supportConnection of such branches, we only need to compare the 
		// values of connectionValues, and give the value to it
		if(connectionValues.get(posIdx)> connectionValues.get(negIdx)) 
			supportConnection = true;
		else
			supportConnection = false;
		return supportConnection;
	}

	public LTConnection(XORBranch<T> first, XORBranch<T> second) {
		sourceBranch = first;
		targetBranch = second;
		connectionValues = new ArrayList<Double>();
		for(int i=0; i< num;i++)
			connectionValues.add(0.0);
	}
	
	
	public LTConnection(XORBranch<T> first, XORBranch<T> second, List<Double> values) {
		sourceBranch = first;
		targetBranch = second;
		connectionValues = values;
	}
	
	public XORBranch<T> getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(XORBranch<T> firstBranch) {
		this.sourceBranch = firstBranch;
	}

	public XORBranch<T> getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(XORBranch<T> secondBranch) {
		this.targetBranch = secondBranch;
	}

	
	public List<Double> getConnectionValues() {
		return connectionValues;
	}

	public void setConnectionValues(List<Double> connectionValues) {
		this.connectionValues = connectionValues;
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

}

