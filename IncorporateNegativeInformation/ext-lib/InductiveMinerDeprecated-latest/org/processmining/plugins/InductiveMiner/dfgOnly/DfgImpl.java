package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.IntegerMultiSet;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.inductiveminer2.helperclasses.MultiIntSet;

import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

public class DfgImpl implements Dfg {
	private Graph<XEventClass> directlyFollowsGraph;
	private Graph<XEventClass> concurrencyGraph;

	/**
	 * Keep track of start and end activities. The keys are the activities (the
	 * directly follows graph keeps the mapping start activities-keys), the
	 * value is the cardinality. Assumption: values are never 0.
	 */
	private final TIntLongMap startActivities;
	private final TIntLongMap endActivities;

	private long numberOfEmptyTraces;

	public DfgImpl() {
		this(1);
	}

	public DfgImpl(int initialSize) {
		directlyFollowsGraph = GraphFactory.create(XEventClass.class, initialSize);
		concurrencyGraph = GraphFactory.create(XEventClass.class, initialSize);

		startActivities = IntegerMultiSet.createEmpty();
		endActivities = IntegerMultiSet.createEmpty();

		numberOfEmptyTraces = 0;
	}

	public static DfgImpl createTimeOptimised(int initialSize) {
		Graph<XEventClass> d = GraphFactory.createTimeOptimised(XEventClass.class, initialSize);
		Graph<XEventClass> c = GraphFactory.createTimeOptimised(XEventClass.class, initialSize);
		TIntLongMap s = IntegerMultiSet.createEmpty();
		TIntLongMap e = IntegerMultiSet.createEmpty();
		return new DfgImpl(d, c, s, e, 0);
	}

	private DfgImpl(final Graph<XEventClass> directlyFollowsGraph, final Graph<XEventClass> concurrencyGraph,
			final TIntLongMap startActivitiesInt, final TIntLongMap endActivitiesInt, long numberOfEmptyTraces) {
		this.directlyFollowsGraph = directlyFollowsGraph;
		this.concurrencyGraph = concurrencyGraph;

		this.startActivities = startActivitiesInt;
		this.endActivities = endActivitiesInt;

		this.numberOfEmptyTraces = numberOfEmptyTraces;
	}

	@Override
	public int addActivity(XEventClass activity) {
		int index = directlyFollowsGraph.addVertex(activity);
		concurrencyGraph.addVertex(activity);
		return index;
	}

	@Override
	public Graph<XEventClass> getDirectlyFollowsGraph() {
		return directlyFollowsGraph;
	}

	@Override
	public XEventClass[] getActivities() {
		return directlyFollowsGraph.getVertices();
	}

	@Override
	public int[] getActivityIndices() {
		return directlyFollowsGraph.getVertexIndices();
	}

