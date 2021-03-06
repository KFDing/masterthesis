package org.processmining.incorporatenegativeinformation.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.incorporatenegativeinformation.algorithms.PN2DfgTransform;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.processtree.ProcessTree;

/**
 * this class is used to store the direct follow relation from existing, pos and
 * neg dfg. It includes cardinality for each element, one original value, one
 * updated value. Methods: -- one to add elements into it -- one to update
 * cardinality w.r.t. weight and colIdx -- one to generate the new dfg
 * 
 * @author dkf
 *
 */
public class DfMatrix {

	Map<ArrayList<XEventClass>, ArrayList<Double>> dfMatrix;// = new HashMap<ArrayList<XEventClass>, ArrayList<Long>>();
	final int keyColNum;
	final int valueColNum;
	long standardCardinality;
	double threshold = ProcessConfiguration.DFG_THRESHOLD;
	// we set start and end activities for the dfg, 
	// use hashCode not help it, so we need to store the ArrayList<XEventClass> for this 
	
	List<ArrayList<XEventClass>> startAct;
	List<ArrayList<XEventClass>> endAct;
	// only initialization of DfMatrix
	public DfMatrix() {
		dfMatrix = new HashMap<ArrayList<XEventClass>, ArrayList<Double>>();
		keyColNum = ProcessConfiguration.MATRIX_KEY_COL_NUM;
		valueColNum = ProcessConfiguration.MATRIX_VALUE_COL_NUM * 2;
		startAct = new ArrayList<ArrayList<XEventClass>>();
		endAct = new ArrayList<ArrayList<XEventClass>>();
	}

	public void setStandardCardinality(long cardiality) {
		standardCardinality = cardiality; 
	}

	public long getStandardCardinality() {
		return standardCardinality;
	}

	public static DfMatrix createDfMatrix(PluginContext context, XLog log, Petrinet net, Marking marking, XEventClassifier classifier) throws ConnectionCannotBeObtained {
		Dfg dfg = PN2DfgTransform.transformPN2Dfg(context, net, marking);
		// int num = XLogInfoFactory.createLogInfo(log).getNumberOfTraces();
		// PN2DfgTransform.setCardinality(dfg, num);
		// -- incorporate the negative information and give out the Dfg and Petri net model
		XLog[] result = EventLogUtilities.splitLog(log, Configuration.POS_LABEL, "true");
		XLog pos_log = result[0];
		XLog neg_log = result[1];
		
		IMLog IM_poslog = new IMLogImpl(pos_log, classifier, null);
		Dfg pos_dfg = new IMLog2IMLogInfoDefault().createLogInfo(IM_poslog).getDfg();
		
		IMLog IM_neglog = new IMLogImpl(neg_log, classifier, null);
		Dfg neg_dfg = new IMLog2IMLogInfoDefault().createLogInfo(IM_neglog).getDfg();
		
		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		DfMatrix dfMatrix = DfMatrix.createDfMatrix(dfg, pos_dfg, neg_dfg);
		dfMatrix.setStandardCardinality(pos_log.size() + neg_log.size());
		return dfMatrix;
	}

	public static DfMatrix createDfMatrix(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		// here we need to update the codes for accepting double percent 

		DfMatrix dfMatrix = new DfMatrix();
		// get all cardinality in each Dfg graph
		long ext_cardinality = getTotalCardinality(dfg);
		long pos_cardinality = getTotalCardinality(pos_dfg);
		long neg_cardinality = getTotalCardinality(neg_dfg);
		long num = ext_cardinality + pos_cardinality+ neg_cardinality;
		// dfMatrix.setStandardCardinality(num);
		// the cardinality can be high because, we use the total cardinality to assign them
		// so here, if we want to use them, we get such number values.
		// here we don't need magical number, but they should exist, or zero
		dfMatrix.addDirectFollowMatrix(dfg, 0, ext_cardinality);
		// one problem here is about the single direct follow relation, it doesn't show here
		dfMatrix.addDirectFollowMatrix(pos_dfg, 1, pos_cardinality);

		dfMatrix.addDirectFollowMatrix(neg_dfg, 2, neg_cardinality);
		// after we have dfMatrix, we need to assign edges to new dfg w.r.t. different situations
		// Dfg new_dfg = dfMatrix.buildDfs();

		return dfMatrix;
	}
	
	public static long getTotalCardinality(Dfg dfg) {
		// TODO get all the cardinality of this dfg
		long sum = 0, cardinality;
		for(long edgeIndex: dfg.getDirectlyFollowsEdges()) {
			cardinality = dfg.getDirectlyFollowsEdgeCardinality(edgeIndex);
			sum+= cardinality;
		}
		for(int idx: dfg.getStartActivityIndices()) {
			long sc = dfg.getStartActivityCardinality(idx);
			sum+= sc;
		}
		for(int idx: dfg.getEndActivityIndices()) {
			long ec = dfg.getEndActivityCardinality(idx);
			sum+= ec;
		}
		return sum;
	}

