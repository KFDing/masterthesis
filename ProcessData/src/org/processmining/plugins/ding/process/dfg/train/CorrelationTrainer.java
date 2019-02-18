package org.processmining.plugins.ding.process.dfg.train;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.processtree.ProcessTreeElement;

import weka.associations.AssociationRule;
import weka.associations.FPGrowth;
import weka.associations.Item;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
/**
 * we use decision tree to get the correlation of data, it is a classification problem with the attributes there
 * @author dkf
 *
 */
public class CorrelationTrainer {
	
	public void trainCorrelation(String classifer, Instances[] data, List<XORBranch<ProcessTreeElement>> branchList) {
		if(classifer.equals(ProcessConfiguration.DECISION_TREE)) {
			Instances trainData = data[0];
			trainInDecisionTree(trainData);
		}else if(classifer.equals(ProcessConfiguration.ASSOCIATIOn_RULE)) {
			Instances posData = data[0];
			Instances negData = data[1];
			if(posData.size() > 0 ) {
				List<LTRule<XORBranch<ProcessTreeElement>>> posRules = trainInAssociationRule(posData, branchList);
				
			}else
				System.out.println("Pos Data is not enough for training");
			
			if(negData.size() > 1) {
				List<LTRule<XORBranch<ProcessTreeElement>>> negRules = trainInAssociationRule(negData, branchList);
			}else {
				System.out.println("Neg Data is not enough for training");
			}
			// we should return the value from it 
		}else if(classifer.equals(ProcessConfiguration.ILP)) {
			Instances trainData = data[0];
			trainInILP(trainData);
		}
	}
	
	
	private void trainInILP(Instances data) {
		// get correlation from inductive logic programming 
		
	}
	
	/**
	 * here we have two different dataset, one is only for positive examples,
	 * one is for the negative and positive examples, for this, we need the classification 
	 * @param data
	 */
	private List<LTRule<XORBranch<ProcessTreeElement>>> trainInAssociationRule(Instances data, List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO get the correlation from accociation rule
		// find the association rule by positive parts and then check the relation of it
		/*
		Apriori apriori = new Apriori();
        apriori.setClassIndex(data.classIndex());
        apriori.buildAssociations(data);
        */
		
		// Apriori model = new Apriori();
		FPGrowth model = new FPGrowth();
    	try {
    			
        	model.buildAssociations(data);
        	
			//model.buildAssociations(data);
			// System.out.println(model);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	List<LTRule<XORBranch<ProcessTreeElement>>> ltRules = new ArrayList<LTRule<XORBranch<ProcessTreeElement>>>();
    	// if here we want to use the petrinet , it's quite early..
    	List<AssociationRule> ruleList = model.getAssociationRules().getRules();
    	if(ruleList.isEmpty()) {
    		System.out.println("No long-term dependency is found");
    		return null;
    	}
    	int i=0;
    	while(i< ruleList.size()) {
    		AssociationRule rule = ruleList.get(i);
    		
    		// if the premises are all before consequences, we keep it
    		// else no!! 
    		Collection<Item> premise = rule.getPremise();
    		Collection<Item> consequences = rule.getConsequence();
    		
    		int preMaxIdx =0;
    		boolean inOrder = true;
    		for(Item pre: premise) {
    			// get the idx of this pre in branchList
    			int preIdx = getIndex(pre, branchList);
    			if(preMaxIdx < preIdx)
    				preMaxIdx = preIdx;
    		}
    		
    		for(Item con: consequences) {
    			// get the idx of this pre in branchList
    			int conIdx = getIndex(con, branchList);
    			if(conIdx < preMaxIdx) {
    				inOrder = false;
    				break;
    			}
    			
    		}
    		// do we need to organize it into the unique format in our LTRule?? we need to add them, because we need to give 
    		// add them into the branch 
    		if(inOrder) {
    			System.out.println("keep this rule");
    			
    			LTRule<XORBranch<ProcessTreeElement>> ltRule = createLTRule(rule, branchList);
    			ltRules.add(ltRule);
    		}
    		else {
    			System.out.println("abandon this rule");
    		}
    		i++;
    	}
    	
    	return ltRules;
	}
	
	// even if we have those correlation, we need to transfer them into rules that
	// we could put them together and add long-term dependency on it 
	// then the question goes back again to, how to add the long-term dependence on Petri net..
	private LTRule<XORBranch<ProcessTreeElement>> createLTRule(AssociationRule rule,
			List<XORBranch<ProcessTreeElement>> branchList) {
		
		LTRule<XORBranch<ProcessTreeElement>> ltRule = new LTRule<XORBranch<ProcessTreeElement>>();
		
		List<XORBranch<ProcessTreeElement>> sourceList, targetList;
		sourceList = new ArrayList<XORBranch<ProcessTreeElement>>();
		targetList = new ArrayList<XORBranch<ProcessTreeElement>>();
		
		Collection<Item> premise = rule.getPremise();
		Collection<Item> consequences = rule.getConsequence();
		for(Item pre : premise) {
			// we need to add thise branch, right?? to get all the begin node in it
			int preIdx = getIndex(pre, branchList);
			if(preIdx!= -1) {
				XORBranch<ProcessTreeElement> branch  = branchList.get(preIdx);
				sourceList.add(branch);
			}
			
		}
		
		for(Item con : consequences) {
			// we need to add thise branch, right?? to get all the begin node in it
			int conIdx = getIndex(con, branchList);
			if(conIdx!= -1) {
				// if we use the branch, to represent the LTRule, it works, too, only at end
				// we know it 
				XORBranch<ProcessTreeElement> branch  = branchList.get(conIdx);
				targetList.add(branch);
			}
			
		}
		ltRule.addRuleList(sourceList, targetList);
		return ltRule;
	}


	private int getIndex(Item pre, List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO check the name of this pre
		int idx = 0;
		for(XORBranch<ProcessTreeElement> branch: branchList) {
			if(branch.getLabel().equals(pre.getAttribute().name())) {
				return idx; 
			}
			idx++;
		}
		return -1;
	}


	// after this we need to get correlation of this data
	// the result is not well, so we choose to use the Id
	public void trainInDecisionTree(Instances data){
		
		// String[] options = {"-C", "0.25", "-M", "2" };
		String [] options = {"-U", "-O"};
		J48 cls = new J48();
		
		try {
			cls.setOptions(options);
			
			cls.buildClassifier(data);
			Evaluation eval = new Evaluation(data);
			eval.evaluateModel(cls, data);
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			
			// display classifier
			final javax.swing.JFrame jf = 
				       new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
		     jf.setSize(500,400);
		     jf.getContentPane().setLayout(new BorderLayout());
		     TreeVisualizer tv = new TreeVisualizer(null, cls.graph(),
		         new PlaceNode2());
		     jf.getContentPane().add(tv, BorderLayout.CENTER);
		     jf.addWindowListener(new java.awt.event.WindowAdapter() {
		       public void windowClosing(java.awt.event.WindowEvent e) {
		         jf.dispose();
		       }
		     });

		     jf.setVisible(true);
		     tv.fitToScreen();
		     
		} catch (Exception e) {
			// not know the type of this exception
			e.printStackTrace();
	    }
		
	}

	
	
	
	
	
	// we want to get the tree structure and output the correlation rules..
	// but what we should do then?? for this decision tree, it gives unordered data, 
	// but long-term dependency, we need ordered data
	// let us try and see the result
	
}
