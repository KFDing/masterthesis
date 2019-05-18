package org.processmining.plugins.InductiveMiner.plugins;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;

public class ReduceAcceptingPetriNetPlugin {
	@Plugin(name = "Reduce Accpeting Petri net language-equivalently (in-place)", returnLabels = {
			"Accepting Petri net" }, returnTypes = { AcceptingPetriNet.class }, parameterLabels = {
					"Accepting Petri net" }, userAccessible = true, help = "Reduce an Accepting Petri net but keep the language the same.", level = PluginLevel.Regular)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public AcceptingPetriNet mineGuiProcessTree(final PluginContext context, AcceptingPetriNet net) {
		ReduceAcceptingPetriNetKeepLanguage.reduce(net, new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
		context.getFutureResult(0).cancel(false);
		return null;
	}
}
