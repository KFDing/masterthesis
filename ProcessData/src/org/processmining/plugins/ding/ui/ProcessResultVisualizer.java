package org.processmining.plugins.ding.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMd;
import org.processmining.plugins.ding.model.ControlParameters;
import org.processmining.plugins.ding.train.Configuration.ViewType;
import org.processmining.plugins.ding.train.DfMatrix;
import org.processmining.processtree.ProcessTree;

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
	public JComponent visualize(UIPluginContext context, DfMatrix dfMatrix) {
		// but here we need to update the value of dfMatrix and then later to generate the dfg
		// so here our parameter can be the dfMatrix
		// Dfg dfg = dfMatrix.buildDfs();
		// it accept the online controlled parameters and update the value, later to generate new-modfied dfg
		
		return new ResultMainView(context, dfMatrix);
		
	}
}
	
	// one to display the process tree, or can we later to put the petri net into the same layout?? 
	// we can do it, and call another methods to achive it.. By add one button to transform it 
	// but now we just focus on the display of process tree
class ResultMainView extends JPanel{
	
	private static final long serialVersionUID = 0L;
	RelativeLayout rl;
	ResultLeftView leftView;
	ResultRightControlView rightView;
	ControlParameters parameters;
	UIPluginContext context;
	
	public ResultMainView(UIPluginContext context, final DfMatrix dfMatrix) {
		this.context = context;
		rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(new Color(240, 240, 240));
		
		parameters = new ControlParameters();
		
		leftView =  new ResultLeftView();
		rightView =  new ResultRightControlView(parameters);
		
		// display the view from rightView
		
		dfMatrix.updateCardinality(0, parameters.getExistWeight());
		dfMatrix.updateCardinality(1, parameters.getPosWeight());
		dfMatrix.updateCardinality(2, parameters.getNegWeight());
		
		Dfg dfg = dfMatrix.buildDfs();
		leftView.drawResult(dfg);
		leftView.setVisible(true);
		
		JButton submitButton = rightView.getSubmitButton();
		
		submitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// we need to pass the parameters to MainView, in the mainView, 
						// it controls to generate the new view
						parameters =rightView.getParameters();
						updateMainView(leftView, dfMatrix, parameters);
					}          
			    });
		
		this.add(this.leftView, new Float(80));
		this.add(this.rightView, new Float(20));
	} 
	
	public void updateMainView(ResultLeftView leftView, DfMatrix dfMatrix, ControlParameters parameters) {
	
		dfMatrix.updateCardinality(0, parameters.getExistWeight());
		dfMatrix.updateCardinality(1, parameters.getPosWeight());
		dfMatrix.updateCardinality(2, parameters.getNegWeight());
		Dfg dfg =  dfMatrix.buildDfs();
		
		if(parameters.getType() == ViewType.Dfg) {
			leftView.drawResult(dfg);
			leftView.updateUI();
		}
		if(parameters.getType() == ViewType.ProcessTree) {
			// change the Dfg to process tree;
			IMd idm = new IMd();
			// how to generate the new dialog for input??? 
			ProcessTree pTree = idm.mineProcessTree(context, dfg);
			
			leftView.drawResult(pTree);
			leftView.updateUI();
		}
	}
	
	
}
