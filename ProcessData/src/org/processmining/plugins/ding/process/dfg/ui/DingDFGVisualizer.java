package org.processmining.plugins.ding.process.dfg.ui;

/*
@Plugin(name = "Show Process Result from Dfg", parameterLabels = { "Process result" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class DingDFGVisualizer {
	@PluginVariant(requiredParameterLabels = { 0})
	public JPanel visualize(PluginContext context, DfMatrix dfMatrix) {
		Dfg dfg = dfMatrix.buildDfs();
		JPanel ret = new JPanel();
		
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.X_AXIS);
		rLayout.setFill( true );
		ret.setLayout(rLayout);
		LeftPanel80 lPanel80 = new LeftPanel80(dfg);
		ret.add(lPanel80, new Float(80));
		RightPanel20 rPanel20 = new RightPanel20();
		
		ret.add(rPanel20, new Float(20));

		return ret;
	}
}

class LeftPanel80 extends JPanel {
	public LeftPanel80(Dfg dfg) {
		JPanel graphPanel = GraphvizDirectlyFollowsGraph.visualise(dfg);
		graphPanel.setVisible(true);
		graphPanel.validate();
		System.out.println("HELLO WORLD");
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.X_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		this.add(graphPanel, new Float(100));
	}
}

class RightPanel20 extends JPanel {
	public RightPanel20() {
		JLabel hello = new JLabel("hello");
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.X_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		this.add(hello, new Float(100));
	}
}*/