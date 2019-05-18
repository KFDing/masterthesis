package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Components;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class CutFinderIMSequenceStrict {

	public static List<Set<XEventClass>> merge(Dfg dfg, List<Set<XEventClass>> cut) {

		if (cut.size() == 2) {
			return cut;
		}

		/*
		 * Assumption: there are no empty traces. (and if there are, they will
		 * be ignored).
		 */

		Components<XEventClass> components = new Components<>(cut);

		//make a mapping node -> subCut
		//initialise counting of taus
		TObjectIntMap<Set<XEventClass>> component2index = new TObjectIntHashMap<>();
		TIntObjectMap<Set<XEventClass>> index2component = new TIntObjectHashMap<>();
		{
			for (int index = 0; index < cut.size(); index++) {
				component2index.put(cut.get(index), index);
				index2component.put(index, cut.get(index));
			}
		}

		//establish the minimum/maximum component from which there is an edge to/from this component.
		int[] edgeMinFrom = new int[cut.size()];
		int[] edgeMaxTo = new int[cut.size()];
		boolean[] hasSkippingEdges = new boolean[cut.size()];
		{
			Arrays.fill(edgeMinFrom, Integer.MAX_VALUE);
			Arrays.fill(edgeMaxTo, Integer.MIN_VALUE);

			for (XEventClass activity : dfg.getStartActivities()) {
				edgeMinFrom[components.getComponentOf(activity)] = -1;
				for (int i = 0; i < components.getComponentOf(activity); i++) {
					hasSkippingEdges[i] = true;
				}
			}

			for (XEventClass activity : dfg.getEndActivities()) {
				edgeMaxTo[components.getComponentOf(activity)] = Integer.MAX_VALUE;
				for (int i = components.getComponentOf(activity) + 1; i < cut.size(); i++) {
					hasSkippingEdges[i] = true;
				}
			}

			for (long edge : dfg.getDirectlyFollowsEdges()) {
				int source = components.getComponentOf(dfg.getDirectlyFollowsEdgeSource(edge));
				int target = components.getComponentOf(dfg.getDirectlyFollowsEdgeTarget(edge));

				edgeMinFrom[target] = Math.min(edgeMinFrom[target], source);
				edgeMaxTo[source] = Math.max(edgeMaxTo[source], target);

				for (int i = source + 1; i < target; i++) {
					hasSkippingEdges[i] = true;
				}
			}
		}

		//find the inversion point in the min-array
		int inversionStart = -1;
		for (int i = 1; i < edgeMaxTo.length; i++) {
			if (edgeMaxTo[i - 1] > edgeMaxTo[i]) {
				inversionStart = i;
				break;
			}
		}

		//find the inversion point in the max-array
		int inversionEnd = edgeMinFrom.length;
		for (int i = edgeMinFrom.length - 1; i > 0; i--) {
			if (edgeMinFrom[i - 1] > edgeMinFrom[i]) {
				inversionEnd = i;
				break;
			}
		}

		if (inversionStart == -1 && inversionEnd == edgeMinFrom.length) {
			return components.getComponents();
		}

		//		System.out.println(Arrays.toString(edgeMinFrom));
		//		System.out.println(Arrays.toString(edgeMaxTo));

		//look for pivots
		for (int i = 0; i < cut.size(); i++) {

			//backward pivot
			if (i >= 1 && hasSkippingEdges[i] && edgeMaxTo[i - 1] == i) {
				//				System.out.println("backward pivot found " + i);
				//walk backward to find dependent nodes
				int j = i - 1;
				while (j >= 0 && edgeMaxTo[j] <= i) {
					//					System.out.println(" depending node " + j);
					j--;
				}
				for (int k = j + 1; k < i; k++) {
					components.mergeComponents(k, k + 1);
				}
			}

			//forward pivot
			if (i < cut.size() - 1 && hasSkippingEdges[i] && edgeMinFrom[i + 1] == i) {
				//				System.out.println("forward pivot found " + i);
				//walk forward to find dependent nodes
				int j = i + 1;
				while (j < cut.size() && edgeMinFrom[j] >= i) {
					//					System.out.println(" depending node " + j);
					j++;
				}
				for (int k = i; k < j - 1; k++) {
					components.mergeComponents(k, k + 1);
				}
			}
		}

		//		System.out.println(components.getComponents());
		//		System.out.println("");

		return components.getComponents();
	}
}
