package org.processmining.plugins.inductiveminer2.helperclasses;

import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraphImplQuadratic;

public class IntDfgImpl implements IntDfg {

	private MultiIntSet activities = new MultiIntSet();
	private long numberOfEmptyTraces = 0;
	private IntGraph directlyFollowsGraph = new IntGraphImplQuadratic();
	private IntGraph concurrencyGraph = new IntGraphImplQuadratic();
	private MultiIntSet startActivities = new MultiIntSet();
	private MultiIntSet endActivities = new MultiIntSet();

	@Override
	public MultiIntSet getActivities() {
		return activities;
	}

	@Override
	public void addActivity(int index) {
		activities.add(index);
		touchActivity(index);
	}

	@Override
	public void touchActivity(int index) {
		directlyFollowsGraph.addNode(index);
		concurrencyGraph.addNode(index);
	}

	@Override
	public int getNumberOfActivities() {
		return activities.setSize();
	}

	@Override
	public long getNumberOfEmptyTraces() {
		return numberOfEmptyTraces;
	}

	@Override
	public void setNumberOfEmptyTraces(long numberOfEmptyTraces) {
		this.numberOfEmptyTraces = numberOfEmptyTraces;
	}

	@Override
	public void addEmptyTraces(long cardinality) {
		numberOfEmptyTraces += cardinality;
	}

	@Override
	public boolean hasStartActivities() {
		return !startActivities.isEmpty();
	}

	@Override
	public boolean hasEndActivities() {
		return !endActivities.isEmpty();
	}

	@Override
	public IntDfgImpl clone() {
		IntDfgImpl result;
		try {
			result = (IntDfgImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		result.activities = activities.clone();
		result.numberOfEmptyTraces = numberOfEmptyTraces;
		result.directlyFollowsGraph = directlyFollowsGraph.clone();
		result.concurrencyGraph = concurrencyGraph.clone();
		result.startActivities = startActivities.clone();
		result.endActivities = endActivities.clone();

		return result;
	}

	@Override
	public String toString() {
		return directlyFollowsGraph.toString();
	}

	@Override
	public void collapseParallelIntoDirectly() {
		for (long edgeIndex : concurrencyGraph.getEdges()) {
			directlyFollowsGraph.addEdge(concurrencyGraph.getEdgeSource(edgeIndex),
					concurrencyGraph.getEdgeTarget(edgeIndex), concurrencyGraph.getEdgeWeight(edgeIndex));
			directlyFollowsGraph.addEdge(concurrencyGraph.getEdgeTarget(edgeIndex),
					concurrencyGraph.getEdgeSource(edgeIndex), concurrencyGraph.getEdgeWeight(edgeIndex));
		}
	}

	public IntGraph getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	public IntGraph getConcurrencyGraph() {
		return concurrencyGraph;
	}

	public MultiIntSet getStartActivities() {
		return startActivities;
	}

	public MultiIntSet getEndActivities() {
		return endActivities;
	}

}
