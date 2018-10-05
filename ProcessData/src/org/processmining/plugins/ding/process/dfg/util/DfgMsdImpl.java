package org.processmining.plugins.ding.process.dfg.util;

import org.processmining.plugins.inductiveminer2.helperclasses.IntDfgImpl;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraphImplQuadratic;

public class DfgMsdImpl extends IntDfgImpl implements DfgMsd {
	private IntGraph minimumSelfDistanceGraph = new IntGraphImplQuadratic();
	private String[] activities;

	public DfgMsdImpl(String[] activities) {
		this.activities = activities;
	}

	public IntGraph getMinimumSelfDistanceGraph() {
		return minimumSelfDistanceGraph;
	}

	public void setMinimumSelfDistanceGraph(IntGraph minimumSelfDistanceGraph) {
		this.minimumSelfDistanceGraph = minimumSelfDistanceGraph;
	}

	@Override
	public void touchActivity(int index) {
		super.touchActivity(index);
		minimumSelfDistanceGraph.addNode(index);
	}

	public String getActivityOfIndex(int value) {
		return activities[value];
	}

	public DfgMsdImpl clone() {
		DfgMsdImpl result = (DfgMsdImpl) super.clone();

		result.minimumSelfDistanceGraph = this.minimumSelfDistanceGraph.clone();
		result.activities = this.activities.clone();

		return result;
	}

}
