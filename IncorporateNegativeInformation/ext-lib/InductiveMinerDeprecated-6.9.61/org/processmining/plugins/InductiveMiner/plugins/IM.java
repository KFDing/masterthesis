package org.processmining.plugins.InductiveMiner.plugins;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

public class IM {

	@Plugin(name = "Mine process tree with Inductive Miner", level = PluginLevel.PeerReviewed, returnLabels = {
			"Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public ProcessTree mineGuiProcessTree(final UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		if (result != InteractionResult.FINISHED || !confirmLargeLogs(context, log, dialog)) {
			context.getFutureResult(0).cancel(false);
			return null;
		}

		context.log("Mining...");

		return IMProcessTree.mineProcessTree(log, dialog.getMiningParameters(), new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
	}

	@Plugin(name = "Mine Petri net with Inductive Miner", level = PluginLevel.PeerReviewed, returnLabels = {
			"Petri net", "initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class,
					Marking.class }, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Mine a Process Tree, dialog", requiredParameterLabels = { 0 })
	public Object[] mineGuiPetrinet(UIPluginContext context, XLog log) {
		IMMiningDialog dialog = new IMMiningDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);
		context.log("Mining...");
		if (result != InteractionResult.FINISHED || !confirmLargeLogs(context, log, dialog)) {
			context.getFutureResult(0).cancel(false);
			context.getFutureResult(1).cancel(false);
			context.getFutureResult(2).cancel(false);
			return new Object[] { null, null, null };
		}
		return IMPetriNet.minePetriNet(context, log, dialog.getMiningParameters());
	}

	@Plugin(name = "Mine Process tree with Inductive Miner, with parameters", returnLabels = {
			"Process tree" }, returnTypes = {
					ProcessTree.class }, parameterLabels = { "Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static ProcessTree mineProcessTree(PluginContext context, XLog log, MiningParameters parameters) {
		context.log("Mining...");
		return IMProcessTree.mineProcessTree(log, parameters);
	}

	@Plugin(name = "Mine Petri net with Inductive Miner, with parameters", returnLabels = { "Petri net",
			"Initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class,
					Marking.class }, parameterLabels = { "Log", "IM Parameters" }, userAccessible = false)
	@PluginVariant(variantLabel = "Mine a Process Tree, parameters", requiredParameterLabels = { 0, 1 })
	public static Object[] minePetriNet(PluginContext context, XLog log, MiningParameters parameters) {
		context.log("Mining...");
		return IMPetriNet.minePetriNet(context, log, parameters);
	}

	public static boolean confirmLargeLogs(final UIPluginContext context, XLog log, IMMiningDialog dialog) {
		if (dialog.getVariant().getWarningThreshold() > 0) {
			XEventClassifier classifier = dialog.getMiningParameters().getClassifier();
			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log, classifier);
			int numberOfActivities = xLogInfo.getEventClasses().size();
			if (numberOfActivities > dialog.getVariant().getWarningThreshold()) {
				int cResult = JOptionPane.showConfirmDialog(null,
						dialog.getVariant().toString() + " might take a long time, as the event log contains "
								+ numberOfActivities
								+ " activities.\nThe chosen variant of Inductive Miner is exponential in the number of activities.\nAre you sure you want to continue?",
						"Inductive Miner might take a while", JOptionPane.YES_NO_OPTION);

				return cResult == JOptionPane.YES_OPTION;
			}
		}
		return true;
	}
}
