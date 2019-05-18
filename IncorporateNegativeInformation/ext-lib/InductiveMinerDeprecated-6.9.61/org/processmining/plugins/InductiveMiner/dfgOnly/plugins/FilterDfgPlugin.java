package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public class FilterDfgPlugin {
	@Plugin(name = "Filter directly follows graph", returnLabels = { "Directly follows graph" }, returnTypes = {
			Dfg.class }, parameterLabels = { "Direclty follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public Dfg filter(UIPluginContext context, Dfg dfg) {
		return CutFinderIMf.filterNoise(dfg, 0.2f);
	}
}
