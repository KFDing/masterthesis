package org.processmining.plugins.ding.preprocess.ui;

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
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.traceview.masterdetail.TransparentSplitPane;
import org.processmining.plugins.ding.preprocess.TraceVariant;

public class VariantWholeView extends JPanel {
	private static final long serialVersionUID = 2073714666502722213L;
	
	private final VariantsViewMaster masterView;
	private final VariantViewChange detailView;
	
	private int select_idx = 0;
	
	public VariantWholeView(List<TraceVariant> variants, XLogInfo info) {
		
		masterView = new VariantsViewMaster(variants, info);
		detailView = new VariantViewChange(variants.get(select_idx));
		
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
		            	// System.out.println(select_idx);
		            	detailView.update(variants.get(select_idx));
		            	// System.out.println(variants.get(select_idx).getTraceVariant().toString());
		            }
		        }
			}
		});
		// JButton submit_button = detailView.getSubmitButton();
		// but for submit value, we need to do it outside of the initial view
	

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
        frame.add(new VariantWholeView(variants, null));
 
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
	private final VariantViewChange detailView;
	private final TransparentSplitPane splitPane;

	public MasterDetailView(final VariantsViewMaster masterView, final VariantViewChange detailView) {
		super();
		setBackground(null);
		setForeground(null);
		setOpaque(false);
		// setLayout(new BorderLayout());
		// I need to find out which causes the ScollableFrame, which is only needed in the left part, not the right part
		this.masterView = masterView;
		this.detailView = detailView;

		splitPane = createSplitPane();
		splitPane.setLeftComponent(masterView);
		splitPane.setRightComponent(detailView);
		splitPane.setResizeWeight(1.0d);
		splitPane.setOneTouchExpandable(true);
		add(splitPane);
	}

	protected TransparentSplitPane createSplitPane() {
		return new TransparentSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
	}

	public  VariantsViewMaster getMasterView() {
		return masterView;
	}

	public  VariantViewChange getDetailView() {
		return detailView;
	}

	public TransparentSplitPane getSplitPane() {
		return splitPane;
	}

	
}

