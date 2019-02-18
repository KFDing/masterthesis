package org.processmining.plugins.ding.process.dfg.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.ding.preprocess.util.Configuration;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class DataTransfer {
	Map<Node, XEventClass> tlmaps; 
	ProcessTree tree;
	XLog log;
	
	
	public DataTransfer(XLog xlog, ProcessTree ptree) {
		log = xlog;
		tree = ptree;
		// we need to make sure for which classifier, we transfer data
		tlmaps = getProcessTree2EventMap(log, tree, null);
	}
	
	public Instances[] transferData(String classifer, List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		if(classifer.equals(ProcessConfiguration.DECISION_TREE)) {
			return trasferDataForDT(branchList, variants);
		}else if(classifer.equals(ProcessConfiguration.ASSOCIATIOn_RULE)) {
			return trasferDataForAT(branchList, variants);
		}else if(classifer.equals(ProcessConfiguration.ILP)) {
			return trasferDataForILP(branchList, variants);
		}else {
			System.out.println("Unknown classifier");
		}
		return null;
	}
	
	private Instances[] trasferDataForILP(List<XORBranch<ProcessTreeElement>> branchList,
			List<LabeledTraceVariant> variants) {
		// TODO Auto-generated method stub
		return null;
	}

	private Instances[] trasferDataForAT(List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		// TODO transfer data for association rule by only using positive examples
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		Attribute attribute = null;
		List<String> normalValues = new ArrayList<String>();
		normalValues.add("Yes"); // 1 represents existing in the data
		normalValues.add("No"); // 
		for(XORBranch<ProcessTreeElement> branch: branchList) {
			// assign one name for it, the value is boolean, exist or not 
			attribute = new Attribute(branch.getLabel(), normalValues);
			atts.add(attribute);
		}

		Instances posData = new Instances("PosTrainDataForAT", atts, 0);
		Instances negData = new Instances("NegTrainDataForAT", atts, 0);
		
		for(LabeledTraceVariant var : variants) {
			// for each var, we create one row into the data info, find out all the relative branch 
			// which shows in variant and then assign the value on it
			
			List<XEventClass> traceVariant = var.getTraceVariant();
			// double[] newVals = new double[data.numAttributes()];
			Instance instance = new DenseInstance(posData.numAttributes());
			int k=0;
			// here we only use information if it exisits here. if it, then we what we do, else,
			// we might use much more data, I guess, we have two values for one part, we create
			// two atrribute for them, one is pos, the other is neg, then we use number to see the effect
			for(XORBranch<ProcessTreeElement> branch: branchList) {
				if(containBranch(traceVariant, branch)) {
					// the other we think there are no data for it 
					instance.setValue(atts.get(k), "Yes");
				}
				k++;
			}
			
			instance.setWeight(var.getCount());
			if(var.isPos()) {
				instance.setDataset(posData);
				posData.add(instance);
			}else {
				instance.setDataset(negData);
				negData.add(instance);
			}
			
		}
		return new Instances[] {posData, negData};
	}

	// after we fill item into connInfo, we have created the table for computation, we transfer it into Instance
	public Instances[] trasferDataForDT(List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		// transfer data into instances, this format and then use it do the classification
		// I would see now it is value decision tree
		
		// the attributes are from branchlist
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		Attribute attribute = null;
		List<String> normalValues = new ArrayList<String>();
		normalValues.add("Yes"); // 1 represents existing in the data
		normalValues.add("No"); // 
		for(XORBranch<ProcessTreeElement> branch: branchList) {
			// assign one name for it, the value is boolean, exist or not 
			attribute = new Attribute(branch.getLabel(), normalValues);
			atts.add(attribute);
		}
		
		attribute = new Attribute(ProcessConfiguration.POS_LABEL, normalValues);
		atts.add(attribute);
		
		Instances data = new Instances("TrainDataForDT", atts, 0);
		
		for(LabeledTraceVariant var : variants) {
			// for each var, we create one row into the data info, find out all the relative branch 
			// which shows in variant and then assign the value on it
			List<XEventClass> traceVariant = var.getTraceVariant();
			// double[] newVals = new double[data.numAttributes()];
			Instance instance = new DenseInstance(data.numAttributes());
			int k=0;
			// here we only use information if it exisits here. if it, then we what we do, else,
			// we might use much more data, I guess, we have two values for one part, we create
			// two atrribute for them, one is pos, the other is neg, then we use number to see the effect
			for(XORBranch<ProcessTreeElement> branch: branchList) {
				if(containBranch(traceVariant, branch)) {
					// we need to get the idx and then assign the value to it 
					// newVals[k] = 1;
					instance.setValue(atts.get(k), "Yes");
				}else
					// newVals[k] = 0;
					instance.setValue(atts.get(k), "No");
				k++;
			}
			// assign the class label to newVals
			if(var.isPos())
				// newVals[k] = 1;
				instance.setValue(atts.get(k), "Yes");
			else
				// newVals[k] = 0;
				instance.setValue(atts.get(k), "No");
			// the same weight for all tinstances
			
			instance.setWeight(var.getCount());
			instance.setDataset(data);
			data.add(instance);
			
		}
		
		data.setClassIndex(data.numAttributes() - 1);
		return new Instances[] {data};
	}
	

	// check if there traceVariant has executed this branch
	private boolean containBranch(List<XEventClass> traceVariant, XORBranch<ProcessTreeElement> branch) {
		// TODO Auto-generated method stub
		// first we need to make change the branch into XEventClass and then we can compare the values on them
		List<ProcessTreeElement> beginNodes = branch.getBeginNodes();
		XEventClass bNode = tlmaps.get(beginNodes.get(0));
		// if a branch happens in this traceVariant, if one of the beginNodes are contained into traceVariant
		if(traceVariant.contains(bNode)) {
			return true;
		}else 
			return false;
	}
	
	private Map<Node, XEventClass> getProcessTree2EventMap(XLog xLog, ProcessTree pTree, XEventClassifier classifier) {
		// TODO generate the transfer from process tree to event classes in log
		Map<Node, XEventClass> map = new HashMap<Node, XEventClass>();
		Collection<Node> nodes = pTree.getNodes();
		// leave only the leaf nodes
		
		
		XEventClasses classes = null;
		if(classifier != null && log.getClassifiers().contains(classifier)) 
			classes = XLogInfoFactory.createLogInfo(xLog).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(xLog).getNameClasses();
		
		XEventClass tauClassinLog = new XEventClass(Configuration.Tau_CLASS, classes.size());
		
		boolean match;
		for (Node node : nodes) {
			if(!node.isLeaf())
				continue;
			
			match = false;
			for (XEventClass eventClass : classes.getClasses()) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs
				// need to check at first what the Name and other stuff
				if (eventClass.getId().equals(node.getName())) {
					map.put(node, eventClass);
					match = true;
					break;
				}
			}
			if(! match) {// it there is node not showing in the event log
				map.put(node, tauClassinLog);
			}
		}
		
		return map;
	}
}
