package org.processmining.plugins.ding.process.dfg.train;

import java.awt.BorderLayout;
import java.util.List;

import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.processtree.ProcessTreeElement;

import weka.associations.AbstractAssociator;
import weka.associations.Apriori;
import weka.associations.AssociationRule;
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
	
	public void trainCorrelation(String classifer, Instances data, List<XORBranch<ProcessTreeElement>> branchList) {
		if(classifer.equals(ProcessConfiguration.DECISION_TREE)) {
			trainInDecisionTree(data);
		}else if(classifer.equals(ProcessConfiguration.ASSOCIATIOn_RULE)) {
			trainInAssociationRule(data, branchList);
		}else if(classifer.equals(ProcessConfiguration.ILP)) {
			trainInILP(data);
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
	private AbstractAssociator trainInAssociationRule(Instances data, List<XORBranch<ProcessTreeElement>> branchList) {
		// TODO get the correlation from accociation rule
		// find the association rule by positive parts and then check the relation of it
		/*
		Apriori apriori = new Apriori();
        apriori.setClassIndex(data.classIndex());
        apriori.buildAssociations(data);
        */
		// first without class
        Apriori model = new Apriori();
    	try {
			model.buildAssociations(data);
			System.out.println(model);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	List<AssociationRule> ruleList = model.getAssociationRules().getRules();
    	int i=0;
    	while(i< ruleList.size()) {
    		AssociationRule rule = ruleList.get(i);
    		
    		// we keep rule if the premise and consequence are not in the same xor structure
    		// we only use the positive rules , if the data is given from only the yes result, others we don't have it
    		
    		i++;
    	}
    	/*
    	FPGrowth fpgModel = new FPGrowth();
    	fpgModel.buildAssociations(data);
    	*/
    	return model;
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
