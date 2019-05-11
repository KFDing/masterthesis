package org.processmining.incorporatenegativeinformation.algorithms;
// create a plugin to read Petri net and print out the cycles in it

import java.util.List;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

@Plugin(name = "Detect Loops in Petri net", parameterLabels = { "Petri net" }, returnLabels = { "Petri net" }, returnTypes = {
		Petrinet.class }, userAccessible = true, help = "detect loops in Petri net ")

public class LoopDetectorTest {

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Detect loops in Petri net", requiredParameterLabels = { 0 })
	public Petrinet detectLoop(PluginContext context, Petrinet net) {
		
		LoopDetectorTarjan detector = new LoopDetectorTarjan(net, false);
		List<List<PetrinetNode>> cycles = detector.findAllSimpleCycles();
		System.out.println(cycles.size());
		return net;
	}
	
}
