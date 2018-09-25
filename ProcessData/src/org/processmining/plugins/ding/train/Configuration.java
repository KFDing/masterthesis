package org.processmining.plugins.ding.train;

import java.awt.Font;

public class Configuration {
	public static final String FIT_LABEL = "fit";

	public static final int FIT_IDX = 1;

	public static final int POS_IDX = 0;
	public static final int NEG_IDX = 1;
	public static final int UNKNOWN_IDX = 2;

	public static final int TRACE_NUMIDX = 0;

	public static final int EVENT_NUMIDX = 0;

	public static final int VARIANT_NUMIDX = 0;

	public static final int DEFAULT_FIXED_WIDTH_TRACE_COUNT = 5;

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
	
	public static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 10);
	// I would like to try the enumerate set, but not now
	// public static enum FIT_CHOICES {FALSE,TRUE,UNKNOWN};
	public static final String[] FIT_CHOICES = {"UNKNOWN", "FALSE","TRUE"};
	public static final int FIT_CHOICES_SIZE = 3;
	public static final String FIT_UNKNOWN = "UNKNOWN";
	public static final String FIT_TRUE = "TRUE";
	public static final String FIT_FALSE = "FALSE";
	
}
