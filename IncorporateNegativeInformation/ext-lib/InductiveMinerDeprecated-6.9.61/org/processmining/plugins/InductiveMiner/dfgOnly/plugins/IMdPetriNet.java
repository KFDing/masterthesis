package org.processmining.plugins.InductiveMiner.dfgOnly.plugins;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

public class IMdPetriNet {

	public static PetrinetWithMarkings minePetriNet(PluginContext context, Dfg dfg, DfgMiningParameters parameters) {

		PetrinetWithMarkings net = minePetriNet(dfg, parameters);

		//create Petri net connections
		context.addConnection(new InitialMarkingConnection(net.petrinet, net.initialMarking));
		context.addConnection(new FinalMarkingConnection(net.petrinet, net.finalMarking));

		return net;
	}

	public static PetrinetWithMarkings minePetriNet(Dfg dfg, DfgMiningParameters parameters) {
		try {
			return ProcessTree2Petrinet.convert(IMdProcessTree.mineProcessTree(dfg, parameters));
		} catch (NotYetImplementedException e) {
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			e.printStackTrace();
		}
		return null;
	}
}
