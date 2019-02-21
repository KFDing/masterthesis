package org.processmining.plugins.ding.process.dfg.ui;

import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.plugins.ding.process.dfg.util.GraphvizProcessTree;
import org.processmining.plugins.ding.process.dfg.util.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizPetriNet;
import org.processmining.processtree.ProcessTree;

public class ResultLeftView extends JPanel {
	private static final long serialVersionUID = 2L;
	// to make the layout stable, not remove all the component 
	JPanel graphPanel; 
	JDesktopPane showCMPanel;
	boolean drawDfg = true;
	
	
	// only show the dfg, one is for initialization, one is only to update the view
	public ResultLeftView() {
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.X_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		
		add(graphPanel, new Float(80));
		add(showCMPanel, new Float(20));
		
	}

	// one part for drawing result is to add confusion matrix for the left view
	// we can always show the confusion matrix in our method, it provides better result on it
	// how to organize the result from them?? we have the relative Layout, so we can put one JPanel under the graphPanel 
	// confusion matrix panel..
	// showCMPanel is drawn when it is called and there are alwats confusion_matrix added to it..
	// it is calculated only 
	public void drawConfusionMatrix() {
		showCMPanel = new JDesktopPane();
		
		showCMPanel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		// we need to draw it, 
	}
	
	
	// create an internal frame
	private void createFrame() {
		// TODO if we want to createFrame, we need to make sure that we have confusion_matrix
		String title = "Ext: 1.0; Pos: 1.0; Neg: 1.0";
		ArrayList<Integer> confusion_matrix = new ArrayList<>();
		confusion_matrix.add(100);
		confusion_matrix.add(10);
		confusion_matrix.add(50);
		confusion_matrix.add(100);
		
		CMInternalFrame cmFrame = new CMInternalFrame(title, confusion_matrix);
		cmFrame.setVisible(true);
		
		showCMPanel.add(cmFrame);
		
		try {
			cmFrame.setSelected(true);
			
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void drawResult(ProcessTree pt) {
		this.remove(graphPanel);
		try {
			graphPanel = new DotPanel(GraphvizProcessTree.convert(pt));
			graphPanel.setVisible(true);
			graphPanel.validate();
			
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void drawResult(PluginContext context, AcceptingPetriNet anet) {
		
		this.remove(graphPanel);
		ProMJGraphVisualizer.instance();
		
		graphPanel = new DotPanel(GraphvizPetriNet.convert(anet));
		graphPanel.setVisible(true);
		graphPanel.validate();
		this.add(graphPanel, new Float(100));
		this.add(showCMPanel, new Float(20));
		
	}
	
	
}
