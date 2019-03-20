package org.processmining.incorporatenegativeinformation.help;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
// this import will be in the certain version of ModelRepair-6.9.61.jar
import org.processmining.modelrepair.parameters.RepairConfiguration;
import org.processmining.modelrepair.plugins.Uma_RepairModel_Plugin;
// 
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * this class is used to test the reason fot interface is found but class is expected..
 * to find out the bug location, in jar file or the setting in KNIME?? 
 * @author dkf
 *
 */

@Plugin(name = "Test jar for repair model", level = PluginLevel.Regular, returnLabels = {"Net","Initial Marking","Final Marking" }, returnTypes = {
		Petrinet.class, Marking.class, Marking.class}, parameterLabels = { "Log", "Accepting Petri net"}, userAccessible = true) 
//<dependency org="prom" name="TransitionSystems" rev="latest" changing="true" transitive="true" />
public class ModelRepairTest {
	
	// if we want to test, we can only use the plugin context on it.
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Test Jar for repair",  requiredParameterLabels = { 0, 1})
	public Object[] repair(PluginContext context, XLog log, AcceptingPetriNet anet) {
		
		// Marking initMarking = NetUtilities.guessInitialMarking(net);
		// Marking finalMarking = NetUtilities.guessFinalMarking(net);
		RepairConfiguration config =  new RepairConfiguration();
		XEventClassifier classifier = new XEventNameClassifier();
		Uma_RepairModel_Plugin repairer = new Uma_RepairModel_Plugin();
    	Object[] result = repairer.repairModel_buildT2Econnection(context, log,
    			anet.getNet(), anet.getInitialMarking(), anet.getFinalMarkings().iterator().next(), config, classifier);
    	System.out.println(result.length);
    	return  result;
	}
}
