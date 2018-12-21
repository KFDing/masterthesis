package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
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
import org.processmining.plugins.ding.process.dfg.model.ControlParameters;
import org.processmining.plugins.ding.process.dfg.model.DfMatrix;
import org.processmining.plugins.ding.process.dfg.model.DfgProcessResult;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration.ViewType;
import org.processmining.plugins.ding.process.dfg.train.NewLTDetector;
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
@Plugin(name = "Show Process Result from Dfg", parameterLabels = {"DfgProcess Result"}, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
@SuppressWarnings("deprecation")
public class ProcessResultVisualizer {

	// one to display the dfg
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, DfgProcessResult dfgResult) {
		return new ResultMainView(context, dfgResult);
		
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
	XLog log;
	DfMatrix dfMatrix;
	Dfg dfg = null;
	ProcessTree pTree = null;
	AcceptingPetriNet anet = null;
	AcceptingPetriNet manet = null;
	
	ProvidedObjectID ltnetId = null;
	ProvidedObjectID pTreeId = null;
	ProvidedObjectID netId = null;
	ProvidedObjectID markingId = null;
	boolean updateAll = true;
	
	
	public ResultMainView(UIPluginContext context, DfgProcessResult dfgResult) {
		this.context = context;
		log = dfgResult.getLog();
		dfMatrix = dfgResult.getDfMatrix();
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
		
		showProcessTree();
		if(pTreeId == null) {
			pTreeId =context.getProvidedObjectManager().createProvidedObject("Generated Process Tree", pTree, ProcessTree.class, context);
		}
		
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
			// System.out.println("neg after update is "+ parameters.getNegWeight());
			dfMatrix.updateCardinality(0, parameters.getExistWeight());
			dfMatrix.updateCardinality(1, parameters.getPosWeight());
			dfMatrix.updateCardinality(2, parameters.getNegWeight());

		}
		
		parameters.setType(newParameters.getType());
		
		if(parameters.getType() == ViewType.ProcessTree) {
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
		}else if(parameters.getType() == ViewType.PetriNetWithLTDependency) {
			showPetriNetWithLT();
			if(ltnetId == null) {
				ltnetId =context.getProvidedObjectManager().createProvidedObject("Petri net with LT", manet.getNet(), Petrinet.class, context);
				markingId =context.getProvidedObjectManager().createProvidedObject("Initial Marking with LT", manet.getInitialMarking(), Marking.class, context);
				
			}else {
				context.getProvidedObjectManager().changeProvidedObjectObject(ltnetId, manet.getNet());
				context.getProvidedObjectManager().changeProvidedObjectObject(markingId, manet.getInitialMarking());
			}
			
		}
			
	}
	
	@SuppressWarnings("deprecation")
	private void showPetriNetWithLT() {
		// TODO input is process tree and output is the petri net with long-term dependency
		// one way is to generate the process tree, because we need it all the time
		// but if we generate the petri net without lt, we can choose it 
		if(updateAll) {
			dfg =  dfMatrix.buildDfs();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);

			// the steps to change is : 
			/* Initial Process Tree generated
			 * Check have anet and manet
			 * Change threshold for a new process tree
			 * now we want to have the new anet and manet for it
			 *   ++ we should have each update value for each of them, and then update them?? Somehow?? 
			 *   ++ because if we change the 
			 *   the easy way to do it --- generate it each time
			 */
			// here we need to use the customized program to add lt dependency on it
			// LTDependencyDetector detector = new LTDependencyDetector(pTree, log);
			NewLTDetector detector = new NewLTDetector(pTree, log);
			PetrinetWithMarkings mnet = detector.buildPetrinetWithLT(log, pTree, parameters);
			// PetrinetWithMarkings mnet = LTDependencyDetector.buildPetrinetWithLT(log, pTree, parameters );
			manet = new AcceptingPetriNetImpl(mnet.petrinet, mnet.initialMarking, mnet.finalMarking);

		}else {
			if(pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}	
			// LTDependencyDetector detector = new LTDependencyDetector(pTree, log);
			NewLTDetector detector = new NewLTDetector(pTree, log);
			PetrinetWithMarkings mnet = detector.buildPetrinetWithLT(log, pTree, parameters);//LTDependencyDetector.buildPetrinetWithLT(log, pTree, parameters);
			manet = new AcceptingPetriNetImpl(mnet.petrinet, mnet.initialMarking, mnet.finalMarking);
			
		}// we need to set another parameter to store it 
		
		leftView.drawResult(context, manet.getNet());
		leftView.updateUI();
	}

	private boolean isWeightUpdated(ControlParameters para, ControlParameters newPara) {
		if(para.getExistWeight() == newPara.getExistWeight() 
				&& para.getPosWeight() == newPara.getPosWeight()
				&& para.getNegWeight() == newPara.getNegWeight())
			return false;
		return true;
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
				e.printStackTrace();
			} catch (InvalidProcessTreeException e) {
				e.printStackTrace();
			}

		}else{
			if(pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}	
			try {
				PetrinetWithMarkings net = ProcessTree2Petrinet.convert(pTree, true);
				anet = new AcceptingPetriNetImpl(net.petrinet, net.initialMarking, net.finalMarking);
			} catch (NotYetImplementedException e) {
				e.printStackTrace();
			} catch (InvalidProcessTreeException e) {
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
