package org.processmining.plugins.InductiveMiner;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;

public class TransitiveClosure {
	/*
	 * compute transitive closure of a graph, using Floyd-Warshall algorithm
	 */

	public static <V> Graph<V> transitiveClosure(Class<V> clazz, Graph<V> graph) {
		int countNodes = graph.getNumberOfVertices();
		boolean dist[][] = new boolean[countNodes][countNodes];
		TObjectIntMap<V> node2index = new TObjectIntHashMap<>();
		TIntObjectMap<V> index2node = new TIntObjectHashMap<>();

		//initialise
		{
			int i = 0;
			for (V v : graph.getVertices()) {
				node2index.put(v, i);
				index2node.put(i, v);
				i++;
			}
		}

		{
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					dist[i][j] = false;
				}
			}
		}

		{
			for (long e : graph.getEdges()) {
				int u = node2index.get(graph.getEdgeSource(e));
				int v = node2index.get(graph.getEdgeTarget(e));
				dist[u][v] = true;
			}
		}

		{
			for (int k = 0; k < countNodes; k++) {
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						dist[i][j] = dist[i][j] || (dist[i][k] && dist[k][j]);
					}
				}
			}
		}

		//extract a graph from the distances
		Graph<V> transitiveClosure = GraphFactory.create(clazz, countNodes);
		transitiveClosure.addVertices(index2node.valueCollection());
		for (int i = 0; i < countNodes; i++) {
			for (int j = 0; j < countNodes; j++) {
				if (dist[i][j]) {
					V u = index2node.get(i);
					V v = index2node.get(j);
					transitiveClosure.addEdge(u, v, 1);
				}

			}
		}

		return transitiveClosure;

	}
}
