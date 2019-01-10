package org.processmining.plugins.ding.preprocess;
/**
 * this class is used to provide a plugin to create process tree. 
 * By using the editModule from InductiveVisualMiner..
 *   In the existing method, we have  EditModelView in org.processmining.plugins.inductiveVisualMiner.editModel;
 *   But it need a parent component, anything, we don't really need a lot of things, 
 *      one method to setModel should be known before how to infer it  
 *  
 *  Because it is a plugin, so we have context, it should generate one Process tree as result for this plugin in
 *    Should we make it interactive, to show the model during the editing. 
 *  Interactive is better, then make it like this, but the output is what ?? 
 *  
 *  Input is 0, 
 *  Output is process tree, if it is closed at last time?? Not really, we just have a Jpanel, that's all right!!
 *  
 *  After opening the plugin, it shows an inductive panel, 
 *   -- right panel is to edit the current tree
 *   -- left panel is to show the process tree generated from editing
 *   
 *   -- Extra button to export current process tree into ProM.
 * Nothing no more
 * 
 * @author dkf, modified code from S.j.j.. 
 * @date 10 Jan 2019 // it's already an another year
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeFactory;
import org.processmining.plugins.ding.preprocess.util.GraphvizProcessTree;
import org.processmining.plugins.ding.preprocess.util.GraphvizProcessTree.NotYetImplementedException;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.editModel.EditModelView;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.processtree.ProcessTree;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class EditProcessTreePlugin {
	@Plugin(name = "Edit Process Tree Plugin", returnLabels = { "Process Tree Edit Panel" }, returnTypes = {
			JComponent.class }, parameterLabels = {"Process Tree"}, userAccessible = true, level = PluginLevel.PeerReviewed)
// 
	@UITopiaVariant(pack = "Preprocess", affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "To have some effect with process tree",  requiredParameterLabels = { 0})
	public JComponent visualise(PluginContext context, ProcessTree tree) {
		return generatePanel(context, tree);
	}
	
	public JPanel generatePanel(PluginContext context, ProcessTree tree) {
		 // how to create an empty efficient tree?? 
		EfficientTree eTree = EfficientTreeFactory.create(null, null, null);
		
		JPanel mainPanel = new JPanel();
		double size[][] = { {5, TableLayoutConstants.FILL, 200 }, { TableLayoutConstants.FILL, 100} };
		mainPanel.setLayout(new TableLayout(size));
		// specifying the upper, left and lower, right corners of that set.
		// add right control panel
		
		RightControlPanel rightControlPanel = new RightControlPanel(eTree);
		mainPanel.add(rightControlPanel, "1,1,2,1");
		// here we should have something to show it here 
		
		// add left view panel	
		// ProcessTree tree = EfficientTree2processTree.convert(eTree);
		LeftViewPanel leftViewPanel = new LeftViewPanel(tree);
		mainPanel.add(leftViewPanel, "1,0,2,1");
		
		// add export panel
		JPanel exportPanel = new JPanel();
		exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.LINE_AXIS));
		exportPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		mainPanel.add(exportPanel, "2,1,2,2");
		
		
		// add button into the exportPanel here
		JTextField exportName = new JTextField("Export Name");
		JButton exportModelBtn = new JButton("Export Model");
		exportModelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO export the model with name ?? we need to assign a name to it
				context.getProvidedObjectManager().createProvidedObject("Process tree of " + exportName.getText(), tree, ProcessTree.class,
						context);
				System.out.println(exportName.getText());
			}
		});
		
		exportPanel.add(Box.createHorizontalGlue());
		exportPanel.add(exportName);
		exportPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		exportPanel.add(exportModelBtn);
		// it might be good that we only give the visualizer only the empty process tree 
		// and then watch out the performance than this one?? 
		
		return mainPanel;
	}
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Test Outcoming");
		EditProcessTreePlugin plugin = new EditProcessTreePlugin();
		
		// frame.add(plugin.generatePanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String args[])
	{
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	
}
//left control panel, divided into two parts, one is for editing, one it for export
//but we can really do it maybe in the table part?? 
class RightControlPanel extends JPanel{

	private static final long serialVersionUID = 3L;
	
	EditModelView editDialog ;
	ProcessTree tree;
	
	public RightControlPanel(EfficientTree eTree) {
		editDialog = new EditModelView(this);
		IvMModel model = new IvMModel(eTree);
		// if we hear sth from the editDialog, we need to change into this values
		this.add(editDialog);
		this.setVisible(true);
	}
	// we just use the setModel when it is used that's all..
	
}
// right view panel, it accept one process tree model from mainPanel, and update the view
// in simple visualizer..
class LeftViewPanel extends JPanel {

	private static final long serialVersionUID = 2L;
	ProcessTree tree;
	public LeftViewPanel(ProcessTree pt) {
		tree = pt;
	}
	
	public void drawModel() {
		
		this.removeAll();
		JPanel graphPanel;
		try {
			graphPanel = new DotPanel(GraphvizProcessTree.convert(tree));
			graphPanel.setVisible(true);
			graphPanel.validate();
			this.add(graphPanel);
			
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}



