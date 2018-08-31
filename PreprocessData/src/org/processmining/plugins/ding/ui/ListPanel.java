package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.ClickListener;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.MoveListener;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.ding.preprocess.TraceVariant;
import org.processmining.plugins.ding.util.Configuration;
import org.processmining.plugins.ding.util.XESTrace;

public class ListPanel extends JPanel {
	private final JList jlist;
	private final DefaultListModel<TraceVariant> listModel;
    private int seleted_idx;
    private  ProMTraceViewCellRenderer<TraceVariant> cellRenderer;
	private transient CopyOnWriteArraySet<ClickListener<TraceVariant>> clickListener = new CopyOnWriteArraySet<ClickListener<TraceVariant>>();
	private transient CopyOnWriteArraySet<MoveListener<TraceVariant>> moveListener = new CopyOnWriteArraySet<MoveListener<TraceVariant>>();
    
	public ListPanel(List<TraceVariant> variants) {
		super(new BorderLayout());
		seleted_idx = 0;
		listModel = new DefaultListModel<TraceVariant>();
		for(TraceVariant var: variants) {
			listModel.addElement(var);
		}
		
		cellRenderer = new ProMTraceViewCellRenderer<TraceVariant>(listModel, new TraceBuilder<TraceVariant>() {
			public Trace<? extends Event> build(TraceVariant element) {
				// here how to transfer element to Trace ?? 
				XTrace  trace = element.getTrace_list().get(0);
				return new XESTrace(trace, new XEventNameClassifier());
			}
		}, Configuration.DEFAULT_FONT, true);
		
		// create the JList to show data 
		// how about each cell render?? 
		jlist = new JList(listModel);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setLayoutOrientation(JList.VERTICAL);
		jlist.setSelectedIndex(seleted_idx);
		jlist.setVisibleRowCount(-1);
        
		jlist.setFixedCellHeight((int) cellRenderer.getPreferredSize().getHeight());
		jlist.setOpaque(false);
		jlist.setForeground(null);
		jlist.setBackground(null);
		jlist.setCellRenderer(cellRenderer);
		
		ProMTraceListMouseAdapter<TraceVariant> mouseAdapter = new ProMTraceListMouseAdapter<TraceVariant>(jlist, cellRenderer, clickListener,
				moveListener);
		jlist.addMouseListener(mouseAdapter);
		jlist.addMouseMotionListener(mouseAdapter);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setForeground(null);
		setBackground(null);
        
        JScrollPane listScrollPane = new JScrollPane(jlist);
        // add part to cell render, but later
        listScrollPane.setPreferredSize(new Dimension(500, 500));
        listScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        
        add(listScrollPane, BorderLayout.CENTER);
	}

	public int getSeleted_idx() {
		return seleted_idx;
	}
	public JList getList() {
		return jlist;
	}

	public Object getSelectData() {
		// TODO Auto-generated method stub
		return listModel.getElementAt(seleted_idx);
	}

	private final static class ProMTraceListMouseAdapter<T> extends MouseAdapter {

		private final Set<ClickListener<T>> clickListener;
		private final Set<MoveListener<T>> moveListener;
		private final JList<T> list;
		private ProMTraceViewCellRenderer<T> cellRenderer;

		private ProMTraceListMouseAdapter(JList<T> list, ProMTraceViewCellRenderer<T> cellRenderer,
				Set<ClickListener<T>> clickListener, Set<MoveListener<T>> moveListener) {
			super();
			this.list = list;
			this.cellRenderer = cellRenderer;
			this.clickListener = clickListener;
			this.moveListener = moveListener;
		}

		public void mouseClicked(MouseEvent e) {

			if (!clickListener.isEmpty()) {
				final int traceIndex = getTraceIndex(e);
				if (traceIndex != -1) {
					final T trace = list.getModel().getElementAt(traceIndex);
					final int eventIndex = getEventIndex(traceIndex, trace, e);

					for (ClickListener<T> listener : clickListener) {
						if (e.getClickCount() == 2) {
							listener.traceMouseDoubleClicked(trace, traceIndex, eventIndex, e);
						} else if (e.getClickCount() == 1) {
							listener.traceMouseClicked(trace, traceIndex, eventIndex, e);
						}
					}
				}
			}
		}

