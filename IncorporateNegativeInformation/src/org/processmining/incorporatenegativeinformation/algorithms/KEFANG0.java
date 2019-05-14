package org.processmining.incorporatenegativeinformation.algorithms;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.astar.AStarException;

@Plugin(name = "KEFANG0", level = PluginLevel.Regular, returnLabels = {
"ALIGNMENTS" }, returnTypes = {
		PNRepResult.class }, parameterLabels = { "Log", "Petri net", "Marking", "Final Marking" }, userAccessible = true)
public class KEFANG0 {
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "KEFANG0", requiredParameterLabels = { 0, 1, 2, 3 })
	public PNRepResult performAlignments(PluginContext context, XLog log, Petrinet net, Marking marking, Marking finalMarking) {
		// NB USE THE STANDARD CLASSIFIER
		XEventClass evClassDummy = new XEventClass("DUMMY", -1);
		TransEvClassMapping mapping = new TransEvClassMapping(XLogInfoImpl.NAME_CLASSIFIER, evClassDummy);
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		for (XEventClass ec : logInfo.getEventClasses().getClasses()) {
			for (Transition transition : net.getTransitions()) {
				if (transition.getLabel().equals(ec.toString())) {
					mapping.put(transition, ec);
				}
			}
		}
		System.out.println(logInfo.getEventClasses().getClasses());
		for (Transition transition : net.getTransitions()) {
			System.out.println(transition.getLabel());
		}
		// ystem.out.println(mapping);
		
		System.out.println("A");
		// create parameter
		CostBasedCompleteParam parameter = new CostBasedCompleteParam(logInfo.getEventClasses().getClasses(),
				evClassDummy, net.getTransitions(), 2, 5);
		parameter.setGUIMode(false);
		parameter.setCreateConn(false);
		parameter.setInitialMarking(marking);
		parameter.setFinalMarkings(new Marking[] {finalMarking});
		parameter.setMaxNumOfStates(200000);
		System.out.println("B");

		
		// instantiate replayer
		PNLogReplayer replayer = new PNLogReplayer();
		
		PetrinetReplayerWithoutILP replWithoutILP = new PetrinetReplayerWithoutILP();
		PNRepResult pnRepResult = null;
		System.out.println("C");

		try {
			pnRepResult = replayer.replayLog(null, net, log, mapping, replWithoutILP, parameter);
		} catch (AStarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("D");
		System.out.println(pnRepResult);
		int fit_sum=0, unfit_sum=1;
		for (SyncReplayResult variantAlignment : pnRepResult) {
			// variantAlignment.getTraceIndex(); <- index of the log of the trace that has the variant
			int i = 0;
			while (i < variantAlignment.getNodeInstance().size()) {
				StepTypes typeOfMove = variantAlignment.getStepTypes().get(i);
				
				if(typeOfMove.equals(StepTypes.LMNOGOOD)) {
					unfit_sum++;
					break;
				}
				i++;
			}
			fit_sum++;
		}
		System.out.println("fit-sum:" + fit_sum);
		System.out.println("unfit-sum:" + unfit_sum);
		return pnRepResult;
	}
}
