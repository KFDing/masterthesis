package org.processmining.incorporatenegativeinformation.dialogs.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import info.clearthought.layout.TableLayout;

/**
 * this class shows the summary information about the each variant -- accept the
 * change parameters to control variant -- but how to change them then??? we
 * don't need to build a Panel each time, when we see different variant we only
 * need to change the data, it shows and also pass parameters from it to achieve
 * this, we need to ?? ++ create an initial panel at the initialization of
 * construct methods ++ update the values of it.. One thing is how to really get
 * access into this panel ++ create all the variant into this class and
 * initialize them into the construct methods ++ update methods can also use it,
 * but for each variant, it need s to change the view data, but only for this.
 * 
 * @author dkf
 *
 */
public class VariantViewChange extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	static double prob = 0;
	String fit_choice;
	private JPanel summary_panel;
	JPanel buttonPane;
	JPanel control_panel;
	JButton submit_button;
	TraceVariant traceVariant;

	public VariantViewChange(TraceVariant newTraceVariant) {
		// traceVariant.setSummary();
		// this.add(createChangePanel(traceVariant));
		traceVariant = newTraceVariant;
		createChangePanel();
		// then we need to updateData according to this traceVariant
	}

	private void createChangePanel() {

		TitledBorder title = BorderFactory.createTitledBorder("Show and Change Variant");
		title.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(title);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		NumberFormat num_format = NumberFormat.getNumberInstance();
		// here to create a panel to show the summary information

		summary_panel = new JPanel();
		TitledBorder title2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Summary of Variant");
		title2.setTitleJustification(TitledBorder.LEFT);
		summary_panel.setBorder(title2);
		summary_panel.setMinimumSize(new Dimension(250, 100));
		summary_panel.setPreferredSize(new Dimension(250, 100));

		double size[][] = { { TableLayout.FILL, 0.05, TableLayout.FILL, 0.05 },
				{ TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 0.1 } };
		summary_panel.setLayout(new TableLayout(size));
		// summary_panel.setSize(400,400);
		summary_panel.setOpaque(false);

		JLabel fit_label = new JLabel("Fit: ");
		JFormattedTextField fit_value = new JFormattedTextField();
		// for the whole trace 
		if (traceVariant.getFitLabel() != null)
			fit_value.setValue(traceVariant.getFitLabel());
		else
			fit_value.setValue("UNKNOWN");

		summary_panel.add(fit_label, "0,0", 0);
		summary_panel.add(fit_value, "2,0", 1);

		JLabel tnum_label = new JLabel("Trace Num: ");
		JFormattedTextField tnum_value = new JFormattedTextField(num_format);
		tnum_value.setValue(traceVariant.getCount());
		summary_panel.add(tnum_label, "0,1", 2);
		summary_panel.add(tnum_value, "2,1", 3);

		JLabel pnum_label = new JLabel("Pos Num: ");
		JFormattedTextField pnum_value = new JFormattedTextField(num_format);
		pnum_value.setValue(traceVariant.getSummary().get(Configuration.POS_IDX));
		summary_panel.add(pnum_label, "0,2", 4);
		summary_panel.add(pnum_value, "2,2", 5);

		JLabel nnum_label = new JLabel("Neg Num: ");
		JFormattedTextField nnum_value = new JFormattedTextField(num_format);
		// nnum_value.setValue(traceVariant.getSummary().get(Configuration.NEG_IDX));
		summary_panel.add(nnum_label, "0,3", 6);
		summary_panel.add(nnum_value, "2,3", 7);

		JLabel unum_label = new JLabel("Unknown Num: ");
		JFormattedTextField unum_value = new JFormattedTextField(num_format);
		unum_value.setValue(traceVariant.getSummary().get(Configuration.UNKNOWN_IDX));
		summary_panel.add(unum_label, "0,4", 8);
		summary_panel.add(unum_value, "2,4", 9);

		// here to create a JPanel to change labels of the variant
		// set for pos and neg distribution

		control_panel = new JPanel();
		control_panel.setLayout(new BoxLayout(control_panel, BoxLayout.PAGE_AXIS));
		TitledBorder title3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Control Variant Labels");
		title3.setTitleJustification(TitledBorder.LEFT);
		control_panel.setBorder(title3);
		control_panel.setMinimumSize(new Dimension(250, 100));
		control_panel.setPreferredSize(new Dimension(250, 100));

		// here we choose if it is fit or not fit 
		JLabel fit_choice_label = new JLabel("Choose Fit: ");
		JComboBox fit_box = new JComboBox<>(Configuration.FIT_CHOICES);
		fit_box.setSelectedItem(0); // the last one is chosen
		fit_choice = Configuration.FIT_UNKNOWN;
		fit_box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JComboBox cb = (JComboBox) e.getSource();
				fit_choice = (String) cb.getSelectedItem();
			}
		});

		JPanel fitChoosePane = new JPanel();
		fitChoosePane.setLayout(new BoxLayout(fitChoosePane, BoxLayout.LINE_AXIS));
		fitChoosePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		fitChoosePane.add(Box.createHorizontalGlue());
		fitChoosePane.add(fit_choice_label);
		fitChoosePane.add(Box.createRigidArea(new Dimension(5, 0)));
		fitChoosePane.add(fit_box);

		JLabel fp_label = new JLabel("Positive Rate: ");
		final JTextField fp_value = new JTextField(1);
		//Lay out the buttons from left to right.
		JPanel valuePane = new JPanel();
		valuePane.setLayout(new BoxLayout(valuePane, BoxLayout.LINE_AXIS));
		valuePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		valuePane.add(Box.createHorizontalGlue());
		valuePane.add(fp_label);
		valuePane.add(Box.createRigidArea(new Dimension(5, 0)));
		valuePane.add(fp_value);

		final JSlider fp_slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		fp_slider.setUI(new SlickerSliderUI(fp_slider));
		fp_slider.setValue(0);
		fp_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == fp_slider) {
					// updateThresholdSlider();
					prob = fp_slider.getValue();
					fp_value.setText(" " + prob);
					// paras.setFit_pos_rate(value/100);
				}
			}
		});

		// add button to confirm the changes
		//Lay out the buttons from left to right.
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		submit_button = new JButton("Submit");
		submit_button.setBounds(100, 100, 140, 40);
		submit_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				JOptionPane.showMessageDialog(buttonPane, "get value" + fp_value.getText(), "show the parameter",
						JOptionPane.INFORMATION_MESSAGE);
				// change the label of variant
				// should I change all of again, or just some values?? The values are in the summary of variant. 
				// I could put it separately, but now just make it work, at first
				// also to add the fit label assign. Now we just consider the two attributes
				// System.out.println("prob value is "+prob); // I need to make sure here it changes right. 
				EventLogUtilities.assignVariantLabel(traceVariant, Configuration.POS_LABEL, prob / 100.0);
				// repaint again the summary view of variant // here we are actually in the view of graph, so no use..
				updateSummaryPanel();
			}
		});

		JButton reset_button = new JButton("Reset");
		reset_button.setBounds(100, 100, 140, 40);
		reset_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				fp_slider.setValue(0);
				fp_value.setText(" " + fp_slider.getValue());
				// JOptionPane.showMessageDialog(buttonPane, "reset value" + fp_value.getText(), "reset the parameter", JOptionPane.INFORMATION_MESSAGE);

			}
		});

		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(reset_button);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(submit_button);

		control_panel.add(fitChoosePane);
		control_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		control_panel.add(valuePane);
		control_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		control_panel.add(fp_slider);
		control_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		control_panel.add(buttonPane);
		control_panel.add(Box.createRigidArea(new Dimension(0, 10)));

		fp_slider.setOpaque(false);

		this.add(summary_panel);
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		this.add(control_panel);
		this.add(Box.createRigidArea(new Dimension(0, 200)));

	}

	public JButton getSubmitButton() {
		return submit_button;
	}

	public void update(TraceVariant newTraceVariant) {
		// update the current view w.r.t the selectVariant
		// remove one Panel and generate another panel???
		traceVariant = newTraceVariant;
		traceVariant.setSummary();
		// this.removeAll();
		updateSummaryPanel();
		this.updateUI();
		// this.add(createChangePanel(traceVariant));
		// this.updateUI();

	}

	// only update the value in summaryPanel, maybe we could use it as a listener?? 
	private void updateSummaryPanel() {

		traceVariant.setFitLabel(fit_choice);
		traceVariant.changeSummary();
		((JFormattedTextField) summary_panel.getComponent(1)).setValue(fit_choice);
		((JFormattedTextField) summary_panel.getComponent(3)).setValue(traceVariant.getCount());
		((JFormattedTextField) summary_panel.getComponent(5))
				.setValue(traceVariant.getSummary().get(Configuration.POS_IDX));
		((JFormattedTextField) summary_panel.getComponent(7))
				.setValue(traceVariant.getSummary().get(Configuration.NEG_IDX));
		((JFormattedTextField) summary_panel.getComponent(9))
				.setValue(traceVariant.getSummary().get(Configuration.UNKNOWN_IDX));
	}

}
