package org.processmining.plugins.ding.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.util.XAttributeUtils;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ColorScheme;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.SaveAsActionListener;
import org.processmining.framework.util.ui.widgets.traceview.masterdetail.ProMTraceListMasterDetail;
import org.processmining.xeslite.query.AttributeTypeResolver;
import org.processmining.xeslite.query.XIndex;
import org.processmining.xeslite.query.XIndexedEvents;
import org.processmining.xeslite.query.syntax.ParseException;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.DeduplicationStrategy;

/**
 * Log Visualizer based on a {@link ProMTraceList}.
 * 
 * @author F. Mannhardt
 * 
 */
public class LogViewVisualizer extends JPanel {

	private static final long serialVersionUID = 2073714666502722213L;

	private static final String QUERY_HELP_TEXT = "<HTML><h1>Query Syntax</h1>"
			+ "SQL-like filtering by trace/event names (event occurrence) or by attributes (attribute with specified value present in trace)"
			+ "<h2>Examples</h2>" + "<ul>" + "<li>'A' - searches for traces that contain event with exact name 'A'</li>"
			+ "<li>'\"event A\"' - searches for traces that contain event with exact name 'event A'</li>"
			+ "<li>'%A' - searches for traces that contain event whose name contains 'A'</li>"
			+ "<li>'~.*A.*' - searches for traces that contain event whose name matches the regex '.*A.*'</li>"
			+ "<li>'amount>50' - searches for traces that contain events with numeric attribute 'amount' which is greater than 50</li>"
			+ "<li>'name%joe' - searches for traces that contain events with literal attribute 'name' which contains the value 'joe'</li>"
			+ "</ul>" + "<h2>Details</h2>" + "<ul>"
			+ "<li>Either searches for 'concept:name' attributes of traces and events (start with '~' for use a java regular expression, start with '%' to use a 'contains' query)</li>"
			+ "<li>Supports filtering by trace/event attributes in form of 'eventName'.'attributeName OP attributeValue'.</li>"
			+ "<li>Supported operators (OP) are (=, >, <, !=, >=, <=, % (contains), ~ (java regex), some operators only work with numeric/date attributes.</li>"
			+ "<li>Terms can be connected with (AND, OR) and nested with parens.</li>" + "</ul></HTML>";

	public enum SearchType {
		TRACES, EVENTS
	}

	private static final class AttributeTypeResolverNaiveImpl implements AttributeTypeResolver {

		private final Iterable<XTrace> traces;

		private AttributeTypeResolverNaiveImpl(Iterable<XTrace> traces) {
			this.traces = traces;
		}

		public Class<?> getAttributeType(String attributeName) {
			// simply return the first class found
			for (XTrace t : traces) {
				for (XEvent e : t) {
					XAttribute attribute = e.getAttributes().get(attributeName);
					if (attribute != null) {
						return XAttributeUtils.getType(attribute);
					}
				}
			}
			return XAttributeLiteral.class;
		}
	}

	private final class SearchFieldAction {

		private final LogViewMaster masterView;
		private final LogViewDetail detailView;
		private final ProMTextField searchField;
		private final JCheckBox searchApproximate;

		private boolean isSearching = false;

		private SearchFieldAction(final ProMTextField searchField, JCheckBox searchApproximate,
				LogViewMaster masterView, LogViewDetail detailView, final Collection<XTrace> traceCollection) {
			this.searchApproximate = searchApproximate;
			this.masterView = masterView;
			this.detailView = detailView;
			this.searchField = searchField;
		}

