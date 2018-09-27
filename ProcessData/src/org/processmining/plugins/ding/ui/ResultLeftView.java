package org.processmining.plugins.ding.ui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.processtree.ProcessTree;

public class ResultLeftView extends JPanel {
	private static final long serialVersionUID = 2L;
	PluginContext context;
	ProMJGraphPanel graphPanel; 
	Double currentScale = 1.0;
	RelativeLayout rl ; 
	boolean drawDfg = true;
	
	class ZoomListenerOnLeftPanel implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			int stps = e.getWheelRotation();
			Double newScale = currentScale;
			if (stps == -1) {
				newScale *= 1.25;
			}
			else if (stps == 1) {
				newScale *= 0.80;
			}
			setScale(newScale);
		}
	}
	
	private void setScale(Double newScale) {
		// TODO Auto-generated method stub
		graphPanel.setScale(newScale);
		currentScale = newScale;
	}
	
	// only show the dfg, one is for initialization, one is only to update the view
	public ResultLeftView(PluginContext context) {
		rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.context = context;
		
	}
	
	public void drawResult(Dfg dfg) {
		// we can get some parameters here to decide which graph we want to get
		graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, (DirectedGraph<?, ?>) dfg);
		graphPanel.getGraph().setEditable(false);
	}
	
	
	public void drawResult(ProcessTree pt) {
		graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, (DirectedGraph<?, ?>) pt);
		graphPanel.getGraph().setEditable(false);
	}
	
	public void drawResult(Petrinet net) {
		graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, net);
		graphPanel.getGraph().setEditable(false);
	}
	
	
}
