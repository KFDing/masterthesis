package org.processmining.plugins.ding.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.ding.preprocess.LabelParameters;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;


public class LabelParameterUI extends JPanel{
	// we create a table layout for setting parameters
	public LabelParameterUI( LabelParameters paras) {
		JPanel controlPanel = new JPanel();
		
		// set the total layout for controlPanel
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL},
				{ 30, TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		controlPanel.setLayout(new TableLayout(size));
		controlPanel.setOpaque(false);
		
		controlPanel.add(new JLabel("setting parameters for labeling"), "0,0");
		
		controlPanel.add(new JLabel("setting parameters fit trace variants"), "0,0");
		// for fit variants and overlap
		JLabel fo_label = new JLabel("Overlap Rate: ");
		JSlider fo_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		fo_slider.setUI(new SlickerSliderUI(fo_slider));
		JTextField fo_value = new JTextField();
		
		fo_slider.setValue(0);
		fo_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == fo_slider) {
					// updateThresholdSlider();
					double value = fo_slider.getValue();
					fo_value.setText(" "+ value);
				}	
			}
		});
		fo_slider.setOpaque(false);
		// fo_slider.setToolTipText("<html>set the overlap parameters</html>");
		
		controlPanel.add(fo_label, "0,1");
		controlPanel.add(fo_slider, "0,1");
		controlPanel.add(fo_value,"0,1");
		
		// set for pos and neg distribution
		JLabel fp_label = new JLabel("Overlap Rate: ");
		JSlider fp_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		fo_slider.setUI(new SlickerSliderUI(fp_slider));
		JTextField fp_value = new JTextField();
		
		fp_slider.setValue(0);
		fp_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == fp_slider) {
					// updateThresholdSlider();
					double value = fp_slider.getValue();
					fp_value.setText(" "+ value);
				}	
			}
		});
		fp_slider.setOpaque(false);
		
		controlPanel.add(fp_label, "0,2");
		controlPanel.add(fp_slider, "0,2");
		controlPanel.add(fp_value,"0,2");
		
		// for unfit traces
		controlPanel.add(new JLabel("setting parameters unfit trace variants"), "1,0");
		JLabel ufo_label = new JLabel("Overlap Rate: ");
		JSlider ufo_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		ufo_slider.setUI(new SlickerSliderUI(ufo_slider));
		JTextField ufo_value = new JTextField();
		
		ufo_slider.setValue(0);
		ufo_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == ufo_slider) {
					// updateThresholdSlider();
					double value = ufo_slider.getValue();
					ufo_value.setText(" "+ value);
				}	
			}
		});
		ufo_slider.setOpaque(false);
		// fo_slider.setToolTipText("<html>set the overlap parameters</html>");
		
		controlPanel.add(ufo_label, "1,1");
		controlPanel.add(ufo_slider, "1,1");
		controlPanel.add(ufo_value,"1,1");
		
		
		JLabel ufp_label = new JLabel("Unfit Overlap Rate: ");
		JSlider ufp_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		ufo_slider.setUI(new SlickerSliderUI(fp_slider));
		JTextField ufp_value = new JTextField();
		
		ufp_slider.setValue(0);
		ufp_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == ufp_slider) {
					// updateThresholdSlider();
					double value = ufp_slider.getValue();
					ufp_value.setText(" "+ value);
				}	
			}
		});
		ufp_slider.setOpaque(false);
		
		controlPanel.add(ufp_label, "1,2");
		controlPanel.add(ufp_slider, "1,2");
		controlPanel.add(ufp_value,"1,2");
	}
	
	public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	LabelParameters paras = new LabelParameters();
            	LabelParameterUI gui = new LabelParameterUI(paras);
            	gui.setOpaque(true);
            	
            	JFrame frame = new JFrame("Setting Parameters");
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            	frame.setContentPane(gui);
            	frame.add(new JLabel("nothing happen"));
            	frame.pack();
            	frame.setVisible(true);
            }
        });
    }
	
}
