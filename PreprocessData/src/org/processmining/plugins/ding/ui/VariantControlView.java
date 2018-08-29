package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.traceview.masterdetail.TransparentSplitPane;
import org.processmining.plugins.ding.preprocess.TraceVariant;

public class VariantControlView extends JPanel {
	private static final long serialVersionUID = 2073714666502722213L;
	
	private final VariantsViewMaster masterView;
	private final VariantChangeView detailView;
	
	private int select_idx = 0;
	
	public VariantControlView(List<TraceVariant> variants, XLog log) {
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
		
		masterView = new VariantsViewMaster(variants, info);
		detailView = new VariantChangeView(variants.get(select_idx));
		
		// here to add the listSelectionListener
		JList list = masterView.getListPanel().getList();
		list.addListSelectionListener(new ListSelectionListener() {
			// select one one variant, then the detailView need to change.
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (e.getValueIsAdjusting() == false) {
		            if (list.getSelectedIndex() == -1) {
		            	
		            } else {
		            	//Selection, update the other detailView
		            	select_idx = list.getSelectedIndex();
		            	detailView.update(variants.get(select_idx));
		            }
		        }
			}
		});

		this.setSize(800,800);
		add(new MasterDetailView(masterView, detailView));
	}

	private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Variant Change Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        List<TraceVariant> variants = new ArrayList<TraceVariant>() ;
        variants.add(new TraceVariant());
        variants.add(new TraceVariant());
        //Add contents to the window.
        // XLogInfo info = XLogInfoImpl.create(log);
        frame.add(new VariantControlView(variants, null));
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}

class MasterDetailView extends JPanel {

	private static final long serialVersionUID = -289071473358339232L;

	private final VariantsViewMaster masterView;
	private final VariantChangeView detailView;
	private final TransparentSplitPane splitPane;

	private boolean doUpdates = true;

	public MasterDetailView(final VariantsViewMaster masterView, final VariantChangeView detailView) {
		super();
		setBackground(null);
		setForeground(null);
		setOpaque(false);
		setLayout(new BorderLayout());
		
		this.masterView = masterView;
		this.detailView = detailView;

		// to get the selected index and pass to the detailView
		
		detailView.update(masterView.getSelectVariant());
		
		splitPane = createSplitPane();
		splitPane.setLeftComponent(masterView);
		splitPane.setRightComponent(detailView);
		splitPane.setResizeWeight(1.0d);
		splitPane.setOneTouchExpandable(true);
		add(splitPane, BorderLayout.CENTER);
	}

	protected TransparentSplitPane createSplitPane() {
		return new TransparentSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
	}

	public  VariantsViewMaster getMasterView() {
		return masterView;
	}

	public  VariantChangeView getDetailView() {
		return detailView;
	}

	public TransparentSplitPane getSplitPane() {
		return splitPane;
	}

	public void disableDetailUpdates() {
		doUpdates = false;
	}

	public void enableDetailUpdates() {
		doUpdates = true;
	}
	
}

