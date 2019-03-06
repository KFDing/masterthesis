package org.processmining.incorporatenegativeinformation.models;

import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XTrace;
/**
 * this class extends the TraceVariant by adding the pos and neg number for each trace, 
 * to benefit the fitness counts
 * @author dkf
 *
 */
public class LabeledTraceVariant extends TraceVariant{
	private int posNum =0;
	private int negNum =0;
	
	boolean isPos;
	public LabeledTraceVariant(List<XEventClass> toTraceClass, XTrace trace, int idx, boolean isPos) {
		super(toTraceClass, trace, idx);
		if(isPos)
			addPosNum(1);
		else
			addNegNum(1);
	}
	// should we change the mechanism?? if pos is greater than neg, then we see it is pos?? 
	// we can test at first this effect and check later.
	public boolean isPos() {
		if(posNum>negNum)
			isPos = true;
		return isPos;
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
