package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class EfficientTree2processTreePlugin {
	@Plugin(name = "Convert efficient tree to process tree", returnLabels = { "Process Tree" }, returnTypes = {
			ProcessTree.class }, parameterLabels = {
					"Efficient tree" }, userAccessible = true, help = "Convert an efficient tree into a process tree.", level = PluginLevel.Regular)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(PluginContext context, EfficientTree tree) {
		return EfficientTree2processTree.convert(tree);
	}
}