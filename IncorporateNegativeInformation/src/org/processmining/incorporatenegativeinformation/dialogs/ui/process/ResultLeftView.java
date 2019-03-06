package org.processmining.incorporatenegativeinformation.dialogs.ui.process;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.incorporatenegativeinformation.dialogs.ui.process.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.plugins.GraphvizPetriNet;
import org.processmining.processtree.ProcessTree;

public class ResultLeftView extends JPanel{
	private static final long serialVersionUID = 2L;
	// to make the layout stable, not remove all the component 
	JPanel showGraphPanel; 
	DesktopScrollPane showCMPanel;
	boolean drawDfg = true;
	JDesktopPane cmDesktopPane;
	JPanel graphPanel;
	
	// only show the dfg, one is for initialization, one is only to update the view
	public ResultLeftView() {
		RelativeLayout rLayout = new RelativeLayout(RelativeLayout.Y_AXIS);
		rLayout.setFill( true );
		this.setLayout(rLayout);
		
		
		showGraphPanel = new JPanel(new BorderLayout());
		showGraphPanel.setBorder(new TitledBorder("Generated Model"));
		
		cmDesktopPane = new JDesktopPane();
		cmDesktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		showCMPanel = new DesktopScrollPane(cmDesktopPane);
		showCMPanel.setBorder(new TitledBorder("Result In Confusion Matrix"));
		
		
		add(showGraphPanel, new Float(70));
		add(showCMPanel, new Float(30));
		
	}

	// one part for drawing result is to add confusion matrix for the left view
	// we can always show the confusion matrix in our method, it provides better result on it
	// how to organize the result from them?? we have the relative Layout, so we can put one JPanel under the graphPanel 
	// confusion matrix panel..
	
	
	// create an internal frame
	public void addConfusionMatrixView(ArrayList<Integer> confusion_matrix, String title) {
		// TODO if we want to createFrame, we need to make sure that we have confusion_matrix
		// we need to input the title and confusion matrix for it 
		// for the calculation it is ok
		/*
		title = "Ext: 1.0; Pos: 1.0; Neg: 1.0";
		confusion_matrix = new ArrayList<>();
		confusion_matrix.add(100);
		confusion_matrix.add(10);
		confusion_matrix.add(50);
		confusion_matrix.add(100);
		*/
		CMInternalFrame cmFrame = new CMInternalFrame(title, confusion_matrix);
		cmFrame.setVisible(true);
		
		cmDesktopPane.add(cmFrame);
		
		try {
			cmFrame.setSelected(true);
			
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void drawResult(ProcessTree pt) {
		if(graphPanel !=null)
			showGraphPanel.remove(graphPanel);
		
		
		try {
			graphPanel = new DotPanel(GraphvizProcessTree.convert(pt));
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		graphPanel.setVisible(true);
		graphPanel.validate();
		
		showGraphPanel.add(graphPanel);
	}
	
	public void drawResult(PluginContext context, AcceptingPetriNet anet) {
		
		if(graphPanel !=null)
			showGraphPanel.remove(graphPanel);
		
		ProMJGraphVisualizer.instance();
		
		graphPanel = new DotPanel(GraphvizPetriNet.convert(anet));
		graphPanel.setVisible(true);
		graphPanel.validate();
		
		showGraphPanel.add(graphPanel);
	}
	
	

	private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        ResultLeftView view = new ResultLeftView();
        frame.setContentPane(view);
        //Display the window.
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
