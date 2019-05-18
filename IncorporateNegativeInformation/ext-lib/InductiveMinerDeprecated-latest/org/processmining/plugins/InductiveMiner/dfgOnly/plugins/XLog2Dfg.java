package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.XLog2DfgDialog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

public class XLog2Dfg {

	@Plugin(name = "Convert log to directly follows graph", returnLabels = { "Directly follows graph" }, returnTypes = {
			Dfg.class }, parameterLabels = {
					"Log" }, userAccessible = true, help = "Convert a log into a directly follows graph.")
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Dfg log2Dfg(UIPluginContext context, XLog log) {
		context.getFutureResult(0)
				.setLabel("Directly follows graph of " + XConceptExtension.instance().extractName(log));
		XLog2DfgDialog dialog = new XLog2DfgDialog(log);
		InteractionResult result = context.showWizard("Convert log to directly follows graph", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}

		XLifeCycleClassifier lifeCycleClassifier = dialog.getIMLog2IMLogInfo().useLifeCycle()
				? new LifeCycleClassifier() : MiningParameters.getDefaultLifeCycleClassifier();
		return dialog.getIMLog2IMLogInfo()
				.createLogInfo(new IMLogImpl(log, dialog.getClassifier(), lifeCycleClassifier)).getDfg();
	}

}
