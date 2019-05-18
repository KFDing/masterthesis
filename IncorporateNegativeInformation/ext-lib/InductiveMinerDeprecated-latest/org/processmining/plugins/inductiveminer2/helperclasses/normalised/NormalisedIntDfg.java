package org.processmining.plugins.inductiveminer2.helperclasses.normalised;

import org.processmining.plugins.inductiveminer2.helperclasses.MultiIntSet;

/**
 * All indices in this class should be normalised, i.e. in [0..number of
 * activities - 1]
 * 
 * @author sander
 *
 */
public interface NormalisedIntDfg extends Cloneable {

	/**
	 * Add an activity. Notice that the index should be normalised.
	 * 
	 * @param index
	 */
	public void addActivity(int index);

	/**
	 * 
	 * @return The number of activities.
	 */
	public int getNumberOfActivities();

	/**
	 * 
	 * @return The number of empty (epsilon) traces.
	 */
	public long getNumberOfEmptyTraces();

	/**
	 * Set the number of empty (epsilon) traces.
	 * 
	 * @param numberOfEmptyTraces
	 */
	public void setNumberOfEmptyTraces(long numberOfEmptyTraces);

	/**
	 * Adds empty traces.
	 * 
	 * @param cardinality
	 */
	public void addEmptyTraces(long cardinality);

	public void addDirectlyFollowsEdge(final int source, final int target, final long cardinality);

	public void addParallelEdge(final int a, final int b, final long cardinality);

	public void addStartActivity(int activity, long cardinality);

	public void addEndActivity(int activity, long cardinality);
	
	public void addEndActivities(MultiIntSet activities);

	public boolean hasStartActivities();

	public boolean hasEndActivities();

	/**
	 * 
	 * @return The size of the set of start activities.
	 */
	public int getNumberOfStartActivitiesAsSet();

	/**
	 * 
	 * @return The size of the set of end activities.
	 */
	public int getNumberOfEndActivitiesAsSet();

	/**
	 * 
	 * @param activityIndex
	 * @return Whether the activity with the given index is a start activity.
	 */
	public boolean isStartActivity(int activityIndex);

	/**
	 * 
	 * @param activityIndex
	 * @return How often the activity was a start activity.
	 */
	public long getStartActivityCardinality(int activityIndex);

	/**
	 * 
	 * @return The number of occurrences of the activity that occurs the most as
	 *         a start activity.
	 */
	public long getMostOccurringStartActivityCardinality();

	/**
	 * 
	 * @param activityIndex
	 * @return Whether the activity with the given index is a end activity.
	 */
	public boolean isEndActivity(int activityIndex);

	/**
	 * 
	 * @return The number of occurrences of the activity that occurs the most as
	 *         an end activity.
	 */
	public long getMostOccurringEndActivityCardinality();

	/**
	 * 
	 * @param activityIndex
	 * @return How often the activity was an end activity.
	 */
	public long getEndActivityCardinality(int activityIndex);

	/**
	 * Returns an iterable that iterates over all edges; The edges that are
	 * returned are indices. Edges of weight 0 are excluded.
	 * 
	 * @return
	 */
	public Iterable<Long> getDirectlyFollowsEdges();

	public boolean containsDirectlyFollowsEdge(int sourceIndex, int targetIndex);

	public int getDirectlyFollowsEdgeSourceIndex(long edgeIndex);

	public int getDirectlyFollowsEdgeTargetIndex(long edgeIndex);

	public long getDirectlyFollowsEdgeCardinality(long edgeIndex);

	public long getMostOccuringDirectlyFollowsEdgeCardinality();

	/**
	 * Returns an iterable that iterates over all edges; The edges that are
	 * returned are indices. Edges of weight 0 are excluded.
	 * 
	 * @return
	 */
	public Iterable<Long> getConcurrencyEdges();

	public boolean containsConcurrencyEdge(int sourceIndex, int targetIndex);

	public int getConcurrencyEdgeSourceIndex(long edgeIndex);

	public int getConcurrencyEdgeTargetIndex(long edgeIndex);

	public long getConcurrencyEdgeCardinality(long edgeIndex);

	public long getMostOccuringConcurrencyEdgeCardinality();

	// ========= start activities ==========

	/**
	 * Removes the start activity.
	 * 
	 * @param activityIndex
	 */
	public void removeStartActivity(int activityIndex);

	/**
	 * 
	 * @return The indices of the start activities. This array should not be
	 *         edited.
	 */
	public int[] getStartActivityIndices();

	/**
	 * 
	 * @return The number of times that an end activity occurred. Use
	 *         getNumberOfStartActivities() for the set-size.
	 */
	public long getNumberOfStartActivities();

	// ========= end activities ==========

	/**
	 * Removes the end activity.
	 * 
	 * @param activityIndex
	 */
	public void removeEndActivity(int activityIndex);

	/**
	 * 
	 * @return The indices of the start activities. This array should not be
	 *         edited.
	 */
	public int[] getEndActivityIndices();

	/**
	 * 
	 * @return The number of times that an end activity occurred. Use
	 *         getNumberOfEndActivities() for the set-size.
	 */
	public long getNumberOfEndActivities();

	public NormalisedIntGraph getDirectlyFollowsGraph();

	public NormalisedIntGraph getConcurrencyGraph();

	public NormalisedIntDfg clone();

	/**
	 * Adds a directly follows graph edge (in each direction) for each parallel
	 * edge.
	 */
	public void collapseParallelIntoDirectly();
}
