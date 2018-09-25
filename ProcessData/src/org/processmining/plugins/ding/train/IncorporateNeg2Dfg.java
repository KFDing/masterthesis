package org.processmining.plugins.ding.train;

import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.XLog2Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
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
	@Plugin(name = "Incorporate Log with Labels into Dfg", level = PluginLevel.Regular, returnLabels = {"Petri net", "Initial Marking" }, returnTypes = {
			Petrinet.class, Marking.class}, parameterLabels = { "Log" ,"Dfg"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Generate a petri net",  requiredParameterLabels = { 0})
	public Object[] buildModel(UIPluginContext context, XLog log, Dfg dfg) throws UnknownTreeNodeException, ReductionFailedException {
		// create the 3 matrix of directly follow relation
		XLog pos_log = (XLog) log.clone();
		XLog neg_log = (XLog) log.clone();
		int neg_count = 0, pos_count=0;
	
		Iterator liter = log.iterator();
		
		while(liter.hasNext()) {
			XTrace trace = (XTrace) liter.next();
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				if(!attr.getValue()) {
					pos_log.remove(trace);
					neg_count++;
				}else {
					neg_log.remove(trace);
					pos_count++;
				}
			}
		}
		// 
		XLog2Dfg ld = new XLog2Dfg();
		Dfg pos_dfg = ld.log2Dfg( context, pos_log);
		Dfg neg_dfg = ld.log2Dfg( context, neg_log);
		
		// compare the directly follow category
		
		// firstly get all the collection of direct follow relation
		// then create the vector for them
		// at last to check how to decide which one to keep or not
		
		
		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		Dfg new_dfg = mergeDfgs(dfg, pos_dfg, neg_dfg);
		// add start, end activity to it, but also we need to add activity into the new dfg
		
		
		
		return null;
	}

	private Dfg mergeDfgs(Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		Dfg new_dfg = dfg.clone();
		Map<String, XEventClass> eventClassMap = null;
		
		createActivity(new_dfg, dfg, pos_dfg, neg_dfg);
		
		mergeDfgStartEnd(new_dfg, dfg, pos_dfg, neg_dfg);
		
		createDfgDirectFollow(new_dfg, dfg, pos_dfg, neg_dfg);
		return null;
	}

	private void createActivity(Dfg new_dfg, Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		// we need to create the XEventClass map between log and graph 
		XEventClass[] pa = pos_dfg.getActivities();
		int idx = new_dfg.getActivities().length;
		for(int pi = 0; pi< pa.length; pi++) {
			if(!isContainActivity(new_dfg, pa[pi])) {
				XEventClass peventClass = new XEventClass(pa[pi].getId(), idx++);
				new_dfg.addActivity(peventClass);
			}
		}
		
	}

	private boolean isContainActivity(Dfg new_dfg, XEventClass xEventClass) {
		for(XEventClass eventClass: new_dfg.getActivities()) {
			if(xEventClass.getId().contains(eventClass.getId()))
				return true;
		}
		return false;
	}

	private void createDfgDirectFollow(Dfg new_dfg, Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		// TODO Auto-generated method stub
		
	}

	private void mergeDfgStartEnd(Dfg new_dfg, Dfg dfg, Dfg pos_dfg, Dfg neg_dfg) {
		// TODO Auto-generated method stub
		
	}
}
