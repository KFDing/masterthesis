package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.ding.process.dfg.model.ControlParameters;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration;
import org.processmining.plugins.ding.process.dfg.model.ProcessConfiguration.ViewType;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

public class ResultRightControlView extends JPanel {
	private static final long serialVersionUID = 1L;

	ControlParameters parameters;
	JLabel existLabel;
	JSlider existSlider;
	JLabel existValueLabel;

	JLabel posLabel;
	JSlider posSlider;
	JLabel posValueLabel;

	JLabel negLabel;
	JSlider negSlider;
	JLabel negValueLabel;

	RelativeLayout rl;
	JRadioButton ptButton;
	JRadioButton pnButton;
	JRadioButton ltpnButton;

	JButton submit_button;
	JButton saveModelBtn;
	JComboBox saveModelCombox;

	AddPairPanel addPairPanel;
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;

	// to initialze the panel 
	public ResultRightControlView() {
		parameters = new ControlParameters();
		rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);

		ptButton = new JRadioButton("Show Process Tree");
		ptButton.setSelected(true);
		ptButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if (ptButton.isSelected()) {
					parameters.setType(ViewType.ProcessTree);
					// block the choice of add Panel, but how to show it again?? Set not editable
					addPairPanel.setPanelEnabled(addPairPanel, false);
				}
			}
		});

		pnButton = new JRadioButton("Show Petri net");
		pnButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if (pnButton.isSelected()) {
					parameters.setType(ViewType.PetriNet);
					addPairPanel.setPanelEnabled(addPairPanel, false);
				}
			}
		});

		ltpnButton = new JRadioButton("Show Petri net with lt");
		ltpnButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if (ltpnButton.isSelected()) {
					parameters.setType(ViewType.PetriNetWithLTDependency);
					addPairPanel.setPanelEnabled(addPairPanel, true);
					// but then how to store the values here?? We need to change it here, maybe
				}
			}
		});

		ButtonGroup typeGroup = new ButtonGroup();

		typeGroup.add(ptButton);
		typeGroup.add(pnButton);
		typeGroup.add(ltpnButton);

		this.add(ptButton, new Float(3));
		this.add(pnButton, new Float(3));
		this.add(ltpnButton, new Float(3));

		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		JPanel weightPanel = new JPanel();
		weightPanel.setBorder(BorderFactory.createTitledBorder(raisedetched, "Set Weights"));
		weightPanel.setOpaque(false);
		weightPanel.setLayout(new RelativeLayout(RelativeLayout.X_AXIS));
		// choose for which type we want to show, one is the dfg, one is process mining.
		// but still one problem, how to change the Process Tree into Petri net , and let people know we focus one ProcessTree Panel

		// create for the existing weight setting 
		JPanel existPanel = new JPanel();
		existPanel.setLayout(new BoxLayout(existPanel, BoxLayout.Y_AXIS));
		existPanel.setOpaque(false);

		existLabel = new JLabel("<html>Weight for<br/>Existing Model</html>", SwingConstants.CENTER);
		existLabel.setOpaque(false);
		existLabel.setForeground(COLOR_FG);
		existLabel.setFont(this.smallFont);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		// weightPanel.add(rl.packVerticallyCentered(existLabel, 50, 20), BorderLayout.NORTH);
		existPanel.add(existLabel);

		existValueLabel = new JLabel();
		existValueLabel.setOpaque(false);
		existValueLabel.setForeground(COLOR_FG);
		existValueLabel.setFont(this.smallFont);
		existValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);

		existSlider = new JSlider(JSlider.VERTICAL, 0, ProcessConfiguration.WEIGHT_RANGE,
				ProcessConfiguration.WEIGHT_VALUE);
		existSlider.setUI(new SlickerSliderUI(existSlider));
		existSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				if (e.getSource() == existSlider) {
					// updateThresholdSlider();
					double existWeight = 1.0 * existSlider.getValue() / ProcessConfiguration.WEIGHT_VALUE;
					existValueLabel.setText(" " + existWeight);
					parameters.setExistWeight(existWeight);

				}
			}
		});
		existSlider.setOpaque(false);
		existSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		existPanel.add(existSlider);
		existPanel.add(existValueLabel);

		// set for pos weight
		JPanel posPanel = new JPanel();
		posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));
		posPanel.setOpaque(false);

		posLabel = new JLabel("<html>Weight for<br/>Pos Examples</html>", SwingConstants.CENTER);
		posLabel.setOpaque(false);
		posLabel.setForeground(COLOR_FG);
		posLabel.setFont(this.smallFont);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		posPanel.add(posLabel);

		posValueLabel = new JLabel();
		posValueLabel.setOpaque(false);
		posValueLabel.setForeground(COLOR_FG);
		posValueLabel.setFont(this.smallFont);
		posValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);
		// I want to get the listener out of this method .. or to create the specific class for it 
		posSlider = new JSlider(JSlider.VERTICAL, 0, ProcessConfiguration.WEIGHT_RANGE,
				ProcessConfiguration.WEIGHT_VALUE);
		posSlider.setUI(new SlickerSliderUI(posSlider));
		posSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				double posWeight = 1.0 * posSlider.getValue() / ProcessConfiguration.WEIGHT_VALUE;
				posValueLabel.setText(" " + posWeight);
				parameters.setPosWeight(posWeight);

			}
		});
		posSlider.setOpaque(false);
		posSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		posPanel.add(posSlider);
		posPanel.add(posValueLabel);

		// create for negative weight setting 
		JPanel negPanel = new JPanel();
		negPanel.setLayout(new BoxLayout(negPanel, BoxLayout.Y_AXIS));
		negPanel.setOpaque(false);

		negLabel = new JLabel("<html>Weight for<br/>Neg Examples</html>", SwingConstants.CENTER);
		negLabel.setOpaque(false);
		negLabel.setForeground(COLOR_FG);
		negLabel.setFont(this.smallFont);
		// rl.centerHorizontally(negLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		// weightPanel.add(rl.packVerticallyCentered(negLabel, 50, 20), BorderLayout.NORTH);
		// I want to get the listener out of this method .. or to create the specific class for it 
		negPanel.add(negLabel);

		negValueLabel = new JLabel();
		negValueLabel.setOpaque(false);
		negValueLabel.setForeground(COLOR_FG);
		negValueLabel.setFont(this.smallFont);
		negValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);

		negSlider = new JSlider(JSlider.VERTICAL, 0, ProcessConfiguration.WEIGHT_RANGE,
				ProcessConfiguration.WEIGHT_VALUE);
		negSlider.setUI(new SlickerSliderUI(negSlider));
		negSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				double negWeight = 1.0 * negSlider.getValue() / ProcessConfiguration.WEIGHT_VALUE;
				negValueLabel.setText(" " + negWeight);
				parameters.setNegWeight(negWeight);
			}
		});
		negSlider.setOpaque(false);
		negSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		negPanel.add(negSlider);
		negPanel.add(negValueLabel);

		weightPanel.add(existPanel, new Float(30));
		weightPanel.add(posPanel, new Float(30));
		weightPanel.add(negPanel, new Float(30));

		// add the submit and reset button panel
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		submit_button = new JButton("Submit");
		submit_button.setBounds(100, 100, 140, 40);

		JButton reset_button = new JButton("Reset");
		reset_button.setBounds(100, 100, 140, 40);
		reset_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				existSlider.setValue(ProcessConfiguration.WEIGHT_VALUE);
				existValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);

				posSlider.setValue(ProcessConfiguration.WEIGHT_VALUE);
				posValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);

				negSlider.setValue(ProcessConfiguration.WEIGHT_VALUE);
				negValueLabel.setText(ProcessConfiguration.DEFAULT_WEIGHT);
				parameters.resetValue();
			}
		});

		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(reset_button);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(submit_button);

		// here we need a panel to choose the xor block for adding lt-dependency. 
		addPairPanel = new AddPairPanel();
		addPairPanel.setVisible(true);
		addPairPanel.setPanelEnabled(addPairPanel, false);

		JPanel savePanel = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		savePanel.setLayout(gridBagLayout);

		savePanel.setBorder(new TitledBorder(null, "Save Model", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		saveModelBtn = new JButton("Save Model To ProM");
		GridBagConstraints gbc_saveModelBtn = new GridBagConstraints();
		gbc_saveModelBtn.insets = new Insets(0, 0, 0, 5);
		gbc_saveModelBtn.gridx = 0;
		gbc_saveModelBtn.gridy = 2;
		savePanel.add(saveModelBtn, gbc_saveModelBtn);

		saveModelCombox = new JComboBox(ProcessConfiguration.SaveModelType);
		GridBagConstraints gbc_saveModelCombox = new GridBagConstraints();
		gbc_saveModelCombox.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveModelCombox.gridx = 1;
		gbc_saveModelCombox.gridy = 2;
		savePanel.add(saveModelCombox, gbc_saveModelCombox);

		this.add(weightPanel, new Float(40));
		this.add(buttonPane, new Float(5));
		this.add(addPairPanel, new Float(40));
		this.add(savePanel, new Float(5));

	}

	public int getSaveModelIndex() {
		return saveModelCombox.getSelectedIndex();
	}

	public ControlParameters getParameters() {
		return parameters;
	}

	public void setParameters(ControlParameters paras) {
		parameters = paras;
	}

	public JButton getSubmitButton() {
		return submit_button;
	}

	public AddPairPanel getAddPairPanel() {
		return addPairPanel;
	}

	// create the Slider and JLabel together as one component 
	private JPanel createSliderLabel() {
		JPanel slJPanel = new JPanel();
		// create weight setting 
		JLabel valueLabel = new JLabel();
		valueLabel.setOpaque(false);
		valueLabel.setForeground(COLOR_FG);
		valueLabel.setFont(this.smallFont);
		valueLabel.setText("weight");
		// rl.centerHorizontally(valueLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		slJPanel.add(rl.packVerticallyCentered(existLabel, 50, 20), BorderLayout.NORTH);

		final JSlider valueSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		valueSlider.setUI(new SlickerSliderUI(existSlider));
		valueSlider.setValue(0);

		valueSlider.setOpaque(false);
		valueSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		slJPanel.add(existSlider, BorderLayout.CENTER);

		return slJPanel;
	}
	// to get the parameter here
}
