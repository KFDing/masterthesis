package org.processmining.incorporatenegativeinformation.dialogs.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.incorporatenegativeinformation.parameters.LabelParameters;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class LabelParameterUI extends JPanel {
	// we create a table layout for setting parameters
	public LabelParameterUI(LabelParameters paras) {
		// set the total layout for this
		double size[][] = { { 0.25, 0.25, 20, 0.25, 0.25 },
				{ 0.1, 0.1, TableLayoutConstants.FILL, 0.05, 0.1, TableLayoutConstants.FILL, 0.05 } };
		this.setLayout(new TableLayout(size));
		this.setSize(600, 600);
		this.setOpaque(false);

		this.add(new JLabel("setting parameters fit trace variants"), "0,0,1,0,l,t");
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
					fo_value.setText(" " + value);
					paras.setFit_overlap_rate(value / 100);
				}
			}
		});
		fo_slider.setOpaque(false);
		// fo_slider.setToolTipText("<html>set the overlap parameters</html>");

		this.add(fo_label, "0,1");
		this.add(fo_value, "1,1");
		this.add(fo_slider, "0,2,1,2");

		// set for pos and neg distribution
		JLabel fp_label = new JLabel("Positive Rate: ");
		JSlider fp_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		fp_slider.setUI(new SlickerSliderUI(fp_slider));
		JTextField fp_value = new JTextField();

		fp_slider.setValue(0);
		fp_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == fp_slider) {
					// updateThresholdSlider();
					double value = fp_slider.getValue();
					fp_value.setText(" " + value);
					paras.setFit_pos_rate(value / 100);
				}
			}
		});
		fp_slider.setOpaque(false);

		this.add(fp_label, "0,4");
		this.add(fp_value, "1,4");
		this.add(fp_slider, "0,5,1,5");

		// for unfit traces
		this.add(new JLabel("setting parameters unfit trace variants"), "3,0,4,0,l,t");
		JLabel ufo_label = new JLabel("Unfit Overlap Rate: ");
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
					ufo_value.setText(" " + value);
					paras.setUnfit_overlap_rate(value / 100);
				}
			}
		});
		ufo_slider.setOpaque(false);
		// fo_slider.setToolTipText("<html>set the overlap parameters</html>");

		this.add(ufo_label, "3,1");
		this.add(ufo_value, "4,1");
		this.add(ufo_slider, "3,2,4,2");

		JLabel ufp_label = new JLabel("Positive Rate: ");
		JSlider ufp_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		ufp_slider.setUI(new SlickerSliderUI(ufp_slider));
		JTextField ufp_value = new JTextField();

		ufp_slider.setValue(0);
		ufp_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == ufp_slider) {
					// updateThresholdSlider();
					double value = ufp_slider.getValue();
					ufp_value.setText(" " + value);
					paras.setUnfit_pos_rate(value / 100);
				}
			}
		});
		ufp_slider.setOpaque(false);

		this.add(ufp_label, "3,4");
		this.add(ufp_value, "4,4");
		this.add(ufp_slider, "3,5,4,5");

	}
	/*
	 * public static void main(final String[] args) {
	 * SwingUtilities.invokeLater(new Runnable() { public void run() {
	 * LabelParameters paras = new LabelParameters(); LabelParameterUI gui = new
	 * LabelParameterUI(paras); gui.setOpaque(true);
	 * 
	 * JFrame frame = new JFrame("Setting Parameters");
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 * frame.setContentPane(gui); frame.pack(); frame.setVisible(true);
	 * System.out.println(paras.getFit_overlap_rate());
	 * System.out.println(paras.getFit_pos_rate()); } }); }
	 */
}