		public void search(final SearchType searchType) {
			if (!isSearching) {
				isSearching = true;
				searchField.getTextField().setEnabled(false);
				masterView.getMasterList().beforeUpdate();
				detailView.getDetailList().clear();
				detailView.getAttributesPanel().clear();
				new SwingWorker<Iterable<XTrace>, Void>() {

					protected Iterable<XTrace> doInBackground() throws Exception {

						String searchQuery = searchField.getText().trim();
						if (searchApproximate.isSelected()) {
							searchQuery = addApproximateMatching(searchQuery);
						}

						if (logViewModel.hasIndex()) {
							// Indexed search
							if (searchQuery.isEmpty()) {
								return logViewModel.getAllTraces();
							} else {
								if (searchType == SearchType.TRACES) {
									return logViewModel.getIndexedTraces().retrieve(searchQuery);
								} else {
									Iterable<XTrace> traceSubset = logViewModel.getIndexedTraces()
											.retrieve(searchQuery);
									return filterEvents(traceSubset, searchQuery);
								}
							}

						} else {
							// Normal search
							if (searchQuery.isEmpty()) {
								return logViewModel.getAllTraces();
							} else {
								Iterable<XTrace> traceResult = XIndex.filterTracesOnAttributesOrNames(
										logViewModel.getAllTraces(), searchQuery,
										new AttributeTypeResolverNaiveImpl(logViewModel.getAllTraces()));
								if (searchType == SearchType.TRACES) {
									return traceResult;
								} else {
									return filterEvents(traceResult, searchQuery);
								}
							}
						}

					}

					private String addApproximateMatching(String query) {
						if (!query.isEmpty() && !(query.startsWith("%") || (query.contains("=") || query.contains(">")
								|| query.contains("<") || query.contains("%") || query.contains("~")))) {
							query = "%" + query;
						}
						return query;
					}

					private Iterable<XTrace> filterEvents(final Iterable<XTrace> traces, String searchQuery)
							throws ParseException {
						XIndexedEvents indexedEvents = XIndex.newEvents(Collections.<XEvent>emptyList());
						// Attribute resolver is a workaround for missing type information on a global level
						Query<XEvent> eventQuery = indexedEvents.parseQuery(searchQuery,
								new AttributeTypeResolverNaiveImpl(traces));

						XFactory factory = XFactoryRegistry.instance().currentDefault();
						Collection<XTrace> filteredLog = new ArrayList<>();
						for (XTrace trace : traces) {
							XTrace filteredTrace = factory.createTrace(trace.getAttributes());
							for (XEvent event : trace) {
								if (eventQuery.matches(event, QueryFactory.queryOptions(
										QueryFactory.deduplicate(DeduplicationStrategy.LOGICAL_ELIMINATION)))) {
									filteredTrace.add(event);
								}
							}
							if (!filteredTrace.isEmpty()) {
								// No need to clone we are just showing a view of the log: attributes remain un-copied
								filteredLog.add(filteredTrace);
							}
						}
						return filteredLog;
					}

					protected void done() {
						isSearching = false;
						masterView.getMasterList().afterUpdate();
						searchField.getTextField().setEnabled(true);
						try {
							Iterable<XTrace> filteredTraces = get();
							masterView.getMasterList().getList().clearSelection();
							masterView.reloadTraces(filteredTraces);
						} catch (InterruptedException | ExecutionException | RuntimeException e) {
							if (e.getCause() instanceof ParseException) {
								ProMUIHelper.showErrorMessage(searchField,
										"Error parsing the query. \n\n" + e.getCause().getMessage(), "Query Error", e);
							} else {
								ProMUIHelper.showErrorMessage(searchField,
										"An error occured while filtering the event log.", "Search Error", e);
							}
						}
					}

				}.execute();
			} else {
				ProMUIHelper.showWarningMessage(searchField, "Please wait until current search finished!",
						"Search in progress");
			}
		}
	}

	abstract private static class PopupMouseAdapter extends MouseAdapter {

