package org.processmining.plugins.ding.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.IMdMiningDialog;
import org.processmining.plugins.ding.model.Configuration.ViewType;
import org.processmining.plugins.ding.model.ControlParameters;
import org.processmining.plugins.ding.train.DfMatrix;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

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
	
	DfMatrix dfMatrix;
	Dfg dfg = null;
	ProcessTree pTree = null;
	@SuppressWarnings("deprecation")
	AcceptingPetriNet anet = null;
	ProvidedObjectID dfgId = null;
	ProvidedObjectID pTreeId = null;
	ProvidedObjectID netId = null;
	ProvidedObjectID markingId = null;
	boolean updateAll = true;
	
	public ResultMainView(UIPluginContext context, final DfMatrix matrix) {
		this.context = context;
		dfMatrix = matrix;
		parameters =  new ControlParameters();
		
		rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(new Color(240, 240, 240));
		
		
		leftView =  new ResultLeftView();
		rightView =  new ResultRightControlView();
		
		parameters.cloneValues(rightView.getParameters());
		
		dfMatrix.updateCardinality(0, parameters.getExistWeight());
		dfMatrix.updateCardinality(1, parameters.getPosWeight());
		dfMatrix.updateCardinality(2, parameters.getNegWeight());
		
		showDfg();
		if(dfgId == null) {
			dfgId =context.getProvidedObjectManager().createProvidedObject("Generated Dfg", dfg, Dfg.class, context);
		}
		
		/*
		dfg = dfMatrix.buildDfs();
		leftView.drawResult(dfg);
		leftView.setVisible(true);
		*/
		JButton submitButton = rightView.getSubmitButton();
		
		submitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// we need to pass the parameters to MainView, in the mainView, 
						// it controls to generate the new view
						try {
							
							updateMainView(leftView, rightView.getParameters());
						} catch (ProvidedObjectDeletedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}          
			    });
		
		this.add(this.leftView, new Float(80));
		this.add(this.rightView, new Float(20));
	} 
	
	public void updateMainView(ResultLeftView leftView, ControlParameters newParameters) throws ProvidedObjectDeletedException{
		// if there is only type changes, so we don't need to generate it again for the dfMatrix
		// we just use the dfg, process tree and petri net 
		// how to distinguish them?? I think I can add the weight to the DfMatrix, and compare it 
		// with the new ones, if sth changes, so we need to generate them again? If not changes, then 
		// we don't need to do it 
		// if we need to create new Dfg ?? 
		updateAll =  isWeightUpdated(parameters, newParameters);
		if(updateAll) {
			parameters.cloneValues(newParameters);
			dfMatrix.updateCardinality(0, parameters.getExistWeight());
			dfMatrix.updateCardinality(1, parameters.getPosWeight());
			dfMatrix.updateCardinality(2, parameters.getNegWeight());

		}
		parameters.setType(newParameters.getType());
		
		if(parameters.getType() == ViewType.Dfg) {
			showDfg();
			// if we show them here, we add dfg into the global context to let them show out
			if(dfgId != null)
				context.getProvidedObjectManager().changeProvidedObjectObject(dfgId, dfg);
		}else if(parameters.getType() == ViewType.ProcessTree) {
			showProcessTree();
			if(pTreeId == null) {
				pTreeId =context.getProvidedObjectManager().createProvidedObject("Generated Process Tree", pTree, ProcessTree.class, context);
			}else {
				context.getProvidedObjectManager().changeProvidedObjectObject(pTreeId, pTree);
			}
			// add process tree into result, but we need to keep there only one process tree there
		}else if(parameters.getType() == ViewType.PetriNet) {
			showPetriNet();
			// if we add new, we need to delete the old petri net 
			if(netId == null) {
				netId =context.getProvidedObjectManager().createProvidedObject("Generated Petri net", anet.getNet(), Petrinet.class, context);
				markingId =context.getProvidedObjectManager().createProvidedObject("Initial Marking", anet.getInitialMarking(), Marking.class, context);
				
			}else {
				context.getProvidedObjectManager().changeProvidedObjectObject(netId, anet.getNet());
				context.getProvidedObjectManager().changeProvidedObjectObject(markingId, anet.getInitialMarking());
			}
		}
			
	}
	
	
	private boolean isWeightUpdated(ControlParameters para, ControlParameters newPara) {
		if(para.getExistWeight() == newPara.getExistWeight() 
				&& para.getPosWeight() == newPara.getPosWeight()
				&& para.getNegWeight() == para.getNegWeight())
			return false;
		return true;
	}

	private void showDfg()  {
		if( updateAll) {
			dfg =  dfMatrix.buildDfs();	
		}
		leftView.drawResult(dfg);
		leftView.updateUI();
	}
	
	private void showProcessTree() {
		if(updateAll) {
			dfg =  dfMatrix.buildDfs();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
		}else if(pTree == null){
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
		}
		
		leftView.drawResult(pTree);
		leftView.updateUI();
	}
	
	@SuppressWarnings("deprecation")
	private void showPetriNet() {
		
		if(updateAll) {
			dfg =  dfMatrix.buildDfs();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);

			try {
				PetrinetWithMarkings net = ProcessTree2Petrinet.convert(pTree, true);
				anet = new AcceptingPetriNetImpl(net.petrinet, net.initialMarking, net.finalMarking);
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidProcessTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if(anet == null) {
			if(pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}	
			try {
				PetrinetWithMarkings net = ProcessTree2Petrinet.convert(pTree, true);
				anet = new AcceptingPetriNetImpl(net.petrinet, net.initialMarking, net.finalMarking);
			} catch (NotYetImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidProcessTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		
		leftView.drawResult(context, anet.getNet());
		leftView.updateUI();
	}
	private DfgMiningParameters getProcessTreParameters() {
		   IMdMiningDialog dialog = new IMdMiningDialog();
		   dialog.setSize(100, 100);
		   // it is a Jpanel, not a dialog, now I just want to get the pop up JPanel and read the input from it 
		   // String parameters = JOptionPane.showInputDialog(this, "Set parameters fro IM", "Setting", JOptionPane.QUESTION_MESSAGE);
		   JOptionPane.showMessageDialog( this,
				   dialog,
	               "Setting Miner Parameters for Dfg",
	               JOptionPane.INFORMATION_MESSAGE);
		   DfgMiningParameters parameters = dialog.getMiningParameters();
	       // System.out.println("Paras " + parameters.getNoiseThreshold());     
	       
	       return parameters;
	   }

	
}
