package org.processmining.plugins.ding.process.dfg.train;

import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.ding.preprocess.util.EventLogUtilities;
import org.processmining.plugins.ding.preprocess.util.LabeledTraceVariant;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.XORBranch;
import org.processmining.plugins.ding.process.dfg.transform.DataTransfer;
import org.processmining.plugins.ding.process.dfg.transform.NewXORBranchGenerator;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;

import weka.core.Instances;

/**
 * this class is used to detect the branch correlation, 
 * Input:: 
 *   input is tree, and branchList from the tree
 * Output:; 
 *   correlation which is represented by LT rules
 *   
 * Method: 
 *    we are going to use which method?? Also, we need to make sure the transfer of branch and its item in table
 *    Incorporate both negative and positive method, we can have decision tree as first try,
 *     but better to use inductive logic programming, we need some transfer, reuse codes, please
 * @author dkf
 * @created_date Dec 17, 2018
 */
public class NewBranchLTDetector {
	ProcessTree tree;
	XLog log;
	
	NewXORBranchGenerator<ProcessTreeElement> generator;
	List<XORBranch<ProcessTreeElement>> branchList;
	Map<XORBranch<ProcessTreeElement>, Integer> colIdxMap ;
	Map<List<XEventClass>, Integer> rowIdxMap ;
	
	
	int UNIT_POS_IDX = 0;
	int UNIT_NEG_IDX = 1;
	public NewBranchLTDetector(ProcessTree pTree, XLog xLog) {
		tree =pTree;
		log = xLog;
		
		// we need to get the size from generator 
		
	}
	
	public void buildLT() {
		generator = new NewXORBranchGenerator();
		generator.buildBranches(tree);
		
		branchList = generator.getBranchList();
		
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, null);
		
		/*
		int colSize = branchList.size();
		int rowSize = variants.size();
		int elementSize = 2;
		colIdxMap = new HashMap<XORBranch<ProcessTreeElement>, Integer>();
		rowIdxMap  = new HashMap<List<XEventClass>, Integer>();
		
		// we only think it is only pos, but with neg, what to do then??
		int[][][] connInfo = new int[rowSize +1][colSize + 1][elementSize];
		*/
		
		String classifier = ProcessConfiguration.ASSOCIATIOn_RULE;
		DataTransfer transfer = new DataTransfer(log, tree);
		Instances data = transfer.transferData(classifier, branchList, variants);
		
		CorrelationTrainer trainer = new CorrelationTrainer();
		trainer.trainCorrelation(classifier,data, branchList);
		
		
	}
	
	// initialize the data table, we only use the array list or we use in?? 
	public void initializeConn(List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		int rowIdx = 0, colIdx = 0;
		
		
		for(XORBranch<ProcessTreeElement> branch: branchList) {
			colIdxMap.put(branch, colIdx);
			colIdx ++;
		}
		
		for(LabeledTraceVariant var : variants) {
			// for each var, we create one row into the data info, find out all the relative branch 
			// which shows in variant and then assign the value on it
			List<XEventClass> traceVariant = var.getTraceVariant();
			rowIdxMap.put(traceVariant, rowIdx);
			rowIdx ++;
		}
		
	}
	
	/*
	// we fill connInfo with information from var
	public void fillItem(int[][][] connInfo, List<XORBranch<ProcessTreeElement>> branchList, List<LabeledTraceVariant> variants) {
		int colIdx, rowIdx;
		
		tlmaps = getProcessTree2EventMap(log, tree , null);
		
		for(LabeledTraceVariant var : variants) {
			// we need to compare with all the branchList and find then put it there
			List<XEventClass> traceVariant = var.getTraceVariant();
			
			rowIdx = rowIdxMap.get(traceVariant);
			
			for(XORBranch<ProcessTreeElement> branch: branchList) {
				if(containBranch(traceVariant, branch)) {
					colIdx = colIdxMap.get(branch);
					// it can have positive, but for this one, we only consider not overlapped data
					connInfo[rowIdx][colIdx][UNIT_POS_IDX] = var.getPosNum();
					connInfo[rowIdx][colIdx][UNIT_NEG_IDX] = var.getNegNum();
				}
				
			}
			
		}
	}
	*/
	

}
