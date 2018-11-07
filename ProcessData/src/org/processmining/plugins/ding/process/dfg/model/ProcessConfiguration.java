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

	public static final int WEIGHT_RANGE = 20;

	public static final int WEIGHT_VALUE = 10;


	public static final int RULESET_IDX_NUM= 3;
	
	public static final int RULESET_EXISTING_IDX = 1;
	public static final int RULESET_POS_IDX = 1;
	public static final int RULESET_NEG_IDX = 2;
	public static final String XOR = "Xor";
	public static final String SEQUENCE = "Seq";
	public static final String PARALLEL = "And";
	public static final String LOOP = "XorLoop";
	public static final String XOR_BRANCH = "Xor Branch";
	public static final String NEW_SEQUENCE = "My_New_Seq";
	public static final String POST_PREFIX = "Place After ";
	public static final String PRE_PREFIX = "Place Before ";
	
	
	public enum ViewType {
		Dfg, ProcessTree, PetriNet
	};

	public enum StructureType {
		Sequence, Parallel, XorLoop
	};
	
	public static Color COLOR_BG2 = new Color(120, 120, 120);
}
