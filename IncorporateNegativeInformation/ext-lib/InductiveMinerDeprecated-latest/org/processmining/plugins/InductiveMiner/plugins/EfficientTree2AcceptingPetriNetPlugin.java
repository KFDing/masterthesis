package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public class EfficientTree2AcceptingPetriNetPlugin {
	@Plugin(name = "Convert efficient tree to accepting Petri net", returnLabels = {
			"Accepting Petri net" }, returnTypes = { AcceptingPetriNet.class }, parameterLabels = {
					"Efficient tree" }, userAccessible = true, help = "Convert an efficient tree into an Accepting Petri net.", level = PluginLevel.Regular)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public AcceptingPetriNet convert(PluginContext context, EfficientTree tree) {
		return EfficientTree2AcceptingPetriNet.convert(tree);
	}
}
