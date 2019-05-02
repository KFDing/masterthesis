package org.processmining.incorporatenegativeinformation.dialogs.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.ProMComboBox;

public class AddAttributePanel extends JPanel {
	static String[] types = {"XAttributeBoolean", "XAttributeDiscrete", "XAttributeLiteral"};
	
	private XLog log;
	
	JList typeListComp;
	ProMComboBox<String> refAttrComp;
	/**
	 * Create the panel.
	 */
	public AddAttributePanel() {
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(40, 10));
		
		JPanel eventCheckPanel = new JPanel();
		
		JScrollPane mapPanel = new JScrollPane();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(15, Short.MAX_VALUE)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 430, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(eventCheckPanel, GroupLayout.PREFERRED_SIZE, 433, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(mapPanel, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(18, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					.addGap(30)
					.addComponent(mapPanel, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
					.addGap(27)
					.addComponent(eventCheckPanel, GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JCheckBox eventAttrCheckComp = new JCheckBox("Assign attribute to Events:");
		eventCheckPanel.add(eventAttrCheckComp);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		
		panel.setLayout(gbl_panel);
		
		JLabel attrNameLabel = new JLabel("Name:");
		GridBagConstraints gbc_attrNameLabel = new GridBagConstraints();
		gbc_attrNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_attrNameLabel.anchor = GridBagConstraints.EAST;
		gbc_attrNameLabel.gridx = 0;
		gbc_attrNameLabel.gridy = 0;
		panel.add(attrNameLabel, gbc_attrNameLabel);
		
		JTextField nameField = new JTextField();
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.insets = new Insets(0, 0, 5, 0);
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 0;
		panel.add(nameField, gbc_nameField);
		nameField.setColumns(10);
		
		JLabel attrTypeLabel = new JLabel("Type:");
		GridBagConstraints gbc_attrTypeLabel = new GridBagConstraints();
		gbc_attrTypeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_attrTypeLabel.gridx = 0;
		gbc_attrTypeLabel.gridy = 1;
		panel.add(attrTypeLabel, gbc_attrTypeLabel);
		
		typeListComp = new JList(types);
		GridBagConstraints gbc_typeListComp = new GridBagConstraints();
		gbc_typeListComp.insets = new Insets(0, 0, 5, 0);
		gbc_typeListComp.fill = GridBagConstraints.BOTH;
		gbc_typeListComp.gridx = 1;
		gbc_typeListComp.gridy = 1;
		panel.add(typeListComp, gbc_typeListComp);
		
		JLabel refAttrlabel = new JLabel("Reference Attribute:");
		GridBagConstraints gbc_refAttrlabel = new GridBagConstraints();
		gbc_refAttrlabel.anchor = GridBagConstraints.EAST;
		gbc_refAttrlabel.insets = new Insets(0, 0, 0, 5);
		gbc_refAttrlabel.gridx = 0;
		gbc_refAttrlabel.gridy = 2;
		panel.add(refAttrlabel, gbc_refAttrlabel);
		
		// this needs to read data from logs and then give values
		// also we need one function to add tables items into it
		// but for the maps, we need to create a maps from them, one is the type, one is the
		// values map
		refAttrComp = new ProMComboBox<String>(getAttributes());
		GridBagConstraints gbc_refAttrComp = new GridBagConstraints();
		gbc_refAttrComp.fill = GridBagConstraints.HORIZONTAL;
		gbc_refAttrComp.gridx = 1;
		gbc_refAttrComp.gridy = 2;
		panel.add(refAttrComp, gbc_refAttrComp);
		setLayout(groupLayout);
		
	
	}
	
	private TreeSet<String> getAttributes() {
		TreeSet<String> attributeNames = new TreeSet<String>();
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				attributeNames.addAll(attributes.keySet());
			}
		return attributeNames;
	}
	
	private Set<String> getValues( String key) {
		// get the global trace attribute type and check if it has this
		TreeSet<String> values = new TreeSet<String>(new AlphanumComparator());
		
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				if (attributes.containsKey(key))
					values.add(attributes.get(key).toString());
			}

		 
		return values;
	
	}
	
	// when we choose the type for new attribute, we trigger the this listener
	private class ChooseRefListener implements ActionListener {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == typeListComp) {

				TreeSet<String> attributeNames = getAttributes();
				refAttrComp.setModel(new DefaultComboBoxModel(attributeNames.toArray()));
			}
		}
	}
	
	private class AttributeListener implements ActionListener {

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == refAttrComp) {

				Set<String> values = getValues((String)refAttrComp.getSelectedItem());
				/*
				 * System.out.println(values.size() + " on AttributeLister");
				 * for(String value: values) System.out.println(value +
				 * " on AttributeLister");
				 */
				MyComboBoxModel model = new MyComboBoxModel(values.toArray());
				// attribute_filter_log_values.setModel(model);
				model.setSelectedItem(null);
			}
		}
	}
}
