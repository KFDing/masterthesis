package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.processtree.ProcessTree;

public class ProcessTree2EfficientTreePlugin {
	@Plugin(name = "Convert process tree to efficient tree", returnLabels = { "Efficient Tree" }, returnTypes = {
			EfficientTree.class }, parameterLabels = { "Process tree" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public EfficientTree measure1automata(UIPluginContext context, ProcessTree tree) throws Exception {
		return ProcessTree2EfficientTree.convert(tree);
	}

	@Plugin(name = "Convert efficient tree to process tree", returnLabels = { "Process Tree" }, returnTypes = {
			ProcessTree.class }, parameterLabels = { "Efficient tree" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree convertBack(UIPluginContext context, EfficientTree tree) throws Exception {
		return EfficientTree2processTree.convert(tree);
	}
}
