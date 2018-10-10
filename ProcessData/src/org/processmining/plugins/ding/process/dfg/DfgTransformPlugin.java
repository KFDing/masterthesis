package org.processmining.plugins.ding.process.dfg;

import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.XLog2Dfg;
import org.processmining.plugins.ding.process.dfg.model.DfMatrix;
import org.processmining.plugins.ding.process.dfg.train.IncorporateNeg2Dfg;
import org.processmining.plugins.ding.process.dfg.transform.PN2DfgTransform;

/**
 * this class includes all the pieces of codes to implement the dfg transform methods
 * input:: 
 * 		Petri net, and event log
 * output::
 * 		new modified Petri net with marking, I'd like to say, so we need to give our result of Petri net and DfMatrix
 * procudere::
 * 		-- transform Petri net into Dfg
 * 		-- incorporate the negative information and give out the Dfg and Petri net model
 * 		-- extract the output of Petri net  and pass it with Event log into evaluation
 * 
 * @author dkf
 *
 */
@Plugin(name = "Repair Model by Kefang", returnLabels = {"DfMatrix"}, 
   returnTypes = { DfMatrix.class}, parameterLabels = { "XLog","Existing Petri Net","Initial Marking"})
public class DfgTransformPlugin {
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Repair Model By Dfg -- AcceptingPN",  requiredParameterLabels = { 0 , 1 })
	public DfMatrix transformPn2Dfg(UIPluginContext context, XLog log, AcceptingPetriNet anet) throws ConnectionCannotBeObtained {
		return transformPn2Dfg(context, log, anet.getNet(), anet.getInitialMarking());
	}
	
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Repair Model By Dfg-- With Marking",  requiredParameterLabels = { 0 , 1 , 2})
	public DfMatrix transformPn2Dfg(UIPluginContext context, XLog log, Petrinet net, Marking marking) throws ConnectionCannotBeObtained {
		// -- transform Petri net into Dfg
		
		Dfg dfg = PN2DfgTransform.transformPN2Dfg(context, net, marking);
		int num = XLogInfoFactory.createLogInfo(log).getNumberOfTraces();
		// PN2DfgTransform.setCardinality(dfg, num);
		// -- incorporate the negative information and give out the Dfg and Petri net model
		Object[] result = IncorporateNeg2Dfg.splitEventLog(log);
		XLog pos_log = (XLog) result[0];
		XLog neg_log = (XLog) result[1];
		 
		XLog2Dfg ld = new XLog2Dfg();
		Dfg pos_dfg = ld.log2Dfg(context, pos_log);
		Dfg neg_dfg = ld.log2Dfg(context, neg_log);
		
		// get a new dfg, how to get it, new start activity, end activity, and also the direct follow
		DfMatrix dfMatrix = IncorporateNeg2Dfg.createDfMatrix(dfg, pos_dfg, neg_dfg);
		dfMatrix.setStandardCardinality(num);
		
		return dfMatrix;
	}

}
