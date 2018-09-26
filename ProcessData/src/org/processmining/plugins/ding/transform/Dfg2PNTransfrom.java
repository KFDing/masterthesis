package org.processmining.plugins.ding.transform;

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
import org.processmining.plugins.ding.train.Configuration;

/**
 * this class is used to tranform Dfg graph into Petri net 
 */

public class Dfg2PNTransfrom {

	/**
	 * it is an plugin in that accepts the dfg, then generate the Petri net work, but we could do some experience on it
	 *  --  how to split the event log
	 *  --  how to change log into dfg 
	 *  --  how to import the dfg transform in existing method
	 *  
	 */
	@Plugin(name = "Test on Dfg to PN", level = PluginLevel.Regular, returnLabels = {"Pos-XLog", "Neg-XLog"}, returnTypes = {XLog.class, XLog.class
			}, parameterLabels = { "Log"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Split into differnt log",  requiredParameterLabels = { 0})
	public Object [] splitEventLog(UIPluginContext context, XLog log) {
		//return splitLogClear(log);
		return splitEventLogNoClear(log);
	}
	
	
	@Plugin(name = "Test on Dfg to PN", level = PluginLevel.Regular, returnLabels = { "pos-Dfg"}, returnTypes = {Dfg.class}, parameterLabels = { "Log"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Transfrom Log into Dfg",  requiredParameterLabels = { 0})
	public Dfg transfromLog2Dfg(UIPluginContext context, XLog log) {
		XLog2Dfg ld = new XLog2Dfg();
		Dfg dfg = ld.log2Dfg( context, log);
		return dfg;
	}
	
	
	public Object[] splitLogClear(XLog log){
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
	
	public Object[] splitEventLogNoClear(XLog log){
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
				if(attr.getValue()) {
					iterator.remove();
				}
			}
		}
		
		return new Object[] {pos_log, neg_log};
	}
	
	public Object[] splitEventLogFresh(XLog log){
		XLog pos_log = null; //XFactoryRegistry.instance().currentDefault().
		XLog neg_log = (XLog) log.clone();
		
		int neg_count = 0, pos_count=0;
		// in this way it doesn't work, but where goes wrong..
		for(int i =0; i< log.size(); i++) {
			XTrace trace = (XTrace) log.get(i);
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				if(!attr.getValue()) {
					// neg_log.add(trace);
					pos_log.remove(i);
					neg_count++;
				}else {
					// pos_log.add(trace);
					neg_log.remove(i);
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
