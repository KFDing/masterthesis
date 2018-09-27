package org.processmining.plugins.ding.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.ding.train.DfMatrix;

/**
 * this class is built as a visualizer for process tree to display, but it can also display the dfg;
 * the I should change the name of this class into ProcessResultVisualizer?? 
 * 
 * @author dkf
 *
 */
@Plugin(name = "Show Process Result from Dfg", parameterLabels = { "Process result" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ProcessResultVisualizer {

	// one to display the dfg
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, DfMatrix dfMatrix) {
		// but here we need to update the value of dfMatrix and then later to generate the dfg
		// so here our parameter can be the dfMatrix
		Dfg dfg = dfMatrix.buildDfs();
		// it accept the online controlled parameters and update the value, later to generate new-modfied dfg
		
		return createMainView(dfMatrix);
		
	}
	
	
	// one to display the process tree, or can we later to put the petri net into the same layout?? 
	// we can do it, and call another methods to achive it.. By add one button to transform it 
	// but now we just focus on the display of process tree
	
	private JPanel createMainView(DfMatrix dfMatrix) {
		ResultLeftView leftView;
		
		
		return null;
	} 
}
