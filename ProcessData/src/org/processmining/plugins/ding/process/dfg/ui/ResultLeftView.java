package org.processmining.plugins.ding.process.dfg.ui;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.ding.process.dfg.util.GraphvizDirectlyFollowsGraph;
import org.processmining.plugins.ding.process.dfg.util.GraphvizProcessTree;
import org.processmining.plugins.ding.process.dfg.util.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processtree.ProcessTree;

public class ResultLeftView extends JPanel {
	private static final long serialVersionUID = 2L;
	JPanel graphPanel; 
	boolean drawDfg = true;
	
	
	// only show the dfg, one is for initialization, one is only to update the view
	public ResultLeftView() {
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.X_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		
	}
	
	public void drawResult(Dfg dfg) { 
		// we can get some parameters here to decide which graph we want to get
		// so how to show the dfg result, then back again to the original view
		this.removeAll();
		graphPanel = GraphvizDirectlyFollowsGraph.visualise(dfg);
		graphPanel.setVisible(true);
		graphPanel.validate();
		this.add(graphPanel, new Float(100)); // set 100 is important to make it show in right size
	}

	
	public void drawResult(ProcessTree pt) {
		this.removeAll();
		try {
			graphPanel = new DotPanel(GraphvizProcessTree.convert(pt));
			graphPanel.setVisible(true);
			graphPanel.validate();
			this.add(graphPanel, new Float(100));
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void drawResult(PluginContext context, Petrinet net) {
		
		this.removeAll();
		ProMJGraphPanel pGraphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, net);
		// pGraphPanel.getGraph().setEditable(false);
		pGraphPanel.setVisible(true);
		pGraphPanel.validate();
		
		this.add(pGraphPanel, new Float(100));
		
	}
	
	
}
