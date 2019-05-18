package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.jbpt.petri.Marking;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.IMdMiningDialog;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class IMd {
	@Plugin(name = "Mine process tree with Inductive Miner - directly follows", returnLabels = {
			"Process Tree" }, returnTypes = {
					ProcessTree.class }, parameterLabels = { "Direclty-follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public ProcessTree mineProcessTree(UIPluginContext context, Dfg dfg) {
		IMdMiningDialog dialog = new IMdMiningDialog();
		InteractionResult result = context.showWizard("Mine using Inductive Miner - directly follows", true, true,
				dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return IMdProcessTree.mineProcessTree(dfg, dialog.getMiningParameters());
	}

	@Plugin(name = "Mine Petri net with Inductive Miner - directly follows", returnLabels = { "Petri net",
			"initial marking", "final marking" }, returnTypes = { Petrinet.class, Marking.class,
					Marking.class }, parameterLabels = { "Direclty-follows graph" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a Process Tree", requiredParameterLabels = { 0 })
	public Object[] minePetriNet(UIPluginContext context, Dfg dfg) {
		IMdMiningDialog dialog = new IMdMiningDialog();
		InteractionResult dialogResult = context.showWizard("Mine using Inductive Miner - directly follows", true, true,
				dialog);
		if (dialogResult != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(false);
			context.getFutureResult(1).cancel(false);
			context.getFutureResult(2).cancel(false);
			return new Object[] { null, null, null };
		}
		PetrinetWithMarkings net = IMdPetriNet.minePetriNet(context, dfg, dialog.getMiningParameters());

		Object[] result = new Object[3];
		result[0] = net.petrinet;
		result[1] = net.initialMarking;
		result[2] = net.finalMarking;

		return result;
	}
}
