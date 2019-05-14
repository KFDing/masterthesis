package org.processmining.incorporatenegativeinformation.algorithms;

import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
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

/**
 * admit it not so easy to write it on your own, let it go in this way. Jaja, let it go like this way
 * but one thing is to incorporate it with our codes, but in reality. It differs from ours in ways
 * @author dkf
 *
 */
public class PNReplayer {
	XLog log;
	Petrinet net;
	TransEvClassMapping mapping ;
	CostBasedCompleteParam parameter;
	public PNReplayer(XLog xlog, Petrinet pnet, Marking initMarking, Marking finalMarking) {
		log = xlog;
		net = pnet;
		
		// create the mapping for alignment checking
		XEventClass evClassDummy = new XEventClass("DUMMY", -1);
		mapping = new TransEvClassMapping(XLogInfoImpl.NAME_CLASSIFIER, evClassDummy);
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		// this map is created based on the transitions, so back to transitions
		
		for (Transition transition : net.getTransitions()) {
			boolean hasMap = false;
			for (XEventClass ec : logInfo.getEventClasses().getClasses()) {
					if (transition.getLabel().equals(ec.toString())) {
						mapping.put(transition, ec);
						hasMap = true;
						break;
					}
			}
			
			if(!hasMap)
				mapping.put(transition, evClassDummy);	
			
		}
		
		// initialize the parameters for replayer
		parameter = new CostBasedCompleteParam(logInfo.getEventClasses().getClasses(),
				evClassDummy, net.getTransitions(), 2, 5);
		parameter.setGUIMode(false);
		parameter.setCreateConn(false);
		parameter.setInitialMarking(initMarking);
		parameter.setFinalMarkings(new Marking[] {finalMarking});
		parameter.setMaxNumOfStates(200000);
		
	}
	// replay on the given data
	public PNRepResult replay() {
		PNLogReplayer replayer = new PNLogReplayer();
		
		PetrinetReplayerWithoutILP replWithoutILP = new PetrinetReplayerWithoutILP();
		PNRepResult pnRepResult = null;
		try {
			pnRepResult = replayer.replayLog(null, net, log, mapping, replWithoutILP, parameter);
		} catch (AStarException e) {
			// TODO Auto-generated catch block
			System.out.println("replayer methods go wrong");
			e.printStackTrace();
		}
		return pnRepResult;
	}
	public boolean fitTraceVariant(SyncReplayResult variantAlignment) {
		// we check here if the trace fit totally-- only with sync move
		boolean fit = true;
		
		List<StepTypes> stepTypes = variantAlignment.getStepTypes();
		for(StepTypes type: stepTypes) {
			if(type.equals(StepTypes.L) || type.equals(StepTypes.MREAL )) {
				fit = false;
				break;
			}
		}
		
		return fit;
	}
	
}
