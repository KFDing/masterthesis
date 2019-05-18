package org.processmining.plugins.InductiveMiner;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class IntegerMultiSet {
	public static TIntLongMap createEmpty() {
		return new TIntLongHashMap(10, 0.5f, -1, 0);
	}

	public static <V> TIntLongMap create(MultiSet<V> multiSet, Graph<V> graph) {
		TIntLongMap result = createEmpty();
		for (V v : multiSet) {
			int index = graph.getIndexOfVertex(v);
			result.put(index, multiSet.getCardinalityOf(v));
		}
		return result;
	}
}
