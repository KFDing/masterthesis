package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public class PreProcessLogForLifeCyclesPlugin {
	@Plugin(name = "Pre-process an event log for life cycle mining", returnLabels = { "Log" }, returnTypes = { XLog.class }, parameterLabels = { "Log" }, userAccessible = true, help = "Make all traces consistent by inserting a completion event directly after each unmatched start event.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public XLog preProcessLog(UIPluginContext context, XLog log) {
		return new LifeCycles(true).preProcessLog(log, MiningParameters.getDefaultClassifier(), MiningParameters.getDefaultLifeCycleClassifier());
	}
}
