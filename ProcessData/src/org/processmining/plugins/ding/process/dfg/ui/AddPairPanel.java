package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.processmining.plugins.ding.process.dfg.model.XORCluster;
import org.processmining.processtree.ProcessTreeElement;

public class AddPairPanel extends JPanel{
	JRadioButton addAllBtn;
	JRadioButton  chooseBtn;
	JPanel choosePanel;
	
	JButton rmPairBtn;
	JButton addPairBtn;
	
	JComboBox addSourceComboBox;
	JComboBox addTargetComboBox;
	JComboBox rmSourceComboBox;
	JComboBox rmTargetComboBox;
	private JRadioButton rdbtnNewRadioButton_2;
	
	
	/**
	 * Create the panel.
	 */
	public AddPairPanel() {
		this.setBounds(100, 100, 691, 507);
		this.setBorder(new TitledBorder(null, "Add Long-term Dependency on Petri Net", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		JPanel selectPanel = new JPanel();
		GridBagConstraints gbc_selectPanel = new GridBagConstraints();
		gbc_selectPanel.insets = new Insets(0, 0, 5, 0);
		gbc_selectPanel.fill = GridBagConstraints.BOTH;
		gbc_selectPanel.gridwidth = 2;
		gbc_selectPanel.gridx = 0;
		gbc_selectPanel.gridy = 0;
		this.add(selectPanel, gbc_selectPanel);
		
		selectPanel.setBorder(new TitledBorder(null, "Select Add Method ", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_select = new GridBagLayout();
		gbl_select.columnWidths = new int[]{0, 0, 0};
		gbl_select.rowHeights = new int[] {0, 0, 10, 0};
		gbl_select.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_select.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		selectPanel.setLayout(gbl_select);
		
		addAllBtn = new JRadioButton("Add All In Order");
		addAllBtn.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_addAllBtn = new GridBagConstraints();
		gbc_addAllBtn.anchor = GridBagConstraints.LINE_START;
		gbc_addAllBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addAllBtn.gridx = 0;
		gbc_addAllBtn.gridy = 0;
		selectPanel.add(addAllBtn, gbc_addAllBtn);
		
		
		chooseBtn = new JRadioButton("Add XOR Pair By Choice");
		GridBagConstraints gbc_chooseBtn = new GridBagConstraints();
		gbc_chooseBtn.anchor = GridBagConstraints.LINE_START;
		gbc_chooseBtn.insets = new Insets(0, 0, 5, 0);
		gbc_chooseBtn.gridx = 0;
		gbc_chooseBtn.gridy = 1;
		selectPanel.add(chooseBtn, gbc_chooseBtn);
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(addAllBtn);
		btnGroup.add(chooseBtn);
		
		choosePanel = new JPanel();
		choosePanel.setBorder(new TitledBorder(null, "Choose XOR Pair To Add Or Remove", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_choosePanel = new GridBagConstraints();
		gbc_choosePanel.insets = new Insets(0, 0, 5, 0);
		gbc_choosePanel.fill = GridBagConstraints.BOTH;
		gbc_choosePanel.gridwidth = 2;
		gbc_choosePanel.gridx = 0;
		gbc_choosePanel.gridy = 1;
		this.add(choosePanel, gbc_choosePanel);
		GridBagLayout gbl_choosePanel = new GridBagLayout();
		gbl_choosePanel.columnWidths = new int[]{0, 0, 0};
		gbl_choosePanel.rowHeights = new int[]{0, 37, 0};
		gbl_choosePanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_choosePanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		choosePanel.setLayout(gbl_choosePanel);
		
		JPanel addPanel = new JPanel();
		GridBagConstraints gbc_addPanel = new GridBagConstraints();
		gbc_addPanel.fill = GridBagConstraints.BOTH;
		gbc_addPanel.gridwidth = 2;
		gbc_addPanel.insets = new Insets(0, 0, 5, 0);
		gbc_addPanel.gridx = 0;
		gbc_addPanel.gridy = 0;
		choosePanel.add(addPanel, gbc_addPanel);
		addPanel.setBorder(new TitledBorder(null, "Chosse XOR Pair To Add LT", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_add = new GridBagLayout();
		gbl_add.columnWidths = new int[]{0, 0, 0};
		gbl_add.rowHeights = new int[]{0, 0, 0, 0};
		gbl_add.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_add.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		addPanel.setLayout(gbl_add);
		
		addPairBtn = new JButton("Add this pair");
		
		GridBagConstraints gbc_addPairBtn = new GridBagConstraints();
		gbc_addPairBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addPairBtn.gridx = 0;
		gbc_addPairBtn.gridy = 0;
		addPanel.add(addPairBtn, gbc_addPairBtn);
		
		JLabel addSourceLabel = new JLabel("Choose Source");
		GridBagConstraints gbc_addSourceLabel = new GridBagConstraints();
		gbc_addSourceLabel.insets = new Insets(0, 0, 5, 5);
		gbc_addSourceLabel.gridx = 0;
		gbc_addSourceLabel.gridy = 1;
		addPanel.add(addSourceLabel, gbc_addSourceLabel);
		
		addSourceComboBox = new JComboBox();
		GridBagConstraints gbc_addSourceComboBox = new GridBagConstraints();
		gbc_addSourceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_addSourceComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_addSourceComboBox.gridx = 1;
		gbc_addSourceComboBox.gridy = 1;
		addPanel.add(addSourceComboBox, gbc_addSourceComboBox);
		
		JLabel addTargetLabel = new JLabel("Choose Target");
		GridBagConstraints gbc_addTargetLabel = new GridBagConstraints();
		gbc_addTargetLabel.insets = new Insets(0, 0, 0, 5);
		gbc_addTargetLabel.gridx = 0;
		gbc_addTargetLabel.gridy = 2;
		addPanel.add(addTargetLabel, gbc_addTargetLabel);
		
		addTargetComboBox = new JComboBox();
		GridBagConstraints gbc_addTargetComboBox = new GridBagConstraints();
		gbc_addTargetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_addTargetComboBox.gridx = 1;
		gbc_addTargetComboBox.gridy = 2;
		addPanel.add(addTargetComboBox, gbc_addTargetComboBox);
		
		JPanel rmPanel = new JPanel();
		GridBagConstraints gbc_rmPanel = new GridBagConstraints();
		gbc_rmPanel.fill = GridBagConstraints.BOTH;
		gbc_rmPanel.gridwidth = 2;
		gbc_rmPanel.insets = new Insets(0, 0, 0, 5);
		gbc_rmPanel.gridx = 0;
		gbc_rmPanel.gridy = 1;
		choosePanel.add(rmPanel, gbc_rmPanel);
		rmPanel.setBorder(new TitledBorder(null, "Choose Pair to Remove", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_rmPanel = new GridBagLayout();
		gbl_rmPanel.columnWidths = new int[]{0, 0, 0};
		gbl_rmPanel.rowHeights = new int[]{37, 0, 0, 0};
		gbl_rmPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_rmPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		rmPanel.setLayout(gbl_rmPanel);
		
		rmPairBtn = new JButton("Remove this pair");
		
		GridBagConstraints gbc_rmPairBtn = new GridBagConstraints();
		gbc_rmPairBtn.insets = new Insets(0, 0, 5, 5);
		gbc_rmPairBtn.gridx = 0;
		gbc_rmPairBtn.gridy = 0;
		rmPanel.add(rmPairBtn, gbc_rmPairBtn);
		
		JLabel rmSourceLabel = new JLabel("Choose Source");
		GridBagConstraints gbc_rmSourceLabel = new GridBagConstraints();
		gbc_rmSourceLabel.insets = new Insets(0, 0, 5, 5);
		gbc_rmSourceLabel.gridx = 0;
		gbc_rmSourceLabel.gridy = 1;
		rmPanel.add(rmSourceLabel, gbc_rmSourceLabel);
		
		rmSourceComboBox = new JComboBox();
		GridBagConstraints gbc_rmSourceComboBox = new GridBagConstraints();
		gbc_rmSourceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rmSourceComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_rmSourceComboBox.gridx = 1;
		gbc_rmSourceComboBox.gridy = 1;
		rmPanel.add(rmSourceComboBox, gbc_rmSourceComboBox);
		
		JLabel rmTargetLabel = new JLabel("Choose Target");
		GridBagConstraints gbc_rmTargetLabel = new GridBagConstraints();
		gbc_rmTargetLabel.insets = new Insets(0, 0, 0, 5);
		gbc_rmTargetLabel.gridx = 0;
		gbc_rmTargetLabel.gridy = 2;
		rmPanel.add(rmTargetLabel, gbc_rmTargetLabel);
		
		rmTargetComboBox = new JComboBox();
		GridBagConstraints gbc_rmTargetComBox = new GridBagConstraints();
		gbc_rmTargetComBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rmTargetComBox.gridx = 1;
		gbc_rmTargetComBox.gridy = 2;
		rmPanel.add(rmTargetComboBox, gbc_rmTargetComBox);
		
		
	}

	public void setPanelEnabled(JPanel panel, Boolean isEnabled) {
	    panel.setEnabled(isEnabled);

	    Component[] components = panel.getComponents();
	    
	    for (Component component : components) {
	        if (component instanceof JPanel) {
	            setPanelEnabled((JPanel) component, isEnabled);
	        }
	        component.setEnabled(isEnabled);
	    }
	}
	
	public int getAddSourceIndex() {
		// TODO Auto-generated method stub
		return addSourceComboBox.getSelectedIndex();
	}
	
	public int getAddTargetIndex() {
		// TODO Auto-generated method stub
		return addTargetComboBox.getSelectedIndex();
	}
	
	public int getRMSourceIndex() {
		// TODO Auto-generated method stub
		return rmSourceComboBox.getSelectedIndex();
	}
	
	public int getRMTargetIndex() {
		// TODO Auto-generated method stub
		return rmTargetComboBox.getSelectedIndex();
	}

	public void updateAddSource(List<XORCluster<ProcessTreeElement>> sources) {
		// add the target to combox
		// System.out.println("before remove all items in add of source");
		addSourceComboBox.removeAllItems();
		for(XORCluster<ProcessTreeElement> source: sources) {
			// we should add the name of it on add TargetCombox 
			String sourceName = source.getLabel();
			addSourceComboBox.addItem(sourceName);
			
		}
		// System.out.println("size of items in JCombox " +addSourceComboBox.getItemCount());
		// we need to remove all the choices before we update it 
	}
	
	public void updateRMSource(List<XORCluster<ProcessTreeElement>> sources) {
		// add the target to combox
		// System.out.println("before remove all items in rm of source");
		rmSourceComboBox.removeAllItems();
		for(XORCluster<ProcessTreeElement> source: sources) {
			// we should add the name of it on add TargetCombox 
			String sourceName = source.getLabel();
			rmSourceComboBox.addItem(sourceName);
		}
		// System.out.println("Size of items in JCombox " + rmSourceComboBox.getItemCount());
	}

	public void updateAddTarget(List<XORCluster<ProcessTreeElement>> targets) {
		// add the target to combox
		addTargetComboBox.removeAllItems();
		for(XORCluster<ProcessTreeElement> target: targets) {
			// we should add the name of it on add TargetCombox 
			String targetName = target.getLabel();
			addTargetComboBox.addItem(targetName);
		}
	}
	
	public void updateRMTarget(List<XORCluster<ProcessTreeElement>> targets) {
		// add the target to combox
		rmTargetComboBox.removeAllItems();
		for(XORCluster<ProcessTreeElement> target: targets) {
			// we should add the name of it on add TargetCombox 
			String targetName = target.getLabel();
			rmTargetComboBox.addItem(targetName);
		}
	}

	
	
}
