package org.processmining.plugins.ding.process.dfg.util;

import org.processmining.plugins.inductiveminer2.helperclasses.IntDfg;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;

public interface DfgMsd extends IntDfg, Cloneable {

	public IntGraph getMinimumSelfDistanceGraph();

	public String getActivityOfIndex(int value);

	public DfgMsd clone();
}