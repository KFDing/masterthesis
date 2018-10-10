package org.processmining.plugins.ding.process.dfg.train;

import java.util.Iterator;

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
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.process.dfg.model.DfMatrix;



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
	public DfMatrix buildDfMatrixModel(UIPluginContext context, XLog log, Dfg dfg) {
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
		
		int num = XLogInfoFactory.createLogInfo(pos_log).getNumberOfTraces();
		dfMatrix.setStandardCardinality(num);
		// now we need to adjust the complete the features it has. 
		// 1. accept the threshold adjust on the result panel
		
		// 2. adjust the result w.r.t. different weight
		
		// 3. to have the process tree, that's the end. 
		
		// transform process tree into Petri net ?? No, we don't need it
		
		return dfMatrix;
	}
	
	@Plugin(name = "Incorporate Log with Labels into Dfg", level = PluginLevel.Regular, returnLabels = {"New Dfg", "existing Dfg","Pos Dfg","Same effect" }, returnTypes = {
			Dfg.class, Dfg.class, Dfg.class, Dfg.class}, parameterLabels = { "Log" ,"Dfg"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Generate new Dfg",  requiredParameterLabels = { 0, 1})
	public static Object[] buildDfgModel(UIPluginContext context, XLog log, Dfg dfg) {
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
		
		// how to make sure the structure to generate is right?? 
		// test cases: 
		// --  if we only have the structure of pos_dfg, existing dfg, and with all the effect of neg_dfg?? 
		// --1.  same structure of existing ones, it means, that we need make pos and neg into 0
		// --2.  same structure of pos_dfg, make existing and neg_dfg into 0
		// --3.  pos and existing is reduce by the neg effect
		
		Dfg new_dfg = dfMatrix.buildDfs(); 
		// Case 1. 
		dfMatrix.updateCardinality(1, 1.0);
		dfMatrix.updateCardinality(1, 0);
		dfMatrix.updateCardinality(2, 0);
		Dfg new_dfg1 = dfMatrix.buildDfs();
		
		// Case 2. pos_dfg
		dfMatrix.updateCardinality(0, 0);
		dfMatrix.updateCardinality(1, 1.0);
		dfMatrix.updateCardinality(2, 0);
		Dfg new_dfg2 = dfMatrix.buildDfs();
		
		// Case 3. 
		dfMatrix.updateCardinality(0, 1.0);
		dfMatrix.updateCardinality(1, 1.0);
		dfMatrix.updateCardinality(2, 1.0);
		Dfg new_dfg3 = dfMatrix.buildDfs();
		
		return new Object[] {new_dfg, new_dfg1, new_dfg2, new_dfg3};
	}
	
	public static DfMatrix createDfMatrix(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		// here we need to update the codes for accepting double percent 
		
		DfMatrix dfMatrix = new DfMatrix();
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
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				if(!attr.getValue()) {
					iterator.remove();
				}
			}
		}
		
		iterator =neg_log.iterator();
		while (iterator.hasNext()) {
			XTrace trace = iterator.next();
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
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
