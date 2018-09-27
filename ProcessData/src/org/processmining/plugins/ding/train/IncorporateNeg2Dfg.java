package org.processmining.plugins.ding.train;

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
	@Plugin(name = "Incorporate Log with Labels into Dfg", level = PluginLevel.Regular, returnLabels = {"DfMatrix" }, returnTypes = {
			DfMatrix.class}, parameterLabels = { "Log" ,"Dfg"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Incorporate Log with Labels into Dfg",  requiredParameterLabels = { 0, 1})
	public DfMatrix buildModel(UIPluginContext context, XLog log, Dfg dfg) throws UnknownTreeNodeException {
		// create the 3 matrix of directly follow relation
		Object[] result = splitEventLog(log);
		XLog pos_log = (XLog) result[0];
		XLog neg_log = (XLog) result[1];
		// 
		XLog2Dfg ld = new XLog2Dfg();
		Dfg pos_dfg = ld.log2Dfg(context, pos_log);
		Dfg neg_dfg = ld.log2Dfg(context, neg_log);
		
		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		DfMatrix dfMatrix = createDfMatrix(dfg, pos_dfg, neg_dfg);
		
		// now we need to adjust the complete the features it has. 
		// 1. accept the threshold adjust on the result panel
		
		// 2. adjust the result w.r.t. different weight
		
		// 3. to have the process tree, that's the end. 
		
		// transform process tree into Petri net ?? No, we don't need it
		
		return dfMatrix;
	}
	
	
	
	private DfMatrix createDfMatrix(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		
		
		DfMatrix dfMatrix = new DfMatrix();
		dfMatrix.addDirectFollowMatrix( dfg, 0);
		// one problem here is about the single direct follow relation, it doesn't show here
		dfMatrix.addDirectFollowMatrix( pos_dfg, 1); 
		dfMatrix.addDirectFollowMatrix(neg_dfg, 2);
		// after we have dfMatrix, we need to assign edges to new dfg w.r.t. different situations
		// Dfg new_dfg = dfMatrix.buildDfs();
		
		return dfMatrix;
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
}
