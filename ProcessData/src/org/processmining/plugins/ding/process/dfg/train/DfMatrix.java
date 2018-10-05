package org.processmining.plugins.ding.process.dfg.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.ding.process.dfg.model.Configuration;

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

	Map<ArrayList<XEventClass>, ArrayList<Long>>  dfMatrix ;// = new HashMap<ArrayList<XEventClass>, ArrayList<Long>>();
	final int keyColNum;
	final int valueColNum;
	// only initialization of DfMatrix
	public DfMatrix(){
		dfMatrix = new HashMap<ArrayList<XEventClass>, ArrayList<Long>>();
		keyColNum = Configuration.MATRIX_KEY_COL_NUM;
		valueColNum = Configuration.MATRIX_VALUE_COL_NUM * 2;
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
			
			// check if already exist one key like this..but we must have the first one here, to value here.
			// addMatrixItemWithCheck(source, target, cardinality, colIdx);
			addMatrixItem(source, target, cardinality, colIdx);
		}
		
		// add the start and end activities as a direct follow relation there, 
		final int startIdx = dfMatrix.size();
		XEventClass originalPoint = new XEventClass(Configuration.START_LABEL, startIdx);
		for(XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass); // set the start and end activity in existing model
			// addMatrixItemWithCheck( originalPoint, startEventClass, cardinality, colIdx);
			addMatrixItem( originalPoint, startEventClass, cardinality, colIdx);
		}
		final int endIdx = dfMatrix.size(); // actually we don't need to build this every time we go to the dfg
		XEventClass endPoint = new XEventClass(Configuration.END_LABEL, endIdx); 
		for(XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			// addMatrixItemWithCheck( endEventClass, endPoint, cardinality, colIdx);
			addMatrixItem( endEventClass, endPoint, cardinality, colIdx);
		}
	}
	
	private void addMatrixItemWithCheck( XEventClass source, XEventClass target, long cardinality, int colIdx) {
		if(dfMatrix.size()<1) {
			addMatrixItem(source, target, cardinality, colIdx);
			
		}else {
			// how to avoid the concurrency modification exception
			// I say that, I keep new added into one list, and putAll, like this?
			Iterator itKey = (Iterator) dfMatrix.keySet().iterator();
			while(itKey.hasNext()) {
				ArrayList<XEventClass> key = (ArrayList<XEventClass>) itKey.next();
				XEventClass keySource = key.get(0);
				XEventClass keyTarget = key.get(1);
				
				if(keySource.getId().equals(source.getId()) && keyTarget.getId().equals(target.getId())) {
					// we get the item of it, put it back after assigning value
					ArrayList<Long> dfValue = dfMatrix.get(key);
					dfValue.set(colIdx,  cardinality);
					
					dfMatrix.put(key, dfValue);
				}else {
					// except its own add and remove method, it is not safe
					
					addMatrixItem(source, target, cardinality, colIdx);
				}
			}
		}
	}
	private void addMatrixItem( XEventClass source, XEventClass target, long cardinality, int colIdx) {
		ArrayList<XEventClass> dfKey ;
		ArrayList<Long> dfValue ;
		dfKey = new ArrayList<XEventClass>(keyColNum);
		dfKey.add(source);
		dfKey.add(target);
		
		dfValue = new ArrayList<Long>(valueColNum);
		// to make sure not overflow
		for(int i=0; i< valueColNum; i++)
			dfValue.add(0l);
		// we assign the 
		dfValue.set(colIdx, cardinality );
		// for update use
		dfValue.set(colIdx + Configuration.MATRIX_VALUE_COL_NUM, cardinality );
		
		if(isContainKey(dfKey)) {
			// still this key is somehow different from others
			// dfMatrix.get(dfKey).set(colIdx, cardinality);
			// dfMatrix.get(dfKey).set(colIdx + Configuration.MATRIX_VALUE_COL_NUM, cardinality );
			setValue(dfKey, dfValue);
		}else {
			dfMatrix.put(dfKey, dfValue);
		}
		
	}
	

	private boolean isContainKey(ArrayList<XEventClass> dfKey) {
		for(ArrayList<XEventClass> key : dfMatrix.keySet()) {
			if(dfKey.get(0).getId().equals(key.get(0).getId()) && dfKey.get(1).getId().equals(key.get(1).getId())) {
				return true;
			}
		}
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
	
	public void setValue(ArrayList<XEventClass> dfKey, ArrayList<Long> carList) {
		@SuppressWarnings("unchecked")
		ArrayList<Long> dfValue = (ArrayList<Long>) getValue(dfKey);
		// we can only change on the dfValue
		for(int i=0; i< valueColNum; i++) {
			dfValue.set(i, carList.get(i) + dfValue.get(i));
		}
	}
	
	// to update the value of dfMatrix according to weight and colIdx
	public void updateCardinality(int colIdx, double weight) {
		for(ArrayList<Long> dfValue: dfMatrix.values()) {
			dfValue.set(colIdx + Configuration.MATRIX_VALUE_COL_NUM, (long) (dfValue.get(colIdx)*weight));
		}
	}
	/**
	 * some bugs here about the start and end activities. Whic I can't really say it.. 
	 * Just debug it and see how it works
	 * @param dfg
	 */
	public Dfg buildDfs() {
		Dfg dfg = new DfgImpl();
		// read of each item in dfMatrix 
		long cardinality;
		for(Map.Entry<ArrayList<XEventClass>, ArrayList<Long>> entry: dfMatrix.entrySet()) {
			ArrayList<XEventClass> dfKey =  entry.getKey();
			ArrayList<Long> dfValue = entry.getValue();
			// 
			// different situation here!! // we can merge those situation and take same action
			// 1. only happen in neg 0 0 1 or 0 0 0==> delete it, no such relation !!
			if(dfValue.get(Configuration.MATRIX_EXISTING_IDX)<1 && dfValue.get(Configuration.MATRIX_POS_IDX)<1) {
				continue; // or should we set it 0??
			}else if(dfValue.get(Configuration.MATRIX_NEG_IDX) <1) {
				// 2. only in positive 0 1 0,  ==> add it into dfg
				// 4. only in the existing model, 1 0 0, ==>  add it into dfg
				// 6. in existing and pos, 1 1 0 ==> no change but add cardinality on it
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				// here we need to check what situation theny are and then deal with it
				addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), cardinality );
			}else {
				// with neg > 0, other combinations can be: 
				// 3. in pos and neg, 0 1 1 ==> neg is only part of pos, then keep, 
				// 7 in all, existing, pos and neg, 1 1 1==> same situation in 3
				// 5. in existing and neg, 1 0 1 ==> check the distribution of it 
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				long neg = dfValue.get(Configuration.MATRIX_NEG_IDX);
				if(cardinality > neg) {
					addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), cardinality - neg );
				}else {
					continue; //  do nothing, or something else..
				}
			}
			
				/*
			 if( dfValue.get(Configuration.MATRIX_POS_IDX)>0 && dfValue.get(Configuration.MATRIX_NEG_IDX) >0) {
				// 3. in pos and neg, 0 1 1 ==> neg is only part of pos, then keep, 
				// 7 in all, existing, pos and neg, 1 1 1==> same situation in 4 
				// but if neg and pos has the both, check the distribution, but should we consider the existing model effect?
				// the sum = pos + existing, sum > neg, then we keep it, sum < neg, delete it , should we give some weight on neg?? 
				// we can assign it before this.. 
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				long neg = dfValue.get(Configuration.MATRIX_NEG_IDX);
				if(cardinality > neg) {
					addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), cardinality - neg );
				}else {
					continue; //  do nothing, or something else..
				}
			}else if(dfValue.get(Configuration.MATRIX_EXISTING_IDX)>0 && dfValue.get(Configuration.MATRIX_POS_IDX)<1 &&
					dfValue.get(Configuration.MATRIX_NEG_IDX) >0) {
				// 5. in existing and neg, 1 0 1 ==> delete it or we check the distribution of existing and neg?? 
				// if the distribution of existing is bigger than the neg, then keep it, else delete it??
				// continue;
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				long neg = dfValue.get(Configuration.MATRIX_NEG_IDX);
				if(cardinality > neg) {
					addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), cardinality - neg );
				}else {
					continue; //  do nothing, or something else..
				}
			}
			*/
		}
		return dfg;
	}
	
	private void addDfgDirectFollow(Dfg dfg, XEventClass source, XEventClass target , long cardinality) {
		
		// if  source is start, then we add to dfg start activity, but we need to make sure there is only one index for it
		if(source.getId().equals(Configuration.START_LABEL)) {
			dfg.addStartActivity(target, cardinality);
		}
		// if target is end, we add it to the dfg end activity
		if(target.getId().equals(Configuration.END_LABEL)) {
			dfg.addEndActivity(source, cardinality);
		}
		// else we need just add it simply to dfg
		if(!source.getId().equals(Configuration.START_LABEL) && !target.getId().equals(Configuration.END_LABEL))
			dfg.addDirectlyFollowsEdge(source, target, cardinality);
	}

	
}
