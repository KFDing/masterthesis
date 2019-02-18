package org.processmining.plugins.ding.process.dfg.transform;

import java.util.List;

import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.processtree.ProcessTreeElement;

public class PostProcessCorrelation {
	public List<LTRule<ProcessTreeElement>> postProcessData(String classifer, List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		if(classifer.equals(ProcessConfiguration.DECISION_TREE)) {
			return postProcessDataForDT(branchList);
		}else if(classifer.equals(ProcessConfiguration.ASSOCIATIOn_RULE)) {
			return postProcessDataForAT(branchList);
		}else if(classifer.equals(ProcessConfiguration.ILP)) {
			return postProcessDataForILP(branchList);
		}else {
			System.out.println("Unknown classifier");
		}
		return null;
	}

	private List<LTRule<ProcessTreeElement>> postProcessDataForILP(List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<LTRule<ProcessTreeElement>> postProcessDataForAT(List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO tansfer the data from 
		return null;
	}

	private List<LTRule<ProcessTreeElement>> postProcessDataForDT(List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
