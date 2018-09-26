package org.processmining.plugins.ding.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.XLog2Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;


/**
 * this class is used to incorporate the negative information with dfg graph
 *  -- create the 3 matrix of directly follow relation, one from existing model, one from pos log, one from neg log
 *  -- compare the directly follow category, and decide if we keep it or not
 *  -- get a new dfg and pass it further as input to generate Petri net
 * @author dkf
 *
 */
public class IncorporateNeg2Dfg {
	@Plugin(name = "Incorporate Log with Labels into Dfg", level = PluginLevel.Regular, returnLabels = {"Nos is Dfg", "Dfg", "Dfg" }, returnTypes = {
			Dfg.class, Dfg.class, Dfg.class}, parameterLabels = { "Log" ,"Dfg"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Incorporate Log with Labels into Dfg",  requiredParameterLabels = { 0, 1})
	public Object[] buildModel(UIPluginContext context, XLog log, Dfg dfg) throws UnknownTreeNodeException {
		// create the 3 matrix of directly follow relation
		Object[] result = splitEventLog(log);
		XLog pos_log = (XLog) result[0];
		XLog neg_log = (XLog) result[1];
		// 
		XLog2Dfg ld = new XLog2Dfg();
		Dfg pos_dfg = ld.log2Dfg( context, pos_log);
		Dfg neg_dfg = ld.log2Dfg( context, neg_log);
		
		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		Dfg new_dfg = mergeDfgs(dfg, pos_dfg, neg_dfg);
		
		return new Object[] {new_dfg, pos_dfg, neg_dfg};
	}
	
	public Object[] splitEventLog(XLog log){
		XLog pos_log = (XLog) log.clone();
		XLog neg_log = (XLog) log.clone();
		pos_log.clear();
		neg_log.clear();
		
		int neg_count = 0, pos_count=0;
		// in this way it doesn't work, but where goes wrong..
		for(int i =0; i< log.size(); i++) {
			XTrace trace = (XTrace) log.get(i);
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				if(!attr.getValue()) {
					neg_log.add(trace);
					neg_count++;
				}else {
					pos_log.add(trace);
					pos_count++;
				}
			}
		}
		assert neg_count == XLogInfoFactory.createLogInfo(neg_log).getNumberOfTraces();
		assert pos_count == XLogInfoFactory.createLogInfo(pos_log).getNumberOfTraces();
		if(neg_count <1) {
			System.out.println("there is no neg examples found in the event log");
			neg_log = null;
		}
		return new Object[] {pos_log, neg_log};
	}
	
	private Dfg mergeDfgs(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		Dfg new_dfg = new DfgImpl();
		
		Map<ArrayList<XEventClass>, ArrayList<Long>>  dfMatrix = new HashMap<ArrayList<XEventClass>, ArrayList<Long>>();
		addDirectFollowMatrix(dfMatrix, dfg, 0);
		// one problem here is about the single direct follow relation, it doesn't show here
		addDirectFollowMatrix(dfMatrix, pos_dfg, 1); 
		addDirectFollowMatrix(dfMatrix, neg_dfg, 2);
		// after we have dfMatrix, we need to assign edges to new dfg w.r.t. different situations
		buildDfs(new_dfg, dfMatrix);
		
		return new_dfg;
	}


	private void buildDfs(Dfg dfg, Map<ArrayList<XEventClass>, ArrayList<Long>> dfMatrix) {
		// read of each item in dfMatrix 
		long cardinality;
		for(Map.Entry<ArrayList<XEventClass>, ArrayList<Long>> entry: dfMatrix.entrySet()) {
			ArrayList<XEventClass> dfKey =  entry.getKey();
			ArrayList<Long> dfValue = entry.getValue();
			
			// different situation here!! // we can merge those situation and take same action
			// 1. only happen in neg 0 0 1 ==> delete it, no such relation !!
			if(dfValue.get(Configuration.MATRIX_EXISTING_IDX)<1 && dfValue.get(Configuration.MATRIX_POS_IDX)<1 &&
					dfValue.get(Configuration.MATRIX_NEG_IDX) >1) {
				continue; // or should we set it 0??
			}else if(dfValue.get(Configuration.MATRIX_NEG_IDX) <1) {
				// 2. only in positive 0 1 0,  ==> add it into dfg
				// 4. only in the existing model, 1 0 0, ==>  add it into dfg
				// 6. in existing and pos, 1 1 0 ==> no change but add cardinality on it
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				// here we need to check what situation theny are and then deal with it
				addDfgDirectFollow(dfg, dfKey,cardinality );
			}else if( dfValue.get(Configuration.MATRIX_POS_IDX)>0 && dfValue.get(Configuration.MATRIX_NEG_IDX) >0) {
				// 3. in pos and neg, 0 1 1 ==> neg is only part of pos, then keep, 
				// 7 in all, existing, pos and neg, 1 1 1==> same situation in 4 
				// but if neg and pos has the both, check the distribution, but should we consider the existing model effect?
				// the sum = pos + existing, sum > neg, then we keep it, sum < neg, delete it , should we give some weight on neg?? 
				// we can assign it before this.. 
				cardinality = dfValue.get(Configuration.MATRIX_POS_IDX) + dfValue.get(Configuration.MATRIX_EXISTING_IDX);
				long neg = dfValue.get(Configuration.MATRIX_NEG_IDX);
				if(cardinality > neg) {
					addDfgDirectFollow(dfg, dfKey, cardinality );
				}else {
					continue; //  do nothing, or something else..
				}
			}else if(dfValue.get(Configuration.MATRIX_EXISTING_IDX)>0 && dfValue.get(Configuration.MATRIX_POS_IDX)<1 &&
					dfValue.get(Configuration.MATRIX_NEG_IDX) >0) {
				// 5. in existing and neg, 1 0 1 ==> delete it or we check the distribution of existing and neg?? 
				// if the distribution of existing is bigger than the neg, then keep it, else delete it??
				continue;
			}
		}	
	}

	/**
	 * this method is kind of twisted but, for computation efficiency,I put them together.
	 * @param dfg
	 * @param dfKey
	 * @param cardinality
	 */
	private void addDfgDirectFollow(Dfg dfg, ArrayList<XEventClass> dfKey, long cardinality) {
		
		XEventClass source = dfKey.get(0);
		XEventClass target = dfKey.get(1);
		// if  source is start, then we add to dfg start activity, but we need to make sure there is only one index for it
		if(source.getId().equals(Configuration.START_LABEL)) {
			dfg.addStartActivity(source, cardinality);
		}
		// if target is end, we add it to the dfg end activity
		if(target.getId().equals(Configuration.END_LABEL)) {
			dfg.addEndActivity(target, cardinality);
		}
		// else we need just add it simply to dfg
		if(!source.getId().equals(Configuration.START_LABEL) && !target.getId().equals(Configuration.END_LABEL))
			dfg.addDirectlyFollowsEdge(dfKey.get(0), dfKey.get(1),cardinality);
	}

	private boolean isContainActivity(Dfg new_dfg, XEventClass xEventClass) {
		for(XEventClass eventClass: new_dfg.getActivities()) {
			if(xEventClass.getId().contains(eventClass.getId()))
				return true;
		}
		return false;
	}

	private void addDirectFollowMatrix(Map<ArrayList<XEventClass>, ArrayList<Long>> dfMatrix, Dfg dfg, int colIdx) {
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
			addMatrixItemWithCheck(dfMatrix, source, target, cardinality, colIdx);
		}
		
		// add the start and end activities as a direct follow relation there, 
		final int startIdx = dfMatrix.size();
		XEventClass originalPoint = new XEventClass(Configuration.START_LABEL, startIdx);
		for(XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass); // set the start and end activity in existing model
			addMatrixItemWithCheck(dfMatrix, originalPoint, startEventClass, cardinality, colIdx);
		}
		final int endIdx = dfMatrix.size(); // actually we don't need to build this every time we go to the dfg
		XEventClass endPoint = new XEventClass(Configuration.END_LABEL, endIdx); 
		for(XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			addMatrixItemWithCheck(dfMatrix, endEventClass, endPoint, cardinality, colIdx);
		}
	}

	private void addMatrixItemWithCheck(Map<ArrayList<XEventClass>, ArrayList<Long>> dfMatrix, XEventClass source, XEventClass target, long cardinality, int colIdx) {
		if(dfMatrix.size()<1) {
			addMatrixItem(dfMatrix, source, target, cardinality, colIdx);
			
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
					
					addMatrixItem(dfMatrix, source, target, cardinality, colIdx);
				}
			}
		}
	}
	private void addMatrixItem(Map<ArrayList<XEventClass>, ArrayList<Long>> dfMatrix, XEventClass source, XEventClass target, long cardinality, int colIdx) {
		ArrayList<XEventClass> dfKey ;
		ArrayList<Long> dfValue ;
		dfKey = new ArrayList<XEventClass>(2);
		dfKey.add(source);
		dfKey.add(target);
		
		dfValue = new ArrayList<Long>(3);
		// to make sure not overflow
		dfValue.add(0l);
		dfValue.add(0l);
		dfValue.add(0l);
		// we assign the 
		dfValue.set(colIdx, cardinality );
		dfMatrix.put(dfKey, dfValue);
	}

	private void mergeDfgStartEnd(Dfg new_dfg, Dfg dfg) {
		
		
	}
}
