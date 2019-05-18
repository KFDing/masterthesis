package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;

public class IMTree {

	public EfficientTree mineTree(PluginContext context, XLog log) {
		return mineTree(log, new MiningParametersIM());
	}
	
	public EfficientTree mineTreeParameters(PluginContext context, XLog log, MiningParameters parameters) {
		return mineTree(log, parameters);
	}
	
	public static EfficientTree mineTree(XLog log, MiningParameters parameters) {
		try {
			return ProcessTree2EfficientTree.convert(IMProcessTree.mineProcessTree(log, parameters));
		} catch (UnknownTreeNodeException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
