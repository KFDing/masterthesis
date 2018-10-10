package org.processmining.plugins.ding.process.dfg.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;

/**
 * this class is used to store the direct follow relation from existing, pos and neg dfg.
 * It includes cardinality for each element, one original value, one updated value.
 * Methods:
 *  -- one to add elements into it
 *  -- one to update cardinality w.r.t. weight and colIdx
 *  -- one to generate the new dfg
 * @author dkf
 *
 */
public class DfMatrix {

	Map<ArrayList<XEventClass>, ArrayList<Double>>  dfMatrix ;// = new HashMap<ArrayList<XEventClass>, ArrayList<Long>>();
	final int keyColNum;
	final int valueColNum;
	long standardCardinality; 
	// only initialization of DfMatrix
	public DfMatrix(){
		dfMatrix = new HashMap<ArrayList<XEventClass>, ArrayList<Double>>();
		keyColNum = ProcessConfiguration.MATRIX_KEY_COL_NUM;
		valueColNum = ProcessConfiguration.MATRIX_VALUE_COL_NUM * 2;
	}
	
	public void setStandardCardinality(long cardiality) {
		standardCardinality = cardiality;
	}
	
	public long getStandardCardinality(Dfg dfg) {
		return standardCardinality;
	}
	
	public void addDirectFollowMatrix(Dfg dfg, int colIdx) {
		/*
		 * read from each dfg, put edges into the new map, 
		 * ======if the map has the edge, then mark which exists, 
		 * ======if not then create new edge into the new, but also mark it 
		 */
		for(long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			XEventClass target = dfg.getDirectlyFollowsEdgeTarget(idx);
			long cardinality = dfg.getDirectlyFollowsEdgeCardinality(idx);
			
			// we need to get the percent of source->target in all source->* 
			// so firstly we need to get the all source cardinality?? 
			double percent = 1.0*cardinality / getOutEdgesCardinality(dfg, source);
					
			addMatrixItem(source, target, percent, colIdx);
		}
		
		// add the start and end activities as a direct follow relation there, 
		final int startIdx = dfMatrix.size();
		XEventClass originalPoint = new XEventClass(ProcessConfiguration.START_LABEL, startIdx);
		for(XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass); // set the start and end activity in existing model
			// addMatrixItemWithCheck( originalPoint, startEventClass, cardinality, colIdx);
			// for the startEvent, we can't count the use the directfollow edge, because it begins from the first position
			// then we need to count all the start activity and then get the percent to it
			double percent = 1.0 * cardinality / getStartEventCardinality(dfg);
			
			addMatrixItem( originalPoint, startEventClass, percent, colIdx);
		}
		final int endIdx = dfMatrix.size(); // actually we don't need to build this every time we go to the dfg
		XEventClass endPoint = new XEventClass(ProcessConfiguration.END_LABEL, endIdx); 
		for(XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			// addMatrixItemWithCheck( endEventClass, endPoint, cardinality, colIdx);
			double percent = 1.0 * cardinality / getEndEventCardinality(dfg);
			addMatrixItem( endEventClass, endPoint, percent, colIdx);
		}
	}
	
	private long getStartEventCardinality(Dfg dfg) {
		long sum = 0;
		for(XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass);
			sum +=  cardinality;
		}
		return sum;
	}

	private long getEndEventCardinality(Dfg dfg) {
		long sum = 0;
		for(XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			sum +=  cardinality;
		}
		return sum;
	}

	private void addMatrixItem( XEventClass source, XEventClass target, double percent, int colIdx) {
		ArrayList<XEventClass> dfKey ;
		dfKey = new ArrayList<XEventClass>(keyColNum);
		dfKey.add(source);
		dfKey.add(target);
		
		ArrayList<Double> dfValue = new ArrayList<Double>(valueColNum);
		// to make sure not overflow
		for(int i=0; i< valueColNum; i++)
			dfValue.add(0.0);
		// we assign the 
		dfValue.set(colIdx, percent);
		// for update use
		dfValue.set(colIdx + ProcessConfiguration.MATRIX_VALUE_COL_NUM, percent );
		
		if(isContainKey(dfKey)) {
			// still this key is somehow different from others
			// dfMatrix.get(dfKey).set(colIdx, cardinality);
			// dfMatrix.get(dfKey).set(colIdx + Configuration.MATRIX_VALUE_COL_NUM, cardinality );
			// System.out.println(dfKey.get(0));
			setValue(dfKey, dfValue);
		}else {
			dfMatrix.put(dfKey, dfValue);
		}
		
	}
	
	private List<XEventClass> getOutEdges(Dfg dfg, XEventClass sourceNode) {
		// List<XEventClass> edges = new ArrayList<XEventClass>();
		List<XEventClass>  edges =  new ArrayList<XEventClass>();
		// check for all the edges if they have the same label as source, then we keep the edges into it
		// should with carnality of only target?? If it is with cardinality, it save us time to search 
		// it again, but the structure should be changed. 
		for(long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			if(isEventClassSame(sourceNode, source)) {
				XEventClass target = dfg.getDirectlyFollowsEdgeTarget(idx);
				edges.add(target);
			}	
		}
		return edges;
	}

	private long getOutEdgesCardinality(Dfg dfg, XEventClass sourceNode) {
		long sum = 0;
		for(long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			if(isEventClassSame(sourceNode, source)) {
				long cardinality = dfg.getDirectlyFollowsEdgeCardinality(idx);
				sum += cardinality;
			}	
		}
		return sum;
	}

	private boolean isContainKey(ArrayList<XEventClass> dfKey) {
		for(ArrayList<XEventClass> key : dfMatrix.keySet()) {
			if(isEventClassSame(dfKey.get(0), key.get(0)) && isEventClassSame(dfKey.get(1), key.get(1))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isEventClassSame(XEventClass event1, XEventClass event2) {
		if(event1.getId().equals(event2.getId()))
			return true;
		return false;
	}
	
	public Object getValue(ArrayList<XEventClass> dfKey) {
		for(ArrayList<XEventClass> key : dfMatrix.keySet()) {
			if(dfKey.get(0).getId().equals(key.get(0).getId()) && dfKey.get(1).getId().equals(key.get(1).getId())) {
				return dfMatrix.get(key);
			}
		}
		System.out.println("there is no such key and value in dfMatrix");
		return null;
	}
	
	public void setValue(ArrayList<XEventClass> dfKey, ArrayList<Double> dfValue2) {
		@SuppressWarnings("unchecked")
		ArrayList<Double> dfValue = (ArrayList<Double>) getValue(dfKey);
		System.out.println(dfValue.get(3));
		// we can only change on the dfValue
		for(int i=0; i< valueColNum; i++) {
			// System.out.println("id " + i+ dfValue2.get(3));
			dfValue.set(i, dfValue2.get(i) + dfValue.get(i));
		}
	}
	
	// to update the value of dfMatrix according to weight and colIdx
	public void updateCardinality(int colIdx, double weight) {
		for(ArrayList<Double> dfValue: dfMatrix.values()) {
			dfValue.set(colIdx + ProcessConfiguration.MATRIX_VALUE_COL_NUM,  dfValue.get(colIdx)*weight);
		}
	}
	/**
	 * some bugs here about the start and end activities. Whic I can't really say it.. 
	 * if we change the DfMatrix into percent form, then how to transform them back itno cardinality
	 * Just debug it and see how it works
	 * @param dfg
	 */
	public Dfg buildDfs() {
		Dfg dfg = new DfgImpl();
		// read of each item in dfMatrix 
		double percent;
		for(Map.Entry<ArrayList<XEventClass>, ArrayList<Double>> entry: dfMatrix.entrySet()) {
			ArrayList<XEventClass> dfKey =  entry.getKey();
			ArrayList<Double> dfValue = entry.getValue();
			// 
			// different situation here!! // we can merge those situation and take same action
			// 1. only happen in neg 0 0 1 or 0 0 0==> delete it, no such relation !!
			if(dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX)<1 && dfValue.get(ProcessConfiguration.MATRIX_POS_IDX)<1) {
				continue; // or should we set it 0??
			}else if(dfValue.get(ProcessConfiguration.MATRIX_NEG_IDX) <1) {
				// 2. only in positive 0 1 0,  ==> add it into dfg
				// 4. only in the existing model, 1 0 0, ==>  add it into dfg
				// 6. in existing and pos, 1 1 0 ==> no change but add cardinality on it
				// one thing is here that, if we only want the existing ones, then we have it, so no problem
				percent = dfValue.get(ProcessConfiguration.MATRIX_POS_IDX) + dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX);
				// here we need to check what situation theny are and then deal with it
				addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), transform2Cardinality(percent) );
			}else {
				// with neg > 0, other combinations can be: 
				// 3. in pos and neg, 0 1 1 ==> neg is only part of pos, then keep, 
				// 7 in all, existing, pos and neg, 1 1 1==> same situation in 3
				// 5. in existing and neg, 1 0 1 ==> check the distribution of it 
				percent = dfValue.get(ProcessConfiguration.MATRIX_POS_IDX) + dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX);
				double neg = dfValue.get(ProcessConfiguration.MATRIX_NEG_IDX);
				if(percent > neg) {
					// how to transform percent into cardinality is another question.. 
					// we could use the 
					addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), transform2Cardinality(percent - neg));
				}else {
					continue; //  do nothing, or something else..
				}
			}
			
		}
		return dfg;
	}
	
	private long transform2Cardinality(double percent) {
		// here is back from the percent into the cardinality, but still some magical number?? 
		// or we use it in corresponding with the log information?? 
		// or?? we consider the positive dfg, to get teh total number of it and then use them?? 
		// now we are int DfMatrix and we want to generate new stuffs
		// startEvent and EndEvent also needs attention.
		// if we assign the same values, it can't reflect the noisy ones[with less cardinality] and ones with more support
		// or we just create the dfg, maybe according to its 
		// we see the negative ones have less choices and it could mean that it have more weights on it.
		// <1> threshold to filter it 
		// <2> according to the source out edges cardinality in pos 
		// <3> according to the whole log or pos log and assign to it.. 
		// === I prefer <2>, then make it possible now!!!!
		
		// using second one, get the source of it , and pos cardinality in the beginning  parts!! 
		// we can have the pos dfg as its attribute, and check it here
		// what if here, we can't see in pos but in existing ones, then we don't have such relation.. 
		// nana, not good about it.. it's another stratege.. 
		// ?? should we record some parts of it?? and they should have the same number like in the pos log..
		// then I would like to say, we choose methods <3>
		long cardinality = (long) (percent * standardCardinality);
		return cardinality;
	}

	private void addDfgDirectFollow(Dfg dfg, XEventClass source, XEventClass target , long cardinality) {
		
		// if  source is start, then we add to dfg start activity, but we need to make sure there is only one index for it
		if(source.getId().equals(ProcessConfiguration.START_LABEL)) {
			dfg.addStartActivity(target, cardinality);
		}
		// if target is end, we add it to the dfg end activity
		if(target.getId().equals(ProcessConfiguration.END_LABEL)) {
			dfg.addEndActivity(source, cardinality);
		}
		// else we need just add it simply to dfg
		if(!source.getId().equals(ProcessConfiguration.START_LABEL) && !target.getId().equals(ProcessConfiguration.END_LABEL))
			dfg.addDirectlyFollowsEdge(source, target, cardinality);
	}

	
}
