package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
/**
 * this class is used to accept the parameters and pass them to the main view
 * @author dkf
 *
 */
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.ding.model.ControlParameters;
import org.processmining.plugins.ding.train.Configuration;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;
public class ResultRightControlView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	ControlParameters parameters;
	final JLabel existLabel;
	final JSlider existSlider;
	
	final JLabel posLabel;
	final JSlider posSlider;
	RelativeLayout rl;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// to initialze the panel 
	public ResultRightControlView() {
		rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		this.setBackground(Configuration.COLOR_BG2);
	
		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		JPanel weightPanel = new JPanel();
		weightPanel.setBorder(BorderFactory.createTitledBorder(raisedetched, "Set Weights"));
		weightPanel.setOpaque(false);
		weightPanel.setLayout(new BorderLayout());
		
		// create for the existing weight setting 
		existLabel = new JLabel();
		existLabel.setOpaque(false);
		existLabel.setForeground(COLOR_FG);
		existLabel.setFont(this.smallFont);
		existLabel.setText("Weight for Existing Model");
		rl.centerHorizontally(existLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		weightPanel.add(rl.packVerticallyCentered(existLabel, 50, 20), BorderLayout.NORTH);
		
		existSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		existSlider.setUI(new SlickerSliderUI(existSlider));
		existSlider.setValue(0);
		existSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == existSlider) {
					// updateThresholdSlider();
					double existWeight = existSlider.getValue();
					existLabel.setText(" "+ existWeight);
					parameters.setExistWeight(existWeight);
				}	
			}
		});
		existSlider.setOpaque(false);
		existSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		weightPanel.add(existSlider, BorderLayout.CENTER);
		
		// set for pos weight
		posLabel = new JLabel();
		posLabel.setOpaque(false);
		posLabel.setForeground(COLOR_FG);
		posLabel.setFont(this.smallFont);
		posLabel.setText("Weight for Existing Model");
		rl.centerHorizontally(existLabel);
		// here we add Label to south, but it shoudl be in JPanel called upperControlPanel, I think !! 
		weightPanel.add(rl.packVerticallyCentered(existLabel, 50, 20), BorderLayout.NORTH);
		// I want to get the listener out of this method .. or to create the specific class for it 
		posSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		posSlider.setUI(new SlickerSliderUI(posSlider));
		posSlider.setValue(0);
		posSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == posSlider) {
					// updateThresholdSlider();
					double posWeight = posSlider.getValue();
					posLabel.setText(" "+ posWeight);
					parameters.setPosWeight(posWeight);
				}	
			}
		});
		posSlider.setOpaque(false);
		posSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		weightPanel.add(posSlider, BorderLayout.CENTER);
		
		// create for negative weight setting 
		JPanel negWeightPanel = createSliderLabel();
		
	}
	
	public class MySuperCoolComponent extends JPanel {
		
		
		private void updateMyVisualization(ChangeEvent e) {
			// doing something with object o
		}
	}
	
	public class MySuperCoolSlider extends JSlider {
		
		private final MySuperCoolComponent parent;
		
		public MySuperCoolSlider(MySuperCoolComponent comp) {
			parent = comp;
			this.addChangeListener(new ChangeListener() {
				
				public void stateChanged(ChangeEvent e) {
					parent.updateMyVisualization(e);
					
				}
			});
		}
		
		public void somewhereInKefangsCode() {
			MySuperCoolComponent component = new MySuperCoolComponent();
			MySuperCoolSlider slider = new MySuperCoolSlider(component);
		}
		
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
