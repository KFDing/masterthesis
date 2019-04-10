package org.processmining.incorporatenegativeinformation.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.algorithms.PN2DfgTransform;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.incorporatenegativeinformation.models.DfMatrix;
import org.processmining.incorporatenegativeinformation.models.DfgProcessResult;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.XLog2Dfg;

/**
 * this class includes all the pieces of codes to implement the dfg transform
 * methods input:: Petri net, and event log output:: new modified Petri net with
 * marking, I'd like to say, so we need to give our result of Petri net and
 * DfMatrix procudere:: -- transform Petri net into Dfg -- incorporate the
 * negative information and give out the Dfg and Petri net model -- extract the
 * output of Petri net and pass it with Event log into evaluation
 * 
 * @author dkf
 *
 */
@Plugin(name = "Repair Model by Kefang", returnLabels = { "DfgProcess Result" }, returnTypes = {
		DfgProcessResult.class }, parameterLabels = { "XLog", "Existing Petri Net", "Initial Marking" })
public class DfgRepairPlugin {
	/*
	 * @Plugin(name = "Repair Model by Kefang", returnLabels = {"DfMatrix"},
	 * returnTypes = { DfMatrix.class}, parameterLabels = {
	 * "XLog","Existing Petri Net","Initial Marking"})
	 * 
	 * @UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email =
	 * "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	 * 
	 * @PluginVariant(variantLabel = "Repair Model By Dfg -- AcceptingPN",
	 * requiredParameterLabels = { 0 , 1 }) public DfMatrix
	 * transformPn2Dfg(UIPluginContext context, XLog log, AcceptingPetriNet
	 * anet) throws ConnectionCannotBeObtained { return transformPn2Dfg(context,
	 * log, anet.getNet(), anet.getInitialMarking()); }
	 */

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "kfding1010@gmail.com")
	@PluginVariant(variantLabel = "Repair Model By Dfg For AcceptingPetriNet", requiredParameterLabels = { 0, 1 })
	public static DfgProcessResult transformPn2Dfg(UIPluginContext context, XLog log, Petrinet net)
			throws ConnectionCannotBeObtained {
		Marking marking = NetUtilities.guessInitialMarking(net);
		// here we extract marking from petri net
		return transformPn2Dfg(context, log, net, marking);
	}

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "kfding1010@gmail.com")
	@PluginVariant(variantLabel = "Repair Model By Dfg Without Marking", requiredParameterLabels = { 0, 1 })
	public static DfgProcessResult transformPn2Dfg(UIPluginContext context, XLog log, AcceptingPetriNet anet)
			throws ConnectionCannotBeObtained {
		Marking marking = anet.getInitialMarking();
		// here we extract marking from petri net
		return transformPn2Dfg(context, log, anet.getNet(), marking);
	}

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "kfding1010@gmail.com")
	@PluginVariant(variantLabel = "Repair Model By Dfg Wirh Marking", requiredParameterLabels = { 0, 1, 2 })
	public static DfgProcessResult transformPn2Dfg(UIPluginContext context, XLog log, Petrinet net, Marking marking)
			throws ConnectionCannotBeObtained {
		// -- transform Petri net into Dfg

		Dfg dfg = PN2DfgTransform.transformPN2Dfg(context, net, marking);
		// int num = XLogInfoFactory.createLogInfo(log).getNumberOfTraces();
		// PN2DfgTransform.setCardinality(dfg, num);
		// -- incorporate the negative information and give out the Dfg and Petri net model
		XLog[] result = EventLogUtilities.splitLog(log, Configuration.POS_LABEL, "true");
		XLog pos_log = result[0];
		XLog neg_log = result[1];

		XLog2Dfg ld = new XLog2Dfg();
		Dfg pos_dfg = ld.log2Dfg(context, pos_log);
		Dfg neg_dfg = ld.log2Dfg(context, neg_log);

		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		DfMatrix dfMatrix = DfMatrix.createDfMatrix(dfg, pos_dfg, neg_dfg);

		return new DfgProcessResult(log, dfMatrix);
	}

}