		public void mouseMoved(MouseEvent e) {

			if (!moveListener.isEmpty()) {
				final int traceIndex = getTraceIndex(e);
				if (traceIndex != -1) {
					final T trace = list.getModel().getElementAt(traceIndex);
					final int eventIndex = getEventIndex(traceIndex, trace, e);

					for (MoveListener<T> listener : moveListener) {
						listener.traceMouseMoved(trace, traceIndex, eventIndex, e);
					}
				}
			}
		}

		private int getEventIndex(int traceIndex, T trace, MouseEvent e) {
			return cellRenderer.translateToEventIndex(e.getPoint(), trace, list.isSelectedIndex(traceIndex));
		}

		private int getTraceIndex(MouseEvent e) {
			Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
			if (r != null && r.contains(e.getPoint())) {
				return list.locationToIndex(e.getPoint());
			}
			return -1;
		}

	}

	
	private final static class ProMTraceViewCellRenderer<E> extends ProMTraceView implements ListCellRenderer<E> {

		private static final long serialVersionUID = -2495069999724478333L;

		private TraceBuilder<E> traceBuilder;
		private E currentValue;
		private int fixedWidthLimit = Configuration.DEFAULT_FIXED_WIDTH_TRACE_COUNT;

		private final ListModel<E> listModel;

		public ProMTraceViewCellRenderer(ListModel listModel, TraceBuilder traceBuilder, Font defaultFont,
				boolean hasLabels) {
			super(defaultFont, hasLabels);
			this.listModel = listModel;
			setTraceBuilder(traceBuilder);
			setOpaque(false);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			setForeground(list.getForeground());
			return this;
		}

		public int translateToEventIndex(Point point, E value, boolean isSelected) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			return translateToEventIndex(point);
		}

		public int translateToDetailedEventIndex(Point point, E value, boolean isSelected) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			return translateToDetailedEventIndex(point);
		}

		public TraceBuilder<E> getTraceBuilder() {
			return traceBuilder;
		}

		public void setTraceBuilder(TraceBuilder<E> traceBuilder) {
			this.currentValue = null;
			this.traceBuilder = traceBuilder;
		}

		public int getFixedWidthLimit() {
			return fixedWidthLimit;
		}

		public void setFixedWidthLimit(int fixedWidthLimit) {
			this.currentValue = null;
			this.fixedWidthLimit = fixedWidthLimit;
		}

		@Override
		public int getFixedWedgeWidth() {
			// listModel might be have been initialized yet
			if (listModel != null && listModel.getSize() > getFixedWidthLimit()) {
				// Override the wedge width, either with max width or the preset fixed width
				return super.getFixedWedgeWidth() == -1 ? getMaxWedgeWidth() : super.getFixedWedgeWidth();
			} else {
				// Return the normal setting
				return super.getFixedWedgeWidth();
			}
		}

		@Override
		public int getFixedInfoWidth() {
			// listModel might be have been initialized yet
			if (listModel != null && listModel.getSize() > getFixedWidthLimit()) {
				// Override the info width, either with max width or the preset fixed width
				return super.getFixedInfoWidth() == -1 ? getMaxInfoWidth() : super.getFixedInfoWidth();
			} else {
				// Return the normal setting
				return super.getFixedInfoWidth();
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			int event = translateToDetailedEventIndex(e.getPoint(), currentValue, isSelected());
			if (event != -1) {
				Trace<?> tr = traceBuilder.build(currentValue);
				if (event == -2) {
					return "<html>" + n(tr.getName()) + "<br>" + n(tr.getInfo()) + "</html>";
				} else if (event >= 0) {
					for (Iterator<? extends Event> it = tr.iterator(); it.hasNext();) {
						Event ev = it.next();
						if (event == 0) {
							return "<html>" + n(ev.getTopLabel()) + "<br>" + n(ev.getLabel()) + "<br>"
									+ n(ev.getBottomLabel()) + "<br>" + n(ev.getBottomLabel2()) + "</html>";
						}
						event--;
					}
				}
			}
			return null;
		}

		private String n(Object x) {
			return x == null ? "" : x.toString();
		}
	}
	
}
