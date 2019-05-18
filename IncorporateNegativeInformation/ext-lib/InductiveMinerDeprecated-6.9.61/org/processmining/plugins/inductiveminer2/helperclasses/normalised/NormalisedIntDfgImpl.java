package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import org.processmining.plugins.inductiveminer2.helperclasses.MultiIntSet;

public class NormalisedIntDfgImpl implements NormalisedIntDfg {

	private int numberOfActivities = 0;
	private long numberOfEmptyTraces = 0;
	private NormalisedIntGraph directlyFollowsGraph = new NormalisedIntGraphImplQuadratic();
	private NormalisedIntGraph concurrencyGraph = new NormalisedIntGraphImplQuadratic();
	private MultiIntSet startActivities = new MultiIntSet();
	private MultiIntSet endActivities = new MultiIntSet();

	@Override
	public void addActivity(int index) {
		numberOfActivities = Math.max(numberOfActivities, index + 1);
	}

	@Override
	public int getNumberOfActivities() {
		return numberOfActivities;
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
	public void addDirectlyFollowsEdge(int source, int target, long cardinality) {
		directlyFollowsGraph.addEdge(source, target, cardinality);
	}

	@Override
	public void addParallelEdge(int a, int b, long cardinality) {
		concurrencyGraph.addEdge(a, b, cardinality);
	}

	@Override
	public void addStartActivity(int activity, long cardinality) {
		startActivities.add(activity, cardinality);
	}

	@Override
	public void addEndActivity(int activity, long cardinality) {
		endActivities.add(activity, cardinality);
	}

	@Override
	public void addEndActivities(MultiIntSet endActivities) {
		endActivities.addAll(endActivities);
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
	public int getNumberOfStartActivitiesAsSet() {
		return startActivities.setSize();
	}

	@Override
	public int getNumberOfEndActivitiesAsSet() {
		return endActivities.setSize();
	}

	@Override
	public boolean isStartActivity(int activityIndex) {
		return startActivities.contains(activityIndex);
	}

	@Override
	public long getStartActivityCardinality(int activityIndex) {
		return startActivities.getCardinalityOf(activityIndex);
	}

	@Override
	public long getMostOccurringStartActivityCardinality() {
		return startActivities.getCardinalityOf(startActivities.getElementWithHighestCardinality());
	}

	@Override
	public boolean isEndActivity(int activityIndex) {
		return endActivities.contains(activityIndex);
	}

	@Override
	public long getMostOccurringEndActivityCardinality() {
		return endActivities.getCardinalityOf(endActivities.getElementWithHighestCardinality());
	}

	@Override
	public long getEndActivityCardinality(int activityIndex) {
		return endActivities.getCardinalityOf(activityIndex);
	}

	@Override
	public Iterable<Long> getDirectlyFollowsEdges() {
		return directlyFollowsGraph.getEdges();
	}

	@Override
	public boolean containsDirectlyFollowsEdge(int sourceIndex, int targetIndex) {
		return directlyFollowsGraph.containsEdge(sourceIndex, targetIndex);
	}

	@Override
	public int getDirectlyFollowsEdgeSourceIndex(long edgeIndex) {
		return directlyFollowsGraph.getEdgeSourceIndex(edgeIndex);
	}

	@Override
	public int getDirectlyFollowsEdgeTargetIndex(long edgeIndex) {
		return directlyFollowsGraph.getEdgeTargetIndex(edgeIndex);
	}

	@Override
	public long getDirectlyFollowsEdgeCardinality(long edgeIndex) {
		return directlyFollowsGraph.getEdgeWeight(edgeIndex);
	}

	@Override
	public long getMostOccuringDirectlyFollowsEdgeCardinality() {
		return directlyFollowsGraph.getWeightOfHeaviestEdge();
	}

	@Override
	public Iterable<Long> getConcurrencyEdges() {
		return concurrencyGraph.getEdges();
	}

	@Override
	public boolean containsConcurrencyEdge(int sourceIndex, int targetIndex) {
		return concurrencyGraph.containsEdge(sourceIndex, targetIndex);
	}

	@Override
	public int getConcurrencyEdgeSourceIndex(long edgeIndex) {
		return concurrencyGraph.getEdgeSourceIndex(edgeIndex);
	}

	@Override
	public int getConcurrencyEdgeTargetIndex(long edgeIndex) {
		return concurrencyGraph.getEdgeTargetIndex(edgeIndex);
	}

	@Override
	public long getConcurrencyEdgeCardinality(long edgeIndex) {
		return concurrencyGraph.getEdgeWeight(edgeIndex);
	}

	@Override
	public long getMostOccuringConcurrencyEdgeCardinality() {
		return concurrencyGraph.getWeightOfHeaviestEdge();
	}

	@Override
	public void removeStartActivity(int activityIndex) {
		startActivities.remove(activityIndex);
	}

	@Override
	public int[] getStartActivityIndices() {
		return startActivities.toSet().toArray();
	}

	@Override
	public long getNumberOfStartActivities() {
		return startActivities.size();
	}

	@Override
	public void removeEndActivity(int activityIndex) {
		endActivities.remove(activityIndex);
	}

	@Override
	public int[] getEndActivityIndices() {
		return endActivities.toSet().toArray();
	}

	@Override
	public long getNumberOfEndActivities() {
		return endActivities.size();
	}

	@Override
	public NormalisedIntGraph getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	@Override
	public NormalisedIntGraph getConcurrencyGraph() {
		return concurrencyGraph;
	}

	@Override
	public NormalisedIntDfgImpl clone() {
		NormalisedIntDfgImpl result;
		try {
			result = (NormalisedIntDfgImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		result.numberOfActivities = numberOfActivities;
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
			directlyFollowsGraph.addEdge(concurrencyGraph.getEdgeSourceIndex(edgeIndex),
					concurrencyGraph.getEdgeTargetIndex(edgeIndex), concurrencyGraph.getEdgeWeight(edgeIndex));
			directlyFollowsGraph.addEdge(concurrencyGraph.getEdgeTargetIndex(edgeIndex),
					concurrencyGraph.getEdgeSourceIndex(edgeIndex), concurrencyGraph.getEdgeWeight(edgeIndex));
		}
	}

}
