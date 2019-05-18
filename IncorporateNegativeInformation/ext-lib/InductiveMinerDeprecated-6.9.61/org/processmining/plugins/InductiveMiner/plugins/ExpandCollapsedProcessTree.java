package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.conversion.ExpandProcessTree;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class ExpandCollapsedProcessTree {
	@Plugin(name = "Expand collapsed process tree", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Collapsed process tree" }, userAccessible = true, help = "Transform a process tree by replacing each leaf a with seq(xor(tau, a+start), a+complete).")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(PluginContext context, ProcessTree tree) {
		return ExpandProcessTree.expand(tree).getA();
	}
}