		abstract protected void showMenu(MouseEvent e);

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				showMenu(e);
		}

		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				showMenu(e);
		}
	}

	private static final class MasterMouseAdapter extends PopupMouseAdapter {

		private final LogViewContext context;

		private final LogViewMaster masterView;
		private final LogViewDetail detailView;

		private final LogViewModel logViewModel;

		private MasterMouseAdapter(LogViewContext context, LogViewMaster masterView, LogViewDetail detailView,
				LogViewModel logViewModel) {
			this.context = context;
			this.masterView = masterView;
			this.detailView = detailView;
			this.logViewModel = logViewModel;
		}

		@Override
		protected void showMenu(MouseEvent e) {
			final JPopupMenu menu = new JPopupMenu();
			if (masterView.getMasterList().getList().getSelectedIndex() != -1) {
				final int countSelected = masterView.getMasterList().getList().getSelectedIndices().length;

				final JMenuItem menuItemExport = new JMenuItem(
						String.format("Export %s selected trace variant(s) as log", countSelected));
				menuItemExport.addActionListener(new ActionGroupExport(masterView, context));
				menu.add(menuItemExport);

				final JMenuItem menuSaveAs = new JMenuItem(
						String.format("Export %s selected trace variant(s) as image", countSelected));
				menuSaveAs.addActionListener(
						new SaveAsActionListener<>(masterView.getMasterList(), masterView.getCurrentOrdering()));
				menu.add(menuSaveAs);

				final JMenuItem menuItemRemove = new JMenuItem(String.format(
						"Remove %s selected trace variant(s) (CAREFUL this changes the input log)", countSelected));
				ActionRemoveFromMaster removeAction = new ActionRemoveFromMaster(masterView, detailView,
						logViewModel.getAllTraces());
				menuItemRemove.addActionListener(removeAction);
				menu.add(menuItemRemove);
			}
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private static final class DetailMouseAdapter extends PopupMouseAdapter {

		private final LogViewContext context;

		private final LogViewMaster masterView;
		private final LogViewDetail detailView;

		private final LogViewModel logViewModel;

		private DetailMouseAdapter(LogViewContext context, LogViewMaster masterView, LogViewDetail detailView,
				LogViewModel logViewModel) {
			this.context = context;
			this.masterView = masterView;
			this.detailView = detailView;
			this.logViewModel = logViewModel;
		}

		@Override
		protected void showMenu(MouseEvent e) {
			JPopupMenu menu = new JPopupMenu();
			if (detailView.getDetailList().getList().getSelectedIndex() != -1) {

				final int countSelected = detailView.getDetailList().getList().getSelectedIndices().length;
				final JMenuItem menuItemExport = new JMenuItem(
						String.format("Export %s selected traces as log", countSelected));
				menuItemExport.addActionListener(new ActionDetailExport(context, detailView));
				menu.add(menuItemExport);

				final JMenuItem menuSaveAs = new JMenuItem(
						String.format("Export %s selected traces as image", countSelected));
				menuSaveAs.addActionListener(
						new SaveAsActionListener<>(detailView.getDetailList(), detailView.getSortOrder()));
				menu.add(menuSaveAs);

				final JMenuItem menuItemRemove = new JMenuItem(String
						.format("Remove %s selected trace(s)  (CAREFUL this changes the input log)", countSelected));
				menuItemRemove.addActionListener(
						new ActionRemoveFromDetail(masterView, detailView, logViewModel.getAllTraces()));
				menu.add(menuItemRemove);

			}
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private final LogViewMaster masterView;
	private final LogViewDetail detailView;

	private final JPanel detailFilterPanel;
	private LogViewModel logViewModel;

	public LogViewVisualizer(final LogViewContext context, final Collection<XTrace> log) {
		this(context, log, createEventClasses(log));
	}

	public static XEventClasses createEventClasses(Collection<XTrace> traces) {
		XEventClassifier classifier = obtainClassifier(traces);
		return createEventClasses(traces, classifier);
	}

	public static XEventClasses createEventClasses(Collection<XTrace> traces, XEventClassifier classifier) {
		if (traces instanceof XLog) {
			XLog log = (XLog) traces;
			XLogInfo existingLogInfo = log.getInfo(classifier);
			if (existingLogInfo != null) {
				return existingLogInfo.getEventClasses();
			}
		}
		return deriveEventClasses(classifier, traces);
	}

	private static XEventClasses deriveEventClasses(XEventClassifier classifier, Collection<XTrace> traces) {
		XEventClasses nClasses = new XEventClasses(classifier);
		for (XTrace trace : traces) {
			nClasses.register(trace);
		}
		nClasses.harmonizeIndices();
		return nClasses;
	}

	private static XEventClassifier obtainClassifier(Collection<XTrace> traces) {
		if (traces instanceof XLog) {
			XLog log = (XLog) traces;
			for (XEventClassifier classifier : log.getClassifiers()) {
				if ((classifier.getDefiningAttributeKeys().length == 1
						&& classifier.getDefiningAttributeKeys()[0].equals(XConceptExtension.KEY_NAME))
						|| classifier.equals(XLogInfoImpl.NAME_CLASSIFIER)) {
					return classifier;
				}
			}
			return !log.getClassifiers().isEmpty() ? log.getClassifiers().get(0) : XLogInfoImpl.NAME_CLASSIFIER;
		} else {
			return XLogInfoImpl.NAME_CLASSIFIER;
		}
	}

	public static Map<XEventClass, Color> createColorMap(XEventClasses eventClasses) {
		Ordering<XEventClass> sizeOrder = new Ordering<XEventClass>() {

			public int compare(XEventClass o1, XEventClass o2) {
				return Ints.compare(o1.size(), o2.size());
			}

		}.reverse().compound(Ordering.natural());
		ImmutableList<XEventClass> listSet = sizeOrder.immutableSortedCopy(eventClasses.getClasses());
		Map<XEventClass, Color> colorMap = Maps.newHashMap();
		int i = 0;
		for (XEventClass eClass : listSet) {
			colorMap.put(eClass, ColorScheme.COLOR_BREWER_12CLASS_PAIRED.getColor(i++));
		}
		return colorMap;
	}

	public LogViewVisualizer(final PluginContext context, final Collection<XTrace> traceCollection,
			final XEventClasses eventClasses) {
		this(new LogViewContextProM(context), traceCollection, eventClasses);
	}

	public LogViewVisualizer(final LogViewContext context, final Collection<XTrace> traceCollection,
			final XEventClasses eventClasses) {
		this(context, traceCollection, eventClasses, ColoringMode.EVENTCLASS);
	}

	public LogViewVisualizer(final PluginContext context, final Collection<XTrace> traceCollection,
			final XEventClasses eventClasses, final Map<XEventClass, Color> colorMap) {
		this(new LogViewContextProM(context), traceCollection, eventClasses, colorMap);
	}

	public LogViewVisualizer(final LogViewContext context, final Collection<XTrace> traceCollection,
			final XEventClasses eventClasses, final Map<XEventClass, Color> colorMap) {
		this(context, traceCollection, eventClasses, new ColoringMode() {

			public String getName() {
				return "Custom Coloring";
			}

			public EventColoring createColoring(Iterable<XTrace> traces, final XEventClasses eventClasses) {
				return new EventColoring() {

					public Color getColor(XEvent event) {
						return colorMap.get(eventClasses.getClassOf(event));
					}
				};
			}
		});
	}

	public LogViewVisualizer(final LogViewContext context, final Collection<XTrace> traceCollection,
			final XEventClasses eventClasses, ColoringMode coloringMode) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		logViewModel = new LogViewModel(traceCollection);
		logViewModel.setEventClasses(eventClasses);
		logViewModel.setColoring(coloringMode);

		masterView = new LogViewMaster(logViewModel);
		detailView = new LogViewDetail(logViewModel);

		final JLabel searchTypeLabel = new JLabel("Filter Mode ");

		final ProMComboBox<SearchType> searchType = new ProMComboBox<>(SearchType.values());
		searchType.setMinimumSize(new Dimension(100, 30));
		searchType.setMaximumSize(new Dimension(100, 30));
		searchType.setPreferredSize(new Dimension(100, 30));
		searchType.setToolTipText(
				"Filter complete traces with matching attributes (TRACES) or only on matching events (EVENTS) in each trace.");

		final ProMTextField searchField = new ProMTextField();
		searchField.setMinimumSize(new Dimension(150, 30));
		searchField.setHint("SQL-like filter by trace/event names or by attributes (attr1=val1)");
		searchField.getTextField().setToolTipText(
				"<html>Supports a simple query language for filtering traces by attributes of events. Possible queries:"
						+ "<br>- (Standard) Searches for 'concept:name' attributes of traces and events (Use '~' as 1st character to use a java regular expressions, Use '%' as 1st character to use a 'contains' query)."
						+ "<br>- Searches for trace/event attributes with syntax: attributeName OPERATOR attributeValue"
						+ "<br>  Operators are =, >, <, !=, >=, <=, % (contains), ~ (java regex), and terms can be connected with AND, OR"
						+ "<br>  Please note, some operators only work with numeric/date attributues and that all matching is case-sensitive.</html>");

		JCheckBox searchApproximate = SlickerFactory.instance().createCheckBox("Match Sub-string", true);
		searchApproximate.setHorizontalTextPosition(SwingConstants.LEFT);

		final SearchFieldAction searchAction = new SearchFieldAction(searchField, searchApproximate, masterView,
				detailView, traceCollection);
		searchField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				searchAction.search((SearchType) searchType.getSelectedItem());
			}
		});
		searchType.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				searchAction.search((SearchType) searchType.getSelectedItem());
			}
		});
		searchApproximate.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				searchAction.search((SearchType) searchType.getSelectedItem());
			}
		});

		JButton helpButton = SlickerFactory.instance().createButton("?");
		helpButton.setMinimumSize(new Dimension(30, 30));
		helpButton.setPreferredSize(new Dimension(30, 30));
		helpButton.setMaximumSize(new Dimension(30, 30));
		helpButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JEditorPane pane = new JEditorPane("text/html", QUERY_HELP_TEXT);
				pane.setEditable(false);
				JOptionPane.showMessageDialog(detailView, pane, "Query Syntax Help", JOptionPane.INFORMATION_MESSAGE);
			}

		});
		helpButton.setToolTipText(QUERY_HELP_TEXT);

		final JTable attributesTable = detailView.getAttributesPanel().getAttributesTable();
		attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributesTable.addMouseListener(new PopupMouseAdapter() {

			protected void showMenu(MouseEvent e) {
				int row = attributesTable.rowAtPoint(e.getPoint());
				int column = attributesTable.columnAtPoint(e.getPoint());

				if (!attributesTable.isRowSelected(row)) {
					attributesTable.changeSelection(row, column, false, false);
				}

				int rowIndex = attributesTable.getSelectedRow();
				String attributeKey = (String) attributesTable.getModel().getValueAt(rowIndex, 0);
				String attributeType = (String) attributesTable.getModel().getValueAt(rowIndex, 1);
				String attributeValue = (String) attributesTable.getModel().getValueAt(rowIndex, 2);

				final JPopupMenu menu = new JPopupMenu();

				if (attributeType.equalsIgnoreCase("DISCRETE") || attributeType.equalsIgnoreCase("CONTINUOUS")) {
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "=" + attributeValue));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "!=" + attributeValue));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + ">" + attributeValue));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "<" + attributeValue));
				} else if (attributeType.equalsIgnoreCase("TIMESTAMP")) {
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "=\"" + attributeValue + "\""));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "!=\"" + attributeValue + "\""));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + ">\"" + attributeValue + "\""));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "<\"" + attributeValue + "\""));
				} else {
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "=\"" + attributeValue + "\""));
					menu.add(createFilterOption(searchType, searchField, searchAction,
							attributeKey + "!=\"" + attributeValue + "\""));
				}

				menu.show(e.getComponent(), e.getX(), e.getY());
			}

			private JMenuItem createFilterOption(final ProMComboBox<SearchType> searchType,
					final ProMTextField searchField, final SearchFieldAction searchAction, final String searchQuery) {
				JMenuItem filterMenu = new JMenuItem(String.format("Filter on %s", searchQuery));
				filterMenu.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						searchField.setText(searchQuery);
						searchAction.search((SearchType) searchType.getSelectedItem());
					}
				});
				return filterMenu;
			}

		});

		detailFilterPanel = new JPanel();
		detailFilterPanel.setLayout(new BoxLayout(detailFilterPanel, BoxLayout.X_AXIS));
		detailFilterPanel.setOpaque(false);
		detailFilterPanel.setForeground(null);
		detailFilterPanel.add(searchField);
		detailFilterPanel.add(searchTypeLabel);
		detailFilterPanel.add(searchType);
		detailFilterPanel.add(searchApproximate);
		detailFilterPanel.add(helpButton);
		
		detailView.add(detailFilterPanel, 0);
		// masterView.getToolbar().add(detailFilterPanel);
		// we need to ass detailFilterPanel into MasterView?? But it doesn't matter, maybe
		masterView.getMasterList().getList()
				.addMouseListener(new MasterMouseAdapter(context, masterView, detailView, logViewModel));
		/*
		detailView.getDetailList().getList()
				.addMouseListener(new DetailMouseAdapter(context, masterView, detailView, logViewModel));
		*/
		masterView.reloadTraces(traceCollection);

		add(new ProMTraceListMasterDetail<>(masterView, detailView));
	}

	public LogViewMaster getMasterView() {
		return masterView;
	}

	public LogViewDetail getDetailView() {
		return detailView;
	}

	public JPanel getDetailFilterPanel() {
		return detailFilterPanel;
	}

}