	public void addDirectFollowMatrix(Dfg dfg, int colIdx, double cardinality_sum) {
		/*
		 * read from each dfg, put edges into the new map, ======if the map has
		 * the edge, then mark which exists, ======if not then create new edge
		 * into the new, but also mark it
		 */
		for (long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			XEventClass target = dfg.getDirectlyFollowsEdgeTarget(idx);

			long cardinality = dfg.getDirectlyFollowsEdgeCardinality(idx);

			// we need to get the percent of source->target in all source->* 
			// so firstly we need to get the all source cardinality?? but how about the existing ones?? how to compare it ??
			double denomial = getOutEdgesCardinality(dfg, source);
			/*
			if (colIdx == 0) {
				denomial = getOutEdgesCardinality(dfg, source);
			} else {
				// there are also other actions here... 
				// for each the directly-follows relation we use the same as it here
				// what is the 
				denomial = getOutEdgesCardinality(dfg, source);
				// denomial = standardCardinality;
			}
			*/
			// modified at 09 April 2019, to use a different normalization method
			// percent = cardinality/(all cardinality in this directly-follows graph)
			// proof : side effect, 
			// (1). if we have ext: pos: neg= 1:0:0,
			// the model keeps the same: Because it can not delete any of them
			// (2). if ext: pos: neg= 0:1:0, the final cardinality turns to 
			//  cardinality/(all cardinality in pos) * (all cardinality in event log)
			// so the effect might be affected, because when changing to effective graph,
			// some noise gets affected, but we can also use the weighted trace in event log
			// to have such effect??  
			// weighted cardinality:: C-pos * All Cardinality in Pos + C-neg * All Cardinality in Neg
			// (3). If ext: pos: neg= 0:0:1, ignore the model
			
			// double percent = 1.0 * cardinality / cardinality_sum;
			double percent = 1.0 * cardinality / denomial;
			addMatrixItem(source, target, percent, colIdx);
		}

		// add the start and end activities as a direct follow relation there, 
		final int startIdx = -1;
		XEventClass originalPoint = new XEventClass(ProcessConfiguration.START_LABEL, startIdx);
		for (XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass); // set the start and end activity in existing model
			// addMatrixItemWithCheck( originalPoint, startEventClass, cardinality, colIdx);
			// for the startEvent, we can't count the use the directfollow edge, because it begins from the first position
			// then we need to count all the start activity and then get the percent to it
			
			double denomial = 1;
			denomial = getStartEventCardinality(dfg);
			/*
			if (colIdx == 0) {
				denomial = getStartEventCardinality(dfg);
			} else
				denomial = getStartEventCardinality(dfg);
				// denomial = standardCardinality;
			*/
			double percent = 1.0 * cardinality / denomial;
			addMatrixItem(originalPoint, startEventClass, percent, colIdx);
			ArrayList<XEventClass> dfKey;
			dfKey = new ArrayList<XEventClass>(keyColNum);
			dfKey.add(originalPoint);
			dfKey.add(startEventClass);
			if(!startAct.contains(dfKey))
				startAct.add(dfKey);
		}
		final int endIdx = -2; // actually we don't need to build this every time we go to the dfg
		XEventClass endPoint = new XEventClass(ProcessConfiguration.END_LABEL, endIdx);
		for (XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			// addMatrixItemWithCheck( endEventClass, endPoint, cardinality, colIdx);
			
			double denomial = getEndEventCardinality(dfg);
			/*
			if (colIdx == 0) {
				denomial = getEndEventCardinality(dfg);
			} else
				denomial = getEndEventCardinality(dfg);
				// denomial = standardCardinality;
			*/
			double percent = 1.0 * cardinality / denomial;
			addMatrixItem(endEventClass, endPoint, percent, colIdx);
			
			ArrayList<XEventClass> dfKey;
			dfKey = new ArrayList<XEventClass>(keyColNum);
			dfKey.add(endEventClass);
			dfKey.add(endPoint);
			if(!endAct.contains(dfKey))
				endAct.add(dfKey);
		}
	}

	private long getStartEventCardinality(Dfg dfg) {
		long sum = 0;
		for (XEventClass startEventClass : dfg.getStartActivities()) {
			long cardinality = dfg.getStartActivityCardinality(startEventClass);
			sum += cardinality;
		}
		return sum;
	}

	private long getEndEventCardinality(Dfg dfg) {
		long sum = 0;
		for (XEventClass endEventClass : dfg.getEndActivities()) {
			long cardinality = dfg.getEndActivityCardinality(endEventClass);
			sum += cardinality;
		}
		return sum;
	}

	private void addMatrixItem(XEventClass source, XEventClass target, double percent, int colIdx) {
		ArrayList<XEventClass> dfKey;
		dfKey = new ArrayList<XEventClass>(keyColNum);
		dfKey.add(source);
		dfKey.add(target);

		ArrayList<Double> dfValue = new ArrayList<Double>(valueColNum);
		// to make sure not overflow
		for (int i = 0; i < valueColNum; i++)
			dfValue.add(0.0);
		// we assign the 
		dfValue.set(colIdx, percent);
		// for update use
		dfValue.set(colIdx + ProcessConfiguration.MATRIX_VALUE_COL_NUM, percent);

		if (isContainKey(dfKey)) {
			// still this key is somehow different from others
			// dfMatrix.get(dfKey).set(colIdx, cardinality);
			// dfMatrix.get(dfKey).set(colIdx + Configuration.MATRIX_VALUE_COL_NUM, cardinality );
			// System.out.println(dfKey.get(0));
			setValue(dfKey, dfValue);
		} else {
			dfMatrix.put(dfKey, dfValue);
		}

	}

	private List<XEventClass> getOutEdges(Dfg dfg, XEventClass sourceNode) {
		// List<XEventClass> edges = new ArrayList<XEventClass>();
		List<XEventClass> edges = new ArrayList<XEventClass>();
		// check for all the edges if they have the same label as source, then we keep the edges into it
		// should with carnality of only target?? If it is with cardinality, it save us time to search 
		// it again, but the structure should be changed. 
		for (long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			if (isEventClassSame(sourceNode, source)) {
				XEventClass target = dfg.getDirectlyFollowsEdgeTarget(idx);
				edges.add(target);
			}
		}
		return edges;
	}

	private long getOutEdgesCardinality(Dfg dfg, XEventClass sourceNode) {
		long sum = 0;
		for (long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			XEventClass source = dfg.getDirectlyFollowsEdgeSource(idx);
			if (isEventClassSame(sourceNode, source)) {
				long cardinality = dfg.getDirectlyFollowsEdgeCardinality(idx);
				sum += cardinality;
			}
		}
		return sum;
	}

	private boolean isContainKey(ArrayList<XEventClass> dfKey) {
		for (ArrayList<XEventClass> key : dfMatrix.keySet()) {
			if (isEventClassSame(dfKey.get(0), key.get(0)) && isEventClassSame(dfKey.get(1), key.get(1))) {
				return true;
			}
		}
		return false;
	}

	private boolean isEventClassSame(XEventClass event1, XEventClass event2) {
		if (event1.getId().equals(event2.getId()))
			return true;
		return false;
	}

	public Object getValue(ArrayList<XEventClass> dfKey) {
		for (ArrayList<XEventClass> key : dfMatrix.keySet()) {
			if (dfKey.get(0).getId().equals(key.get(0).getId()) && dfKey.get(1).getId().equals(key.get(1).getId())) {
				return dfMatrix.get(key);
			}
		}
		System.out.println("there is no such key and value in dfMatrix");
		return null;
	}

	public void setValue(ArrayList<XEventClass> dfKey, ArrayList<Double> dfValue2) {
		@SuppressWarnings("unchecked")
		ArrayList<Double> dfValue = (ArrayList<Double>) getValue(dfKey);
		// System.out.println(dfValue.get(3));
		// we can only change on the dfValue
		for (int i = 0; i < valueColNum; i++) {
			// System.out.println("id " + i+ dfValue2.get(3));
			dfValue.set(i, dfValue2.get(i) + dfValue.get(i));
		}
	}

	// to update the value of dfMatrix according to weight and colIdx
	public void updateCardinality(int colIdx, double weight) {
		for (ArrayList<Double> dfValue : dfMatrix.values()) {
			dfValue.set(colIdx + ProcessConfiguration.MATRIX_VALUE_COL_NUM, dfValue.get(colIdx) * weight);
		}
	}

	/**
	 * some errors happen, because of the values on them, we have dfMatrix in
	 * double, and it represents the percentage to transform.. Then how to judge
	 * the effect of them, right noe?? We have rules, we keep t, if first, to
	 * know how to assign them into the matrix:: -- if pos + existing > neg, we
	 * keep it??? Else, we leave it out??
	 * 
	 * @param dfg
	 */
	public Dfg buildDfg() {
		Dfg dfg = new DfgImpl();
		// read of each item in dfMatrix 
		double keepPercent, removePercent;
		for (Map.Entry<ArrayList<XEventClass>, ArrayList<Double>> entry : dfMatrix.entrySet()) {
			ArrayList<XEventClass> dfKey = entry.getKey();
			ArrayList<Double> dfValue = entry.getValue();
			// here must be some situations we need to consider more 
			// existing weigths holds, change the parameters, we can do, but add negative
			// values on it 
			keepPercent = dfValue.get(ProcessConfiguration.MATRIX_POS_IDX)
					+ dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX);
			removePercent = dfValue.get(ProcessConfiguration.MATRIX_NEG_IDX);
			double diffPercent = Math.min(keepPercent - removePercent, 1.0);
			if (diffPercent > threshold) {
				addDfgDirectFollow(dfg, dfKey.get(0), dfKey.get(1), transform2Cardinality(diffPercent));
			}
		}
		
		if(!dfg.getDirectlyFollowsEdges().iterator().hasNext())
			return dfg;
		// after this we need to check the directly-follows graph
		// for the start and end activities only
		// if all the start activities become 0, we need to add them specially, we choose the one with
		// best value for the diffPercent..It means that we need to recalculate it again
		if(dfg.getStartActivityIndices().length < 1) {
			// the start is empty
			// if all the start activities become 0, we need to add them specially, we choose the one with
			// best value for the diffPercent..It means that we need to recalculate it again
			// visit dfMatrix get the start Activities
			double maxDiff = -2; 
			int maxIdx = 0;
			for(int i=0; i< startAct.size(); i++) {
				ArrayList<XEventClass> start = startAct.get(i);
				ArrayList<Double> dfValue = dfMatrix.get(start);
				keepPercent = dfValue.get(ProcessConfiguration.MATRIX_POS_IDX)
						+ dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX);
				removePercent = dfValue.get(ProcessConfiguration.MATRIX_NEG_IDX);
				double diffPercent = keepPercent - removePercent;
				if(maxDiff < diffPercent) {
					maxDiff = diffPercent;
					maxIdx = i;
				}
			}
			// choose this start activities as the start activities and assign what cardinality? 
			ArrayList<XEventClass> refStart = startAct.get(maxIdx);
			int cardinality = (int)(transform2Cardinality(Math.abs(maxDiff)))  +1;
			dfg.addStartActivity(refStart.get(1), cardinality);
		}	
		
		if(dfg.getEndActivityIndices().length < 1) {
			// the start is empty
			// if all the start activities become 0, we need to add them specially, we choose the one with
			// best value for the diffPercent..It means that we need to recalculate it again
			// visit dfMatrix get the start Activities
			double maxDiff = -2; 
			int maxIdx = 0;
			for(int i=0; i< endAct.size(); i++) {
				ArrayList<XEventClass> end = endAct.get(i);
				ArrayList<Double> dfValue = dfMatrix.get(end);
				keepPercent = dfValue.get(ProcessConfiguration.MATRIX_POS_IDX)
						+ dfValue.get(ProcessConfiguration.MATRIX_EXISTING_IDX);
				removePercent = dfValue.get(ProcessConfiguration.MATRIX_NEG_IDX);
				double diffPercent = keepPercent - removePercent;
				if(maxDiff < diffPercent) {
					maxDiff = diffPercent;
					maxIdx = i;
				}
			}
			// choose this start activities as the start activities and assign what cardinality? 
			ArrayList<XEventClass> refEnd = endAct.get(maxIdx);
			int cardinality = (int)(transform2Cardinality(Math.abs(maxDiff) ))  +1;
			dfg.addEndActivity(refEnd.get(0), cardinality);
		}
		
		return dfg;
	}
	
	public ProcessTree buildProcessTree(DfgMiningParameters ptParas) {
		Dfg dfg = buildDfg();
		
		ProcessTree pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
		return pTree;
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
		// <2> according to the source outedges cardinality in pos 
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

	private void addDfgDirectFollow(Dfg dfg, XEventClass source, XEventClass target, long cardinality) {

		// if  source is start, then we add to dfg start activity, but we need to make sure there is only one index for it
		if (source.getId().equals(ProcessConfiguration.START_LABEL)) {
			dfg.addStartActivity(target, cardinality);
		}
		// if target is end, we add it to the dfg end activity
		if (target.getId().equals(ProcessConfiguration.END_LABEL)) {
			dfg.addEndActivity(source, cardinality);
		}
		// else we need just add it simply to dfg
		if (!source.getId().equals(ProcessConfiguration.START_LABEL)
				&& !target.getId().equals(ProcessConfiguration.END_LABEL))
			dfg.addDirectlyFollowsEdge(source, target, cardinality);
	}

}
