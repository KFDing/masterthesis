package org.processmining.plugins.ding.preprocess.util;

import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.ding.preprocess.TraceVariant;
/**
 * this class extends the TraceVariant by adding the pos and neg number for each trace, 
 * to benefit the fitness counts
 * @author dkf
 *
 */
public class LabeledTraceVariant extends TraceVariant{
	private int posNum =0;
	private int negNum =0;
	
	
	public LabeledTraceVariant(List<XEventClass> toTraceClass, XTrace trace, int idx, boolean isPos) {
		super(toTraceClass, trace, idx);
		if(isPos)
			addPosNum(1);
		else
			addNegNum(1);
	}
	public int getPosNum() {
		// but how to get it, we need to do something else
		return posNum;
	}
	public void setPosNum(int posNum) {
		this.posNum = posNum;
	}
	
	public void addPosNum(int num) {
		posNum += num;
	}
	
	public int getNegNum() {
		return negNum;
	}
	public void setNegNum(int negNum) {
		this.negNum = negNum;
	}
	
	public void addNegNum(int num) {
		negNum += num;
	}
	public void addTrace(XTrace trace, int idx, boolean isPos) {
		// TODO Auto-generated method stub
		addTrace(trace, idx);
		if(isPos)
			addPosNum(1);
		else
			addNegNum(1);
	}
	
}
