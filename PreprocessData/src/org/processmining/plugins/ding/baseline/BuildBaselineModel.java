package org.processmining.plugins.ding.baseline;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.modelrepair.plugins.Uma_RepairModel_Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.ding.util.Configuration;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.MiningParameters;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerDialog;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;

/**
 * This class accepts the event log and build baseline models from it. 
 * Event log with labels information after preprocessing
 * Baseline Algorithm is Inductive Miner, we need to invoke it 
 * @author dkf
 *
 */

public class BuildBaselineModel {
		
	// first not consider if the event log is labeled, or not
	@Plugin(name = "Build Petri net Model with Inductive Miner", level = PluginLevel.Regular, returnLabels = {"Petri net", "Initial Marking" }, returnTypes = {
			Petrinet.class, Marking.class}, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Generate a petri net",  requiredParameterLabels = { 0})
	public Object[] buildModel(UIPluginContext context, XLog log) throws UnknownTreeNodeException, ReductionFailedException {
		
		InductiveMinerDialog dialog = new InductiveMinerDialog(log);
		InteractionResult result = context.showWizard("Mine using Inductive Miner", true, true, dialog);

		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(false);
			return null;
		}
		MiningParameters parameters = dialog.getMiningParameters();
		IMLog xlog = parameters.getIMLog(log);

		//check that the log is not too big and mining might take a long time
		if (!InductiveMinerPlugin.confirmLargeLogs(context, xlog, dialog)) {
			context.getFutureResult(0).cancel(false);
			return null;
		}

		context.log("Mining...");
		
		Collection<BaselineConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(BaselineConnection.class, context, log);
			for (BaselineConnection connection : connections) {
				if ( connection.getObjectWithRole(BaselineConnection.TYPE).equals(BaselineConnection.BASELINE_PN)
						&& connection.getObjectWithRole(BaselineConnection.LOG).equals(log)
						&& ((MiningParameters)connection.getObjectWithRole(BaselineConnection.MINING_PARAMETERS)).equals(parameters) 
						) {
					return connection.getObjectWithRole(BaselineConnection.RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}

		AcceptingPetriNet anet = InductiveMinerPlugin.minePetriNet(xlog, parameters, new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
		
		// we need to create a connection which put parameters into it 
		context.addConnection( new BaselineConnection(log, parameters, anet.getNet()));
		return new Object[] {anet.getNet(), anet.getInitialMarking()};
	}
	
	// we could some controls to see it if we use the labeled log, or whole log
	@Plugin(name = "Build Petri net Model with Inductive Miner", level = PluginLevel.Regular, returnLabels = {"Petri net"," Initial Marking" }, 
			returnTypes = {Petrinet.class, Marking.class}, parameterLabels = { "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Generate a petri net only based on the postive value",  requiredParameterLabels = { 0})
	public Object[] buildPosModel(UIPluginContext context, XLog log) throws UnknownTreeNodeException, ReductionFailedException {
		// here we need to create a new log, so we can keep the original ones untouched
		XLog pos_log = (XLog) log.clone();
		
		boolean only_pos = true;
		int neg_count = 0, pos_count=0;
		if(only_pos) {
			Iterator liter = pos_log.iterator();
			while(liter.hasNext()) {
				XTrace trace = (XTrace) liter.next();
				if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
					XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
					if(!attr.getValue()) {
						liter.remove();
						neg_count++;
					}else
						pos_count++;
				}
			}
			// output one dialog to say how many pos and neg traces they have
			JOptionPane.showConfirmDialog(null,
				    "The positive example has " + pos_count + ", negative has "+ neg_count,
				    "Inane information",
				    JOptionPane.INFORMATION_MESSAGE);
		}
		return buildModel(context, pos_log);
	}
	
	@Plugin(name = "Generate efficient tree with Inductive Miner", level = PluginLevel.Regular, returnLabels = {
	"Efficient Tree" }, returnTypes = {	EfficientTree.class },   parameterLabels = { "Log" }, userAccessible = false)
	// we could some controls to see it if we use the labeled log, or whole log
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Generate a Process Tree only based on the postive value",  requiredParameterLabels = { 0})
	public EfficientTree buildPosProcessTree(UIPluginContext context, XLog log) {
		
		XLog pos_log = (XLog) log.clone();
		boolean only_pos = true;
		if(only_pos) {
			Iterator liter = pos_log.iterator();
			while(liter.hasNext()) {
				XTrace trace = (XTrace) liter.next();
				if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
					XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
					if(!attr.getValue()) 
						liter.remove();
				}
			}
		}
		
		InductiveMinerPlugin miner = new InductiveMinerPlugin();
		return miner.mineGuiProcessTree(context, pos_log);
	}
	
	
	/**
	 * build baseline from repaired model and later on KPI driven process
	 */
	@Plugin(name = "Build Petrinet Model after Repair", level = PluginLevel.Regular, returnLabels = {"Petri net"," Initial Marking" }, 
			returnTypes = {Petrinet.class, Marking.class}, parameterLabels = { "Log" , "Original Petrinet"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "repair a petri net on event log",  requiredParameterLabels = { 0 ,1})
	public Object[] buildRepairModel(UIPluginContext context, XLog log, Petrinet net){
		Uma_RepairModel_Plugin modelRepairPlugin=new Uma_RepairModel_Plugin();
		Object[] result = modelRepairPlugin.repairModel(context, log, net);
		
		// it has some exceptions, so I need to debug them ...
		return new Object[]{result[0], result[1]};
	}
	
	/**
	 * and later on KPI driven process
	 */
	@Plugin(name = "Build Petrinet KPI Model after Repair", level = PluginLevel.Regular, returnLabels = {"Petri net"," Initial Marking" }, 
			returnTypes = {Petrinet.class, Marking.class}, parameterLabels = { "Log" ,"Original Petrinet"}, userAccessible = true)
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "repair a petri net on event log with KPI",  requiredParameterLabels = { 0,1})
	public Object[] buildKPIRepairModel(UIPluginContext context, XLog log, Petrinet net){
		
		
		Uma_RepairModel_Plugin modelRepairPlugin=new Uma_RepairModel_Plugin();
		Object[] result = modelRepairPlugin.repairModel(context, log, net);
		return new Object[]{result[0], result[1]};
	}
}
