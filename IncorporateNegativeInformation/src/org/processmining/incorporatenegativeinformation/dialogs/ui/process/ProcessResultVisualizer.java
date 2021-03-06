package org.processmining.incorporatenegativeinformation.dialogs.ui.process;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.incorporatenegativeinformation.algorithms.NewLTDetector;
import org.processmining.incorporatenegativeinformation.algorithms.NewXORPairGenerator;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration.ActionType;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration.ViewType;
import org.processmining.incorporatenegativeinformation.models.DfMatrix;
import org.processmining.incorporatenegativeinformation.models.DfgProcessResult;
import org.processmining.incorporatenegativeinformation.models.LTRule;
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.incorporatenegativeinformation.models.XORClusterPair;
import org.processmining.incorporatenegativeinformation.parameters.ControlParameters;
import org.processmining.incorporatenegativeinformation.plugins.EvaluateResult;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.dialogs.IMdMiningDialog;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

/**
 * this class is built as a visualizer for process tree to display, but it can
 * also display the dfg; the I should change the name of this class into
 * ProcessResultVisualizer??
 * 
 * @author dkf
 *
 */
@Plugin(name = "Show Process Result from Dfg", parameterLabels = { "DfgProcess Result" }, returnLabels = {
		"JPanel" }, returnTypes = { JPanel.class })
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
class ResultMainView extends JPanel {

	private static final long serialVersionUID = 0L;
	RelativeLayout rl;
	ResultLeftView leftView;
	ResultRightControlView rightView;
	AddPairPanel addPairPanel;

	ControlParameters parameters;
	UIPluginContext context;
	XLog log;
	DfMatrix dfMatrix;
	Dfg dfg = null;
	ProcessTree pTree = null;
	AcceptingPetriNet anet = null;
	AcceptingPetriNet manet = null;
	AcceptingPetriNet danet = null;
	// here we put the generator here to initialize the values at first time, when we have it 
	// then not again!!
	NewXORPairGenerator<ProcessTreeElement> generator = null;
	NewLTDetector detector = null;// = new NewLTDetector(pTree, log);

	ProvidedObjectID ltnetId = null;
	ProvidedObjectID rnetId = null;
	ProvidedObjectID pTreeId = null;
	ProvidedObjectID netId = null;
	ProvidedObjectID markingId = null;
	ProvidedObjectID rmarkingId = null;
	boolean updateAll = true;

