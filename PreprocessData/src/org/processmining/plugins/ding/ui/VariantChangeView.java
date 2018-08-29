package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.ding.preprocess.TraceVariant;
import org.processmining.plugins.ding.util.Configuration;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

import info.clearthought.layout.TableLayout;

public class VariantChangeView extends JPanel {
	
	public VariantChangeView(TraceVariant traceVariant) {
		this.setLayout(new BorderLayout());
		this.add(createChangePanel(traceVariant), BorderLayout.CENTER);
		// this.setSize(600,600);
	}
 
	private JPanel createChangePanel(TraceVariant traceVariant) {
		JPanel c_panel =  new JPanel();
		TitledBorder title = BorderFactory.createTitledBorder("Show and Change Variant");
		title.setTitleJustification(TitledBorder.CENTER);
		c_panel.setBorder(title);
		
		c_panel.setLayout(new BoxLayout(c_panel, BoxLayout.PAGE_AXIS));
		NumberFormat num_format = NumberFormat.getNumberInstance();
		// here to create a panel to show the summary information

		JPanel summary_panel = new JPanel();
		TitledBorder title2 = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.black), "Summary of Variant");
		title2.setTitleJustification(TitledBorder.LEFT);
		summary_panel.setBorder(title2);
		
		double size[][] = { 
				{ TableLayout.FILL, 0.05, TableLayout.FILL, 0.05 } 
				,{ 0.25,0.25, 0.25,0.25} };
		summary_panel.setLayout(new TableLayout(size));
		// summary_panel.setSize(400,400);
		summary_panel.setOpaque(false);
		
		JLabel fit_label = new JLabel("Fit: ");
		JFormattedTextField fit_value =  new JFormattedTextField();
		fit_value.setValue(traceVariant.getFitLabel());
		summary_panel.add(fit_label, "0,0");
		summary_panel.add(fit_value, "2,0");
		
		JLabel tnum_label = new JLabel("Trace Num: ");
		JFormattedTextField tnum_value =  new JFormattedTextField(num_format);
		tnum_value.setValue(traceVariant.getCount());
		summary_panel.add(tnum_label, "0,1");
		summary_panel.add(tnum_value, "2,1");
		
		JLabel pnum_label = new JLabel("Trace Num: ");
		JFormattedTextField pnum_value =  new JFormattedTextField(num_format);
		pnum_value.setValue(traceVariant.getSummary().get(Configuration.POS_IDX));
		summary_panel.add(pnum_label, "0,2");
		summary_panel.add(pnum_value, "2,2");
		
		JLabel nnum_label = new JLabel("Trace Num: ");
		JFormattedTextField nnum_value =  new JFormattedTextField(num_format);
		nnum_value.setValue(traceVariant.getSummary().get(Configuration.NEG_IDX));
		summary_panel.add(nnum_label, "0,3");
		summary_panel.add(nnum_value, "2,3");
		
		
		// here to create a JPanel to change labels of the variant
		// set for pos and neg distribution
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
					double value = fp_slider.getValue();
					fp_value.setText(" "+ value);
					// paras.setFit_pos_rate(value/100);
				}	
			}
		});
        
		// add button to confirm the changes
		//Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
		JButton submit_button=new JButton("Submit");    
		submit_button.setBounds(100,100,140, 40);    
		submit_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				JOptionPane.showMessageDialog(buttonPane, "get value" + fp_value.getText(), "show the parameter", JOptionPane.INFORMATION_MESSAGE);
				// change the label of variant
				// repaint again the summary view of variant
			}          
	    });
		        	
		JButton reset_button=new JButton("Reset");    
		reset_button.setBounds(100,100,140, 40);    
		reset_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// submit all the changed parameters 
				fp_slider.setValue(0);
				fp_value.setText(" "+ fp_slider.getValue());
				JOptionPane.showMessageDialog(buttonPane, "reset value" + fp_value.getText(), "reset the parameter", JOptionPane.INFORMATION_MESSAGE);
				// change the label of variant
				// repaint again the summary view of variant
			}          
	    });
		
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(reset_button);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(submit_button);
        
		fp_slider.setOpaque(false);
		
		c_panel.add(summary_panel);
		c_panel.add(Box.createRigidArea(new Dimension(0,10)));
		c_panel.add(valuePane);
		c_panel.add(Box.createRigidArea(new Dimension(0,10)));
		c_panel.add(fp_slider);
		c_panel.add(Box.createRigidArea(new Dimension(0,10)));
		c_panel.add(buttonPane);
		c_panel.add(Box.createRigidArea(new Dimension(0,10)));	
		
		return c_panel;
	}
	
	public void update(TraceVariant traceVariant) {
		// update the current view w.r.t the selectVariant
		// remove one Panel and generate another panel???
		this.removeAll();
		this.add(createChangePanel(traceVariant), BorderLayout.CENTER);
		
	}

}