	@Override
	public Graph<XEventClass> getConcurrencyGraph() {
		return concurrencyGraph;
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
	public void addDirectlyFollowsEdge(final XEventClass source, final XEventClass target, final long cardinality) {
		addActivity(source);
		addActivity(target);
		directlyFollowsGraph.addEdge(source, target, cardinality);
	}

	@Override
	public void addParallelEdge(final XEventClass a, final XEventClass b, final long cardinality) {
		addActivity(a);
		addActivity(b);
		concurrencyGraph.addEdge(a, b, cardinality);
	}

	@Override
	public void addStartActivity(XEventClass activity, long cardinality) {
		int activityIndex = addActivity(activity);
		addStartActivity(activityIndex, cardinality);
	}

	@Override
	public void addEndActivity(XEventClass activity, long cardinality) {
		int activityIndex = addActivity(activity);
		addEndActivity(activityIndex, cardinality);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int activity : getActivityIndices()) {
			result.append(activity + ": " + getActivityOfIndex(activity) + ", ");
		}

		result.append("\n");
		for (long edgeIndex : directlyFollowsGraph.getEdges()) {
			result.append(directlyFollowsGraph.getEdgeSource(edgeIndex));
			result.append("->");
			result.append(directlyFollowsGraph.getEdgeTarget(edgeIndex));
			result.append(", ");
		}
		return result.toString();
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

	@Override
	public Dfg clone() {
		return new DfgImpl(directlyFollowsGraph.clone(), concurrencyGraph.clone(), new TIntLongHashMap(startActivities),
				new TIntLongHashMap(endActivities), numberOfEmptyTraces);
	}

	@Override
	public int getNumberOfActivities() {
		return directlyFollowsGraph.getNumberOfVertices();
	}

	@Override
	public int getIndexOfActivity(XEventClass activity) {
		return directlyFollowsGraph.getIndexOfVertex(activity);
	}

	@Override
	public XEventClass getActivityOfIndex(int activityIndex) {
		return directlyFollowsGraph.getVertexOfIndex(activityIndex);
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
		return startActivities.size();
	}

	@Override
	public int getNumberOfEndActivitiesAsSet() {
		return endActivities.size();
	}

	@Override
	public boolean isStartActivity(int activityIndex) {
		return startActivities.containsKey(activityIndex);
	}

	@Override
	public boolean isStartActivity(XEventClass activity) {
		return isStartActivity(getIndexOfActivity(activity));
	}

	@Override
	public long getStartActivityCardinality(int activityIndex) {
		return startActivities.get(activityIndex);
	}

	@Override
	public long getStartActivityCardinality(XEventClass activity) {
		return getStartActivityCardinality(getIndexOfActivity(activity));
	}

	@Override
	public long getEndActivityCardinality(int activityIndex) {
		return endActivities.get(activityIndex);
	}

	@Override
	public long getEndActivityCardinality(XEventClass activity) {
		return getEndActivityCardinality(getIndexOfActivity(activity));
	}

	@Override
	public boolean isEndActivity(int activityIndex) {
		return endActivities.containsKey(activityIndex);
	}

	@Override
	public boolean isEndActivity(XEventClass activity) {
		return isEndActivity(getIndexOfActivity(activity));
	}

	@Override
	public long getMostOccurringStartActivityCardinality() {
		long max = 0;
		for (long value : startActivities.values()) {
			max = Math.max(max, value);
		}
		return max;
	}

	@Override
	public long getMostOccurringEndActivityCardinality() {
		long max = 0;
		for (long value : endActivities.values()) {
			max = Math.max(max, value);
		}
		return max;
	}

	// ========= directly follows graph ==========

	@Override
	public java.lang.Iterable<Long> getDirectlyFollowsEdges() {
		return directlyFollowsGraph.getEdges();
	};

	public boolean containsDirectlyFollowsEdge(int sourceIndex, int targetIndex) {
		return directlyFollowsGraph.containsEdge(sourceIndex, targetIndex);
	}

	public boolean containsDirectlyFollowsEdge(XEventClass source, XEventClass target) {
		return directlyFollowsGraph.containsEdge(source, target);
	}

	public int getDirectlyFollowsEdgeSourceIndex(long edgeIndex) {
		return directlyFollowsGraph.getEdgeSourceIndex(edgeIndex);
	}

	public int getDirectlyFollowsEdgeTargetIndex(long edgeIndex) {
		return directlyFollowsGraph.getEdgeTargetIndex(edgeIndex);
	}

	public XEventClass getDirectlyFollowsEdgeSource(long edgeIndex) {
		return directlyFollowsGraph.getEdgeSource(edgeIndex);
	}

	public XEventClass getDirectlyFollowsEdgeTarget(long edgeIndex) {
		return directlyFollowsGraph.getEdgeTarget(edgeIndex);
	}

	public long getDirectlyFollowsEdgeCardinality(long edgeIndex) {
		return directlyFollowsGraph.getEdgeWeight(edgeIndex);
	}

	public long getMostOccuringDirectlyFollowsEdgeCardinality() {
		return directlyFollowsGraph.getWeightOfHeaviestEdge();
	}

	// ========= concurrency graph ==========

	public java.lang.Iterable<Long> getConcurrencyEdges() {
		return concurrencyGraph.getEdges();
	}

	public boolean containsConcurrencyEdge(int sourceIndex, int targetIndex) {
		return concurrencyGraph.containsEdge(sourceIndex, targetIndex);
	}

	public boolean containsConcurrencyEdge(XEventClass source, XEventClass target) {
		return concurrencyGraph.containsEdge(source, target);
	}

	public int getConcurrencyEdgeSourceIndex(long edgeIndex) {
		return concurrencyGraph.getEdgeSourceIndex(edgeIndex);
	}

	public int getConcurrencyEdgeTargetIndex(long edgeIndex) {
		return concurrencyGraph.getEdgeTargetIndex(edgeIndex);
	}

	public XEventClass getConcurrencyEdgeSource(long edgeIndex) {
		return concurrencyGraph.getEdgeSource(edgeIndex);
	}

	public XEventClass getConcurrencyEdgeTarget(long edgeIndex) {
		return concurrencyGraph.getEdgeTarget(edgeIndex);
	}

	public long getConcurrencyEdgeCardinality(long edgeIndex) {
		return concurrencyGraph.getEdgeWeight(edgeIndex);
	}

	public long getMostOccuringConcurrencyEdgeCardinality() {
		return concurrencyGraph.getWeightOfHeaviestEdge();
	}

	// ========= start activities ==========

	@Override
	public void addStartActivities(MultiSet<XEventClass> startActivities) {
		for (XEventClass e : startActivities) {
			addStartActivity(e, startActivities.getCardinalityOf(e));
		}
	}

	@Override
	public void addStartActivities(Dfg dfg) {
		for (XEventClass e : dfg.getStartActivities()) {
			addStartActivity(e, dfg.getStartActivityCardinality(e));
		}
	}

	@Override
	public void removeStartActivity(int activityIndex) {
		startActivities.remove(activityIndex);
	}

	@Override
	public void removeStartActivity(XEventClass activity) {
		removeStartActivity(getIndexOfActivity(activity));
	}

	@Override
	public Iterable<XEventClass> getStartActivities() {
		return new Iterable<XEventClass>() {
			public Iterator<XEventClass> iterator() {
				return new Iterator<XEventClass>() {
					TIntLongIterator it = startActivities.iterator();

					public XEventClass next() {
						it.advance();
						return getActivityOfIndex(it.key());
					}

					public boolean hasNext() {
						return it.hasNext();
					}

					public void remove() {

					}
				};
			}
		};
	}

	@Override
	public int[] getStartActivityIndices() {
		return startActivities.keys();
	}

	@Override
	public long getNumberOfStartActivities() {
		long count = 0;
		for (long value : startActivities.values()) {
			count += value;
		}
		return count;
	}

	// ========= end activities ==========

	@Override
	public void addEndActivities(MultiSet<XEventClass> endActivities) {
		for (XEventClass e : endActivities) {
			addEndActivity(e, endActivities.getCardinalityOf(e));
		}
	}

	@Override
	public void addEndActivities(Dfg dfg) {
		for (XEventClass e : dfg.getEndActivities()) {
			addEndActivity(e, dfg.getEndActivityCardinality(e));
		}
	}

	@Override
	public void removeEndActivity(int activityIndex) {
		endActivities.remove(activityIndex);
	}

	@Override
	public void removeEndActivity(XEventClass activity) {
		removeEndActivity(getIndexOfActivity(activity));
	}

	@Override
	public Iterable<XEventClass> getEndActivities() {
		return new Iterable<XEventClass>() {
			public Iterator<XEventClass> iterator() {
				return new Iterator<XEventClass>() {
					TIntLongIterator it = endActivities.iterator();

					public XEventClass next() {
						it.advance();
						return getActivityOfIndex(it.key());
					}

					public boolean hasNext() {
						return it.hasNext();
					}

					public void remove() {

					}
				};
			}
		};
	}

	@Override
	public int[] getEndActivityIndices() {
		return endActivities.keys();
	}

	@Override
	public long getNumberOfEndActivities() {
		long count = 0;
		for (long value : endActivities.values()) {
			count += value;
		}
		return count;
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
	public void addStartActivity(int activityIndex, long cardinality) {
		long newValue = startActivities.adjustOrPutValue(activityIndex, cardinality, cardinality);
		if (newValue < 0) {
			startActivities.remove(activityIndex);
		}
	}

	@Override
	public void addEndActivity(int activityIndex, long cardinality) {
		long newValue = endActivities.adjustOrPutValue(activityIndex, cardinality, cardinality);
		if (newValue < 0) {
			endActivities.remove(activityIndex);
		}
	}

	public void addActivity(int index) {
		throw new RuntimeException("Nope. Use the XEventClass variant.");
	}

	public void addEndActivities(MultiIntSet activities) {
		for (int a : activities) {
			addEndActivity(a, activities.getCardinalityOf(a));
		}
	}

	public MultiIntSet getActivityMultiSet() {
		return null;
	}

	public int[] getActivitiesIndices() {
		return null;
	}
}