package org.processmining.plugins.ding.preprocess;

public class Configuration {
	public static final String FIT_LABEL = "fit";

	public static final int FIT_IDX = 1;

	public static final int POS_IDX = 0;

	public static final int NEG_IDX = 1;

	public static String POS_LABEL = "pos_outcome";
	
	public static String TP_TIME = "throughputime";
	public static String TOKEN = "token";
	
	public static int CONFUSION_MATRIX_SIZE = 4;
	public static int ALLOWED_POS_IDX = 0;
	public static int ALLOWED_NEG_IDX = 1;
	public static int NOT_ALLOWED_POS_IDX = 2;
	public static int NOT_ALLOWED_NEG_IDX = 3;
	
	public static int CRITERIA_SIZE = 4;
	public static int RECALL = 0;
	public static int PRECISION = 1;
	public static int ACCURACY = 2;
	public static int F1_SCORE = 3;
	
	
}
