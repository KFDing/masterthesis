package org.processmining.plugins.ding.process.dfg.transform;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.ding.process.dfg.train.NewBranchLTDetector;
import org.processmining.processtree.ProcessTree;

@Plugin(name = "Get XOR Branches Correlation", level = PluginLevel.Regular, returnLabels = {"Test Process Tree" }, returnTypes = {
		ProcessTree.class}, parameterLabels = { "Process Tree","XLog"}, userAccessible = true)

public class GeneratorTest {

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(requiredParameterLabels = { 0 , 1})
	public ProcessTree testGenerate(PluginContext context, ProcessTree tree, XLog log) {
		//NewXORBranchGenerator<ProcessTreeElement> generator =  new NewXORBranchGenerator();
		// generator.buildBranches(tree);
		NewBranchLTDetector detector = new NewBranchLTDetector(tree, log);
		detector.buildLT();
		
		return tree;
	}
	
}
