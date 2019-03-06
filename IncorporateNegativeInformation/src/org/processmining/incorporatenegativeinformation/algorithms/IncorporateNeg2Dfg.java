package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.Iterator;

import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.incorporatenegativeinformation.models.DfMatrix;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;



/**
 * this class is used to incorporate the negative information with dfg graph
 *  -- create the 3 matrix of directly follow relation, one from existing model, one from pos log, one from neg log
 *  -- compare the directly follow category, and decide if we keep it or not
 *  -- get a new dfg and pass it further as input to generate Petri net
 * @author dkf
 *
 */
public class IncorporateNeg2Dfg {
	
	public static DfMatrix createDfMatrix(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg, int num) {
		// here we need to update the codes for accepting double percent 
		
		DfMatrix dfMatrix = new DfMatrix();
		dfMatrix.setStandardCardinality(num);
		// here we don't need magical number, but they should exist, or zero
		dfMatrix.addDirectFollowMatrix( dfg, 0);
		// one problem here is about the single direct follow relation, it doesn't show here
		dfMatrix.addDirectFollowMatrix( pos_dfg, 1); 
		
		dfMatrix.addDirectFollowMatrix(neg_dfg, 2);
		// after we have dfMatrix, we need to assign edges to new dfg w.r.t. different situations
		// Dfg new_dfg = dfMatrix.buildDfs();
		
		return dfMatrix;
	}

	public static Object[] splitEventLog(XLog log){
		XLog pos_log = (XLog) log.clone();
		XLog neg_log = (XLog) log.clone();
		
		Iterator<XTrace> iterator = pos_log.iterator();
		while (iterator.hasNext()) {
			XTrace trace = iterator.next();
			if(trace.getAttributes().containsKey(ProcessConfiguration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(ProcessConfiguration.POS_LABEL);
				if(!attr.getValue()) {
					iterator.remove();
				}
			}
		}
		
		iterator =neg_log.iterator();
		while (iterator.hasNext()) {
			XTrace trace = iterator.next();
			if(trace.getAttributes().containsKey(ProcessConfiguration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(ProcessConfiguration.POS_LABEL);
				if(!attr.getValue()) {
					// iterator.remove();
					continue;
				}
			}
			iterator.remove();
		}
		// should we give sth information here to tell the log info?
		return new Object[] {pos_log, neg_log};
	}
}
