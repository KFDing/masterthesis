package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
import org.processmining.plugins.ding.process.dfg.model.LTRule;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration.ViewType;
import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.plugins.ding.process.dfg.model.XORClusterPair;
import org.processmining.plugins.ding.process.dfg.train.NewLTDetector;
import org.processmining.plugins.ding.process.dfg.transform.NewXORPairGenerator;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
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
	
	// here we put the generator here to initialize the values at first time, when we have it 
	// then not again!!
	NewXORPairGenerator<ProcessTreeElement> generator = null;
	NewLTDetector detector =null;// = new NewLTDetector(pTree, log);
	
	
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
		
		// we get control from exposing the JComponent from controlView
		// if we also expose the addPairPanel Part, then we can have the remove and add performance here?? 
		// The addPair is only used to show the result, that's all... 
		AddPairPanel addPairPanel = rightView.getAddPairPanel();
		// if we expose its structure here, actually, we can get the parameter directly from it..
		// should we do it ?? Or not ?? We need to decide it maybe here
		// at first, add all is chosen, but the action can be added here directly.
		// after it get some movements from addPairPanel, then we need to do it ??
		
		
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

		}else {
			if(pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}	
			
		}
		// let it return the acceptionNet, so we don't need to struggle about it here
		createPNWithLT();
		manet = detector.getAcceptionPN();
		
		leftView.drawResult(context, manet.getNet());
		leftView.updateUI();
	}
	
	private void createPNWithLT() {

		initialize();
		// in other mode, we need to define it another function
		List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
		List<LTRule<XORCluster<ProcessTreeElement>>> connSet = generator.getAllLTConnection();
		// generate all the pairs here 
		detector.addLTOnPairList(clusterPairs, connSet);
		
	}
	
	// here, given petri net with lt already, the chosen pair to add there already in another action there, 
	// but we need to adapt the view here
	private void addPairLTOnPN(XORClusterPair<ProcessTreeElement> pair) {
		List<XORClusterPair<ProcessTreeElement>> clusterPairs = new ArrayList<>();
		clusterPairs.add(pair);
		List<LTRule<XORCluster<ProcessTreeElement>>> connSet = pair.getConnection();
		
		detector.addLTOnPairList(clusterPairs, connSet);
		manet = detector.getAcceptionPN();
		
		leftView.drawResult(context, manet.getNet());
		leftView.updateUI();
		
	}
	
	// here, given petri net with lt already, the chosen pair to add
	private void rmPairLTOnPN(PetrinetWithMarkings mnet, XORClusterPair<ProcessTreeElement> pair) {
		detector.rmLTOnSinglePair(pair);
		manet = detector.getAcceptionPN();
		
		leftView.drawResult(context, manet.getNet());
		leftView.updateUI();
	}
	
	private void initialize() {
		
		generator = new NewXORPairGenerator<ProcessTreeElement>();
		generator.initialize(pTree);
		
		detector = new NewLTDetector(pTree, log, parameters);
		
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
