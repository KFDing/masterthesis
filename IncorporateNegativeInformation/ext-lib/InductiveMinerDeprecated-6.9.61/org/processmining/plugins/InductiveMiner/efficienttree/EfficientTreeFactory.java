package org.processmining.plugins.InductiveMiner.efficienttree;

import gnu.trove.map.TObjectIntMap;

public class EfficientTreeFactory {
	public static EfficientTree create(int[] tree, TObjectIntMap<String> activity2int, String[] int2activity) {
		return new EfficientTreeImpl(tree, activity2int, int2activity);
	}
}