	public ResultMainView(UIPluginContext context, DfgProcessResult dfgResult) {
		this.context = context;
		log = dfgResult.getLog();
		dfMatrix = dfgResult.getDfMatrix();
		parameters = new ControlParameters();

		rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill(true);
		this.setLayout(rl);
		this.setBackground(new Color(240, 240, 240));

		leftView = new ResultLeftView();
		rightView = new ResultRightControlView();

		parameters.cloneValues(rightView.getParameters());

		dfMatrix.updateCardinality(0, parameters.getExistWeight());
		dfMatrix.updateCardinality(1, parameters.getPosWeight());
		dfMatrix.updateCardinality(2, parameters.getNegWeight());

		showProcessTree();

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

		addPairPanel = rightView.getAddPairPanel();
		// after we have chosen add or remove button, then 
		addPairPanel.addAllBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO  after this, we need to show all the PN with LT
				rightView.getParameters().setAddAllPair(true);
				addPairPanel.choosePanel.setEnabled(false);
				if (rightView.getParameters().getType() == ViewType.PetriNetWithLTDependency)
					showPetriNetWithLT();
				else if (rightView.getParameters().getType() == ViewType.ReducedPetriNet)
					showReducedPetriNet();

			}
		});

		addPairPanel.chooseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// if this is selected, then we show the next Component, 
				// we can put them into one Panel, or we jsut set them invisible??? 
				// I think it is better to put them into one Jpanel
				if (addPairPanel.chooseBtn.isSelected()) {
					// so it still shows but different color
					rightView.getParameters().setType(ViewType.PetriNetWithLTDependency);
					rightView.getParameters().setAddAllPair(false);

					addPairPanel.choosePanel.setEnabled(true);
					// addPairPanel.choosePanel.setVisible(true);
					// at same time, we need to reset the petri net into original
					// we need to update the source of add and remove, but one thing is to reset them again
					System.out.println("press choose panel...");
					// here we need to reset all the pairs and make them not showing in the graph
					resetPNWithLT();
					addPairPanel.updateAddSource(generator.getAddAvailableSources());
					addPairPanel.updateRMSource(generator.getRMAvailableSources());
				}
			}
		});
		// how to update the combobox values?? we need to give it outside, by using this,, it's all right
		// later, we can change it...

		// combox chosen pair
		addPairPanel.addSourceComboBox.addItemListener(new ItemListener() {
			// if we delete element from it, it listen to this, so we shouldn't do it
			public void itemStateChanged(ItemEvent e) {
				// TODO if source is chosen, then change the target choices
				if (e.getStateChange() == ItemEvent.SELECTED) {

					//					System.out.println("Size of items in change add target -- " + addPairPanel.addSourceComboBox.getItemCount());
					int sourceIdx = addPairPanel.addSourceComboBox.getSelectedIndex();
										System.out.println("Exception Add: source Idx " + sourceIdx);
										System.out.println(generator);
										System.out.println(generator.getAddAvailableSources().size());

					XORCluster<ProcessTreeElement> source = generator.getAddAvailableSources().get(sourceIdx);
					System.out.println("get the source for add lt pair");
					System.out.println(source.getLabel());
					updateAddTargetClusterList(source);
				}
			}
		});

		addPairPanel.rmSourceComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// System.out.println("Size of items in rm target" + addPairPanel.rmSourceComboBox.getItemCount());
					int sourceIdx = addPairPanel.rmSourceComboBox.getSelectedIndex();
					//					System.out.println("Exception Remove: source Idx" + sourceIdx);
					//					System.out.println(generator);
					//					System.out.println(generator.getRMAvailableSources().size());
					XORCluster<ProcessTreeElement> source = generator.getRMAvailableSources().get(sourceIdx);

					updateRMTargetClusterList(source);
				}
			}
		});

		addPairPanel.addPairBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO if it is pressed, then we need to set the values of controlparameters
				parameters.cloneValues(rightView.getParameters());
				parameters.setAction(ActionType.AddLTOnPair);

				int sourceIdx = addPairPanel.getAddSourceIndex();
				int targetIdx = addPairPanel.getAddTargetIndex();
				System.out.println("Index to add the values on it :" + sourceIdx + "target Idx : "+ targetIdx );
				XORCluster<ProcessTreeElement> source = generator.getAddAvailableSources().get(sourceIdx);
				XORCluster<ProcessTreeElement> target = generator.getAddAvailableTargets(source).get(targetIdx);

				// if it already exists, then we don't add it here.. but we already have checked before we have it 
				// we only show the availabel xor cluster for pair
				XORClusterPair<ProcessTreeElement> pair = generator.createClusterXORPair(source, target);
				addPairLTOnPN(pair);
			}
		});

		addPairPanel.rmPairBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				parameters.cloneValues(rightView.getParameters());
				parameters.setAction(ActionType.RemoveLTOnPair);
				int sourceIdx = addPairPanel.getRMSourceIndex();
				XORCluster<ProcessTreeElement> source = generator.getRMAvailableSources().get(sourceIdx);
				XORClusterPair<ProcessTreeElement> pair = generator.getPairBySource(source);
				//				// remove it form the clusterList in generator
				generator.getClusterPair().remove(pair);
				rmPairLTOnPN(pair);
			}
		});

		rightView.saveModelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// if we select it, then we will submit the current model to ProM
				// there are several types we need to choose,
				// default it is the petri net with lt
				int idx = rightView.getSaveModelIndex();
				try {
					// get the name for this model or by default of using it ?? 
					saveModel2ProM(idx);
				} catch (ProvidedObjectDeletedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		rightView.showCMBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ArrayList<Integer> confusion_matrix = null;
				// show the confusion matrix into leftView, we can call the draw method from leftview
				String title = "Ext: " + parameters.getExistWeight() + " ; Pos: " + parameters.getPosWeight()
						+ " ; Neg: " + parameters.getNegWeight();
				if (parameters.getType() == ViewType.PetriNet) {
					title += "; Without LT";
					confusion_matrix = EvaluateResult.naiveCheckPN(log, anet);
				} else if (parameters.getType() == ViewType.PetriNetWithLTDependency) {
					title += "; with LT";
					confusion_matrix = EvaluateResult.naiveCheckPN(log, manet);
				} else if (parameters.getType() == ViewType.ReducedPetriNet) {
					title += "; Reduced with LT";
					confusion_matrix = EvaluateResult.naiveCheckPN(log, danet);
				}
				// for the confusion_matrix I'd like to use the log variants for saving computation time
				// here one bug hiiden, we have two net, one is with LT, one without LT, so we need to test them here
				leftView.addConfusionMatrixView(confusion_matrix, title);
			}
		});

		this.add(this.leftView, new Float(75));
		this.add(Box.createVerticalGlue(), new Float(3));
		this.add(this.rightView, new Float(20));
	}

	private void saveModel2ProM(int idx) throws ProvidedObjectDeletedException {
		// TODO save model to ProM according to the chosen index
		// every time we should create a new one!! 
		switch (idx) {
			case 0 : // save petri net without lt

				rnetId = context.getProvidedObjectManager().createProvidedObject("Reduced Petri net with LT",
						danet.getNet(), Petrinet.class, context);
				rmarkingId = context.getProvidedObjectManager().createProvidedObject("Initial Marking",
						danet.getInitialMarking(), Marking.class, context);
				break;
			case 1 : // save petri net with lt

				ltnetId = context.getProvidedObjectManager().createProvidedObject("Petri net with LT", manet.getNet(),
						Petrinet.class, context);
				markingId = context.getProvidedObjectManager().createProvidedObject("Initial Marking with LT",
						manet.getInitialMarking(), Marking.class, context);
				break;
			case 2 : // save petri net without lt

				netId = context.getProvidedObjectManager().createProvidedObject("Generated Petri net", anet.getNet(),
						Petrinet.class, context);
				markingId = context.getProvidedObjectManager().createProvidedObject("Initial Marking",
						anet.getInitialMarking(), Marking.class, context);
				break;

			case 3 : // save process tree
				pTreeId = context.getProvidedObjectManager().createProvidedObject("Generated Process Tree", pTree,
						ProcessTree.class, context);
				break;

		}
	}

	public void updateMainView(ResultLeftView leftView, ControlParameters newParameters)
			throws ProvidedObjectDeletedException {
		// if there is only type changes, so we don't need to generate it again for the dfMatrix
		// we just use the dfg, process tree and petri net 
		// how to distinguish them?? I think I can add the weight to the DfMatrix, and compare it 
		// with the new ones, if sth changes, so we need to generate them again? If not changes, then 
		// we don't need to do it 
		// if we need to create new Dfg ?? 
		addPairPanel.addAllBtn.setSelected(true);
		updateAll = isWeightUpdated(parameters, newParameters);

		if (updateAll) {

			parameters.cloneValues(newParameters);
			// System.out.println("Parameter before update: " + parameters.getExistWeight()+":"+parameters.getPosWeight()+":"+ parameters.getNegWeight());

			// System.out.println("neg after update is "+ parameters.getNegWeight());
			dfMatrix.updateCardinality(0, parameters.getExistWeight());
			dfMatrix.updateCardinality(1, parameters.getPosWeight());
			dfMatrix.updateCardinality(2, parameters.getNegWeight());

		}

		parameters.setType(newParameters.getType());
		// System.out.println("ViewType: " + parameters.getType());
		if (parameters.getType() == ViewType.ProcessTree) {
			showProcessTree();
			// add process tree into result, but we need to keep there only one process tree there
		} else if (parameters.getType() == ViewType.PetriNet) {
			showPetriNet();
			// if we add new, we need to delete the old petri net 

		} else if (parameters.getType() == ViewType.PetriNetWithLTDependency) {
			showPetriNetWithLT();
			// only it is add all, then we add them all, else we can't have it without the addpairPanel..
			// anyway, we keep it here
			addPairPanel.setPanelEnabled(addPairPanel, true);
		} else if (parameters.getType() == ViewType.ReducedPetriNet) {
			// show the reduced petri net
			showReducedPetriNet();

			addPairPanel.setPanelEnabled(addPairPanel, true);
		}

	}

	private void showReducedPetriNet() {
		// TODO show the petri net with petri net after reducing silent transition
		// do you expect to recover them again?? Not really, I think... I don't want to review them again
		// I want to save time for it, but if we always delete it after, we can recover it, 
		// so another parameters in detectors

		if (updateAll) {
			dfg = dfMatrix.buildDfg();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);

		} else {
			if (pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}
			if (manet == null) {
				// if this is empty, we create it
				createPNWithLT();
				manet = detector.getAcceptionPN();

			}
		}
		// we accept the manet and generate a new one to show them here
		// the net is already with 
		// if we put the deletion there, what to do them?? acutally it is like reduced 
		Petrinet dnet = detector.getReducedPetriNet();
		danet = AcceptingPetriNetFactory.createAcceptingPetriNet(dnet);

		rightView.showCMBtn.setEnabled(true);
		leftView.drawResult(context, danet);
		leftView.updateUI();

	}

	@SuppressWarnings("deprecation")
	private void showPetriNetWithLT() {
		// TODO input is process tree and output is the petri net with long-term dependency
		// one way is to generate the process tree, because we need it all the time
		// but if we generate the petri net without lt, we can choose it 
		if (updateAll) {
			dfg = dfMatrix.buildDfg();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);

		} else {
			if (pTree == null) {
				DfgMiningParameters ptParas = getProcessTreParameters();
				pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
			}

		}
		// let it return the acceptionNet, so we don't need to struggle about it here
		createPNWithLT();
		manet = detector.getAcceptionPN();

		rightView.showCMBtn.setEnabled(true);
		leftView.drawResult(context, manet);
		leftView.updateUI();
	}

	private void initialize() {

		generator = new NewXORPairGenerator<ProcessTreeElement>();
		generator.initialize(pTree);

		detector = new NewLTDetector(pTree, log, parameters, dfMatrix.getStandardCardinality());
		detector.reset(null);
	}

	private void createPNWithLT() {

		initialize();
		// in other mode, we need to define it another function
		generator.buildAllPairInOrder();
		List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
		List<LTRule<XORCluster<ProcessTreeElement>>> connSet = generator.getAllLTConnection();
		// generate all the pairs here 
		if(clusterPairs.size()>0)
			detector.addLTOnPairList(clusterPairs, connSet);
		else {
			System.out.println("Not enough xors for long-term dependency");
		}
		// after this we can delete silent transition to make model simpler
		// but do we need it, or not?? 

	}

	protected void resetPNWithLT() {
		// TODO reset PN with LT, it is we show the graph without any lt
		// we can find out all the pair and then remove it 
		// if we use something simple, we can get the values quickly?? 
		// delete all the connection?? Or generate a new graph? 

		// it should show in the graph after chooseBtn..
		if (generator != null) {
			System.out.println("The number of clusterList before reset " + generator.getClusterPair().size());
			generator.resetSourceTargetMark();
			System.out.println("The number of clusterList after reset " + generator.getClusterPair().size());
		} else {
			generator = new NewXORPairGenerator<ProcessTreeElement>();
			generator.initialize(pTree);

		}
		// we go from adding all lt dependency to single one, the detector should be reset, but 
		// the maps and variants built before should not change! if the parameter keeps the same
		// so after reset, we should leep 
		
		detector = new NewLTDetector(pTree, log, parameters, dfMatrix.getStandardCardinality());
		
		// detector.reset(generator.getClusterPair());
		manet = detector.getAcceptionPN();

		leftView.drawResult(context, manet);
		leftView.updateUI();

	}

	// here, given petri net with lt already, the chosen pair to add there already in another action there, 
	// but we need to adapt the view here
	private void addPairLTOnPN(XORClusterPair<ProcessTreeElement> pair) {
		List<XORClusterPair<ProcessTreeElement>> clusterPairs = new ArrayList<>();
		clusterPairs.add(pair);
		List<LTRule<XORCluster<ProcessTreeElement>>> connSet = pair.getConnection();

		detector.addLTOnPairList(clusterPairs, connSet);
		manet = detector.getAcceptionPN();

		addPairPanel.updateAddSource(generator.getAddAvailableSources());
		addPairPanel.updateRMSource(generator.getRMAvailableSources());
		addPairPanel.repaint();

		if (parameters.getType() == ViewType.PetriNetWithLTDependency) {
			leftView.drawResult(context, manet);
			leftView.updateUI();
		} else if (parameters.getType() == ViewType.ReducedPetriNet) {
			Petrinet dnet = detector.getReducedPetriNet();
			danet = AcceptingPetriNetFactory.createAcceptingPetriNet(dnet);

			leftView.drawResult(context, danet);
			leftView.updateUI();
		}

	}

	// here, given petri net with lt already, the chosen pair to add
	private void rmPairLTOnPN(XORClusterPair<ProcessTreeElement> pair) {
		detector.rmLTOnSinglePair(pair);

		manet = detector.getAcceptionPN();
		// after this update the combox list of source and targets
		addPairPanel.updateAddSource(generator.getAddAvailableSources());
		addPairPanel.updateRMSource(generator.getRMAvailableSources());
		addPairPanel.repaint();

		if (parameters.getType() == ViewType.PetriNetWithLTDependency) {
			leftView.drawResult(context, manet);
			leftView.updateUI();
		} else if (parameters.getType() == ViewType.ReducedPetriNet) {
			Petrinet dnet = detector.getReducedPetriNet();
			danet = AcceptingPetriNetFactory.createAcceptingPetriNet(dnet);

			leftView.drawResult(context, danet);
			leftView.updateUI();
		}

	}

	// but one thing, when we chose source, the target should update accordingly.
	private void updateAddTargetClusterList(XORCluster<ProcessTreeElement> source) {
		List<XORCluster<ProcessTreeElement>> targets = generator.getAddAvailableTargets(source);
		addPairPanel.updateAddTarget(targets);
	}

	private void updateRMTargetClusterList(XORCluster<ProcessTreeElement> source) {
		List<XORCluster<ProcessTreeElement>> targets = generator.getRMAvailableTargets(source);
		addPairPanel.updateRMTarget(targets);
	}

	private boolean isWeightUpdated(ControlParameters para, ControlParameters newPara) {
		if (para.getExistWeight() == newPara.getExistWeight() && para.getPosWeight() == newPara.getPosWeight()
				&& para.getNegWeight() == newPara.getNegWeight())
			return false;
		return true;
	}

	private void showProcessTree() {
		if (updateAll) {
			dfg = dfMatrix.buildDfg();
			// I think I should change something about it, which could remember the result from before
			// so I could put the Dfg, ProcessTree and Petri net in the class
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
		} else if (pTree == null) {
			DfgMiningParameters ptParas = getProcessTreParameters();
			pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
		}
		rightView.showCMBtn.setEnabled(false);
		leftView.drawResult(pTree);
		leftView.updateUI();
	}

	@SuppressWarnings("deprecation")
	private void showPetriNet() {

		if (updateAll) {
			dfg = dfMatrix.buildDfg();
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

		} else {
			if (pTree == null) {
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
		rightView.showCMBtn.setEnabled(true);
		leftView.drawResult(context, anet);
		leftView.updateUI();
	}

	private DfgMiningParameters getProcessTreParameters() {
		IMdMiningDialog dialog = new IMdMiningDialog();
		dialog.setSize(100, 100);
		// it is a Jpanel, not a dialog, now I just want to get the pop up JPanel and read the input from it 
		// String parameters = JOptionPane.showInputDialog(this, "Set parameters fro IM", "Setting", JOptionPane.QUESTION_MESSAGE);
		JOptionPane.showMessageDialog(this, dialog, "Setting Miner Parameters for Dfg",
				JOptionPane.INFORMATION_MESSAGE);
		DfgMiningParameters parameters = dialog.getMiningParameters();
		// System.out.println("Paras " + parameters.getNoiseThreshold());     

		return parameters;
	}

}
