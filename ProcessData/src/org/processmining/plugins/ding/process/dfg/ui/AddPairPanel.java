package org.processmining.plugins.ding.process.dfg.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

public class AddPairPanel extends JPanel {
	JPanel choosePanel;
	/**
	 * Create the panel.
	 */
	public AddPairPanel() {
		this.setBounds(100, 100, 691, 507);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		JRadioButton addAllBtn = new JRadioButton("Add All In Order");
		addAllBtn.setSelected(true);
		GridBagConstraints gbc_addAllBtn = new GridBagConstraints();
		gbc_addAllBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addAllBtn.gridx = 0;
		gbc_addAllBtn.gridy = 0;
		this.add(addAllBtn, gbc_addAllBtn);
		
		final JRadioButton chooseBtn = new JRadioButton("Add XOR Pair By Choice");
		chooseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// if this is selected, then we show the next Component, 
				// we can put them into one Panel, or we jsut set them invisible??? 
				// I think it is better to put them into one Jpanel
				if(chooseBtn.isSelected()) {
					// so it still shows but different color
					chooseBtn.setEnabled(false);
				}
			}
		});
		GridBagConstraints gbc_chooseBtn = new GridBagConstraints();
		gbc_chooseBtn.insets = new Insets(0, 0, 5, 0);
		gbc_chooseBtn.gridx = 1;
		gbc_chooseBtn.gridy = 0;
		this.add(chooseBtn, gbc_chooseBtn);
		
		choosePanel = new JPanel();
		choosePanel.setBorder(new TitledBorder(null, "Choose XOR Pair To Add Or Remove", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_choosePanel = new GridBagConstraints();
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
		
		JButton addPairBtn = new JButton("Add this pair");
		addPairBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// when it is clicked, we need to check if the pair is availabel, if not avaible, we need to
				// give an information dialog
			}
		});
		GridBagConstraints gbc_addPairBtn = new GridBagConstraints();
		gbc_addPairBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addPairBtn.gridx = 0;
		gbc_addPairBtn.gridy = 0;
		addPanel.add(addPairBtn, gbc_addPairBtn);
		
		JLabel addSourceLabel = new JLabel("Choose Source");
		GridBagConstraints gbc_addSourceLabel = new GridBagConstraints();
		gbc_addSourceLabel.insets = new Insets(0, 0, 5, 5);
		gbc_addSourceLabel.anchor = GridBagConstraints.EAST;
		gbc_addSourceLabel.gridx = 0;
		gbc_addSourceLabel.gridy = 1;
		addPanel.add(addSourceLabel, gbc_addSourceLabel);
		
		JComboBox addSourceComboBox = new JComboBox();
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
		
		JComboBox addTargetComboBox = new JComboBox();
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
		
		JButton rmPairBtn = new JButton("Remove this pair");
		rmPairBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// after we have this action, we remove the long-term dependency from this pair..
				// it means that each pair should store its places and transitions..
				// or we just mark the source or the target and then delete them from the 
				// map we have, yes, it seems a better way, so we don't need to be so solid
				
				// how to pass data?? We need to communicate with the main View, so we need to ?? 
				// we have data as one parameter, and then return data also as a parameter, here, 
				// will the panel change?? Acutally no!! the structure keeps the same, and that's all.
				// we will also see the list shoudl be the name of choosing items... 
				// so they just pass two list of xor block names:: 
				// source list and target list :: 
				// Also, we need to have the level of them, and the mark, if they are chosen or not...
				// A special data structure here?? Right ?? 
				// if not, we need to pass the list of xorClusterList, 
				// in this way, it saves the space, but the computation may increase.. But its ok, I would say..
				
				
			}
		});
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
		
		JComboBox rmSourceComboBox = new JComboBox();
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
		
		JComboBox rmTargetComBox = new JComboBox();
		GridBagConstraints gbc_rmTargetComBox = new GridBagConstraints();
		gbc_rmTargetComBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rmTargetComBox.gridx = 1;
		gbc_rmTargetComBox.gridy = 2;
		rmPanel.add(rmTargetComBox, gbc_rmTargetComBox);
	}

}
