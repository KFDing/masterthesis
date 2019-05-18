package org.processmining.plugins.InductiveMiner.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class IMPetriNet {

	public Object[] minePetriNet(PluginContext context, XLog log) {
		return minePetriNet(context, log, new MiningParametersIM());
	}

	public Object[] minePetriNetParameters(PluginContext context, XLog log, MiningParameters parameters) {
		return minePetriNet(context, log, parameters);
	}

	public static Object[] minePetriNet(final PluginContext context, XLog log, MiningParameters parameters) {

		Object[] result = minePetriNet(log, parameters, new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});

		//create Petri net connections
		context.addConnection(new InitialMarkingConnection((Petrinet) result[0], (Marking) result[1]));
		context.addConnection(new FinalMarkingConnection((Petrinet) result[0], (Marking) result[2]));

		return result;
	}

	public static Object[] minePetriNet(XLog log, MiningParameters parameters, Canceller canceller) {
		ProcessTree tree = IMProcessTree.mineProcessTree(log, parameters, canceller);
		PetrinetWithMarkings pn = null;
		try {
			pn = ProcessTree2Petrinet.convert(tree);
		} catch (NotYetImplementedException e) {
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			e.printStackTrace();
		}

		AcceptingPetriNet a = AcceptingPetriNetFactory.createAcceptingPetriNet(pn.petrinet, pn.initialMarking,
				pn.finalMarking);

		ReduceAcceptingPetriNetKeepLanguage.reduce(a, canceller);

		return new Object[] { a.getNet(), a.getInitialMarking(), a.getFinalMarkings().iterator().next() };
	}

}
