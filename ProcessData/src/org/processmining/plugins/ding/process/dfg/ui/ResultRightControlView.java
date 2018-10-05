package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.ding.process.dfg.model.Configuration;
import org.processmining.plugins.ding.process.dfg.model.Configuration.ViewType;
import org.processmining.plugins.ding.process.dfg.model.ControlParameters;

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
	JRadioButton dfgButton;
	JRadioButton ptButton;
	JRadioButton pnButton;
	
	JButton submit_button;
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// to initialze the panel 
	public ResultRightControlView() {
		parameters = new ControlParameters();
		rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(COLOR_BG2);
		
		dfgButton = new JRadioButton("Show Dfg View");
		dfgButton.setSelected(true);
		dfgButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if(dfgButton.isSelected()) {
					parameters.setType(ViewType.Dfg);
				}
			}
		});
		ptButton = new JRadioButton("Show Process Tree");
		ptButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if(ptButton.isSelected()) {
					parameters.setType(ViewType.ProcessTree);
				}
			}
		});
		
		pnButton = new JRadioButton("Show Petri net");
		pnButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// after we choose delete, we delete places and repaint the graph again
				if(pnButton.isSelected()) {
					parameters.setType(ViewType.PetriNet);
				}
			}
		});
		
		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(dfgButton);
		typeGroup.add(ptButton);
		typeGroup.add(pnButton);
		
		this.add(dfgButton, new Float(5));
		this.add(ptButton, new Float(5));
		this.add(pnButton, new Float(5));
		
	
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
		existValueLabel.setText(Configuration.DEFAULT_WEIGHT);
		
		existSlider = new JSlider(JSlider.VERTICAL, 0, Configuration.WEIGHT_RANGE, Configuration.WEIGHT_VALUE);
		existSlider.setUI(new SlickerSliderUI(existSlider));
		existSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				if (e.getSource() == existSlider) {
					// updateThresholdSlider();
					double existWeight = 1.0*existSlider.getValue()/Configuration.WEIGHT_VALUE;
					existValueLabel.setText(" "+ existWeight);
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
		posValueLabel.setText(Configuration.DEFAULT_WEIGHT);
		// I want to get the listener out of this method .. or to create the specific class for it 
		posSlider = new JSlider(JSlider.VERTICAL, 0, Configuration.WEIGHT_RANGE, Configuration.WEIGHT_VALUE);
		posSlider.setUI(new SlickerSliderUI(posSlider));
		posSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				double posWeight = 1.0*posSlider.getValue()/Configuration.WEIGHT_VALUE;
				posValueLabel.setText(" "+ posWeight);
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
		negValueLabel.setText(Configuration.DEFAULT_WEIGHT);
		
		negSlider = new JSlider(JSlider.VERTICAL, 0, Configuration.WEIGHT_RANGE, Configuration.WEIGHT_VALUE);
		negSlider.setUI(new SlickerSliderUI(negSlider));
		negSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				double negWeight = 1.0*negSlider.getValue()/ Configuration.WEIGHT_VALUE;
				negValueLabel.setText(" "+ negWeight);
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
        buttonPane.setBackground(COLOR_BG2);
        
		submit_button=new JButton("Submit");    
		submit_button.setBounds(100,100,140, 40);    
		
		        	
		JButton reset_button=new JButton("Reset");    
		reset_button.setBounds(100,100,140, 40);    
		reset_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				existSlider.setValue(Configuration.WEIGHT_VALUE);
				existValueLabel.setText(Configuration.DEFAULT_WEIGHT);
				
				posSlider.setValue(Configuration.WEIGHT_VALUE);
				posValueLabel.setText(Configuration.DEFAULT_WEIGHT);
				
				negSlider.setValue(Configuration.WEIGHT_VALUE);
				negValueLabel.setText(Configuration.DEFAULT_WEIGHT);
				parameters.resetValue();
			}          
	    });
		
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(reset_button);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(submit_button);
        
        this.add(weightPanel, new Float(50));
        this.add(buttonPane, new Float(10));
        this.add(Box.createRigidArea(new Dimension(50, 100)));
	}
	
	public ControlParameters getParameters() {
		
		return parameters;
	}
	
	public void setParameters(ControlParameters paras) {
		parameters =  paras;
	}
	
	public JButton getSubmitButton() {
		return submit_button;
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
		/*
		valueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == valueSlider) {
					// updateThresholdSlider();
					existLabel.setText(" "+ valueSlider.getValue());
					// parameters.setExistWeight(existWeight);
					valueSlider.getParent().dispatchEvent(e);
				}	
			}
		});
		*/
		valueSlider.setOpaque(false);
		valueSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		slJPanel.add(existSlider, BorderLayout.CENTER);
				
		return slJPanel;
	}
	// to get the parameter here
}
