package org.processmining.plugins.ding.process.dfg.model;

import java.awt.Color;

import org.processmining.plugins.ding.preprocess.util.Configuration;

public class ProcessConfiguration extends Configuration{
	
	public static final int MATRIX_KEY_COL_NUM = 2;
	public static final int MATRIX_VALUE_COL_NUM = 3;
	public static final int MATRIX_EXISTING_IDX = 3;
	public static final int MATRIX_POS_IDX = 4;
	public static final int MATRIX_NEG_IDX = 5;

	public static final String START_LABEL = "start";

	public static final String END_LABEL = "end";

	public static final String DEFAULT_WEIGHT = "1.0";

	public static final int WEIGHT_RANGE = 10;

	public static final int WEIGHT_VALUE = 10;


	public static final int LT_IDX_NUM= 3;
	public static final int LT_EXISTING_IDX = 3;
	public static final int LT_POS_IDX = 4;
	public static final int LT_NEG_IDX = 5;
	
	public static final String XOR = "Xor";
	public static final String SEQUENCE = "Seq";
	public static final String PARALLEL = "And";
	public static final String LOOP = "XorLoop";
	public static final String XOR_BRANCH = "Xor Branch";
	public static final String NEW_SEQUENCE = "My_New_Seq";
	public static final String PLACE_POST_PREFIX = "PlaceAfter";
	public static final String PLACE_PRE_PREFIX = "PlaceBefore";
	public static final String TRANSITION_POST_PREFIX = "TransitionAfter";
	public static final String TRANSITION_PRE_PREFIX = "TransitionBefore";
	public static final String POST_PREFIX = "After";
	public static final String PRE_PREFIX = "Before";
	
	// the threshold to keep the dfg working
	public static double DFG_THRESHOLD = 0.5;
	public static double LT_THRESHOLD = 0.25;
	
	public enum ViewType {
		ProcessTree, PetriNet, PetriNetWithLTDependency
	};

	public enum StructureType {
		Sequence, Parallel, XorLoop
	};
	
	public static Color COLOR_BG2 = new Color(120, 120, 120);
	public static String DECISION_TREE = "Decision Tree";
	public static String ASSOCIATIOn_RULE = "Association Rule";
	public static String ILP = "ILP";
	
	
	public enum ActionType{AddLTOnPair, RemoveLTOnPair};
	
	public static String[] SaveModelType = {"Petrinet With LT", "Petrinet Without LT", "Process Tree"};
	
}
