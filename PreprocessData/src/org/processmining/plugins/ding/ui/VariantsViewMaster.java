package org.processmining.plugins.ding.ui;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.info.XLogInfo;
import org.processmining.framework.util.ui.widgets.ProMTableWithoutPanel;
import org.processmining.framework.util.ui.widgets.traceview.masterdetail.TransparentSplitPane;
import org.processmining.plugins.ding.preprocess.TraceVariant;

public class VariantsViewMaster extends JPanel {

	private ListPanel list_panel ;
	// info panel we need to add,too
	private final DefaultTableModel infoTableModel;
	private JTable logInfoTable;
	
	public VariantsViewMaster(List<TraceVariant> variants, XLogInfo info) {
		// TODO Auto-generated constructor stub
		list_panel = new ListPanel(variants);
		
		infoTableModel = new DefaultTableModel(new String[] { "Name", "Value" }, 0);
		/*
		infoTableModel.addRow(new String[] { "Traces", " "+info.getNumberOfTraces()});
		infoTableModel.addRow(new String[] { "Events", " "+info.getNumberOfEvents()});
		infoTableModel.addRow(new String[] { "Event Classes", " "+info.getEventClasses().size() });
		infoTableModel.addRow(new String[] { "Variants", " " +  variants.size()});
		*/
		infoTableModel.addRow(new String[] { "Traces", "0"});
		infoTableModel.addRow(new String[] { "Events", "0"});
		infoTableModel.addRow(new String[] { "Event Classes", "0"});
		infoTableModel.addRow(new String[] { "Variants", "" +  variants.size()});
		
		logInfoTable = new ProMTableWithoutPanel(infoTableModel);
		logInfoTable.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
		logInfoTable.setBackground(Color.WHITE);

		TransparentSplitPane leftBottomPanel = new TransparentSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftBottomPanel.setBackground(null);
		
		leftBottomPanel.setTopComponent(list_panel);
		leftBottomPanel.setResizeWeight(1);
		leftBottomPanel.setOneTouchExpandable(true);
		leftBottomPanel.setBottomComponent(logInfoTable);
		add(leftBottomPanel);
		
	}

	public ListPanel getListPanel() {
		// TODO Auto-generated method stub
		return list_panel;
	}

	public TraceVariant getSelectVariant() {
		// TODO Auto-generated method stub
		return (TraceVariant) list_panel.getSelectData();
	}
	
	
}
