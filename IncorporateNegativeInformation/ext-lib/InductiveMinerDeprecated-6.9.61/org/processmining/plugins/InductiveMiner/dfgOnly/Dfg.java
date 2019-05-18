package org.processmining.plugins.InductiveMiner.dfgOnly;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.inductiveminer2.helperclasses.normalised.NormalisedIntDfg;

public interface Dfg extends NormalisedIntDfg {

	/**
	 * Adds an activity to the Dfg.
	 * 
	 * @param activity
	 * @return The index of the inserted activity.
	 */
	public int addActivity(XEventClass activity);

	@Deprecated
	public Graph<XEventClass> getDirectlyFollowsGraph();

	public int[] getActivityIndices();

	/**
	 * 
	 * @return The concurrency graph. Do not edit directly.
	 */
	@Deprecated
	public Graph<XEventClass> getConcurrencyGraph();

	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality);

	public void addParallelEdge(final XEventClass a, final XEventClass b, final long cardinality);

	public void addStartActivity(XEventClass activity, long cardinality);

	public void addEndActivity(XEventClass activity, long cardinality);

	/**
	 * Adds a directly follows graph edge (in each direction) for each parallel
	 * edge.
	 */
	public void collapseParallelIntoDirectly();

	/**
	 * 
	 * @return An unconnected copy of the Dfg.
	 */
	public Dfg clone();

	/**
	 * 
	 * @return The number of activities (as if they were a set).
	 */
	public int getNumberOfActivities();

	/**
	 * 
	 * @param activity
	 * @return The index of the given activity, or -1 if it does not exist.
	 */
	public int getIndexOfActivity(XEventClass activity);

	/**
	 * 
	 * @param activityIndex
	 * @return The activity of the given index.
	 */
	public XEventClass getActivityOfIndex(int activityIndex);

	/**
	 * 
	 * @param activity
	 * @return Whether the activity is a start activity. If possible, use the
	 *         integer-variant.
	 */
	public boolean isStartActivity(XEventClass activity);

	/**
	 * 
	 * @param activity
	 * @return How often the activity was a start activity. Use the integer
	 *         variant if possible.
	 */
	public long getStartActivityCardinality(XEventClass activity);

	/**
	 * 
	 * @param activity
	 * @return Whether the activity is a end activity. If possible, use the
	 *         integer-variant.
	 */
	public boolean isEndActivity(XEventClass activity);

	/**
	 * 
	 * @param activity
	 * @return How often the activity was an end activity. Use the integer
	 *         variant if possible.
	 */
	public long getEndActivityCardinality(XEventClass activity);

	// ========= activities ==========

	/**
	 * 
	 * @return An array of the activities. Do not edit this array.
	 */
	public XEventClass[] getActivities();

	// ========= directly follows graph ==========

	public boolean containsDirectlyFollowsEdge(XEventClass source, XEventClass target);

	public XEventClass getDirectlyFollowsEdgeSource(long edgeIndex);

	public XEventClass getDirectlyFollowsEdgeTarget(long edgeIndex);

	// ========= concurrency graph ==========

	public boolean containsConcurrencyEdge(XEventClass source, XEventClass target);

	public XEventClass getConcurrencyEdgeSource(long edgeIndex);

	public XEventClass getConcurrencyEdgeTarget(long edgeIndex);

	// ========= start activities ==========

	/**
	 * Add the start activities in the multiset to the start activities.
	 * 
	 * @param startActivities
	 */
	public void addStartActivities(MultiSet<XEventClass> startActivities);

	/**
	 * Add the start activities in the dfg to the start activities.
	 * 
	 * @param dfg
	 */
	public void addStartActivities(Dfg dfg);

	/**
	 * Removes the start activity. Use the integer variant if possible.
	 * 
	 * @param activity
	 */
	public void removeStartActivity(XEventClass activity);

	/**
	 * Return an iterable over the start activities. Use the integer variant if
	 * possible.
	 * 
	 * @return
	 */
	public Iterable<XEventClass> getStartActivities();

	// ========= end activities ==========

	/**
	 * Add the end activities in the multiset to the end activities.
	 * 
	 * @param endActivities
	 */
	public void addEndActivities(MultiSet<XEventClass> endActivities);

	/**
	 * Add the end activities in the dfg to the end activities.
	 * 
	 * @param dfg
	 */
	public void addEndActivities(Dfg dfg);

	/**
	 * Removes the end activity. Use the integer variant if possible.
	 * 
	 * @param activity
	 */
	public void removeEndActivity(XEventClass activity);

	/**
	 * Return an iterable over the start activities. Use the integer variant if
	 * possible.
	 * 
	 * @return
	 */
	public Iterable<XEventClass> getEndActivities();

}
