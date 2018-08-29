package org.processmining.plugins.ding.preprocess;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.ding.util.Configuration;

public class TraceVariant {
	private int count ; 
	private boolean isFit ;
	private List<XEventClass> variant; 
	// here we need to record the log index in this variants.
	// later, we could do the label assign on this variant.
	// or should we add them actually the trace of them?? 
	private List<Integer> idx_list;
	private List<XTrace> trace_list;
	
	private List<Integer> summary;
	
	
	public List<XTrace> getTrace_list() {
		return trace_list;
	}

	public void setTrace_list(List<XTrace> trace_list) {
		this.trace_list = trace_list;
	}
	
	public TraceVariant() {
		count = 0;
		idx_list = new ArrayList<Integer>();
		trace_list = new ArrayList<XTrace>();
	}
	
	public TraceVariant(List<XEventClass>  variant, XTrace trace, int idx){
		this.variant = variant;
		count = 0;
		idx_list = new ArrayList<Integer>();
		trace_list = new ArrayList<XTrace>();
		addTrace(trace, idx);
	}
	
	public List<XEventClass>  getTraceVariant() {
		return variant;
	}
	
	public int getCount() {
		return count;
	}

	
	public int getCount(List<XEventClass>  var) {
		// maybe this comparation could be some tricky but others could be fine, right??
		/*
		if(variant.size() != var.size())
			return -1;
		
		for(int i=0; i< variant.size();i++) {
			if(variant.get(i).getIndex() != var.get(i).getIndex())
				return -1;
		}
		return count;
		*/
		if(variant.equals(var)) {
			return count;
		}
		return -1;
	}
	
	public void setTraceVariant(List<XEventClass> variant) {
		this.variant = variant;
	}
	
	public void addTrace(XTrace trace, int idx) {
		// add the index of this trace into variants list of index
		// should we have one map of them??	
		this.count ++;
		idx_list.add(idx);
		trace_list.add(trace);
	}
	
	public void setFitLabel(boolean isFit) {
		this.isFit = isFit;
	}
	
	public boolean getFitLabel() {
		return isFit;
	}
	public List<Integer> getSummary(){
		if(summary == null)
			setSummary();
		return summary;
	}
	
	public void setSummary() {
		summary = new ArrayList<Integer>();
		
		if(summary.size()<1) {
			summary.add(Configuration.POS_IDX,0);
			summary.add(Configuration.NEG_IDX,0);
		}
		
		for(XTrace trace : trace_list) {
			XAttributeBoolean pos_attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
			if(pos_attr != null) {
				if(pos_attr.getValue())
					summary.set(Configuration.POS_IDX, summary.get(Configuration.POS_IDX) +1);
				else
					summary.set(Configuration.NEG_IDX, summary.get(Configuration.NEG_IDX) +1);
			}
		}
	}
	// we accept some parameters and change it, this could be label parameters , with fit and with pos and then others 
	// we can't really set if it is fit or not, or we can set it ??? 
	public void changeSummary(double pos_prob) {
		// choose the trace and assign variant label, that's all
		
	}
}
