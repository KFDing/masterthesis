package org.processmining.plugins.ding.ui;

import java.util.List;

import javax.swing.JPanel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.traceview.masterdetail.ProMTraceListMasterDetail;
import org.processmining.plugins.ding.preprocess.TraceVariant;

public class VariantViewVisualizer extends JPanel {
	private static final long serialVersionUID = 2073714666502722213L;
	
	private final VariantsViewMaster masterView;
	private final VariantChangeView detailView;
	
	private final int var_idx = 0;
	
	public VariantViewVisualizer(PluginContext context, List<TraceVariant> variants) {
		
		masterView = new VariantsViewMaster(variants);
		detailView = new VariantChangeView(variants.get(var_idx));
		
		masterView.getMasterList().getList()
		.addMouseListener(new MasterMouseAdapter(context, masterView, detailView, variants));
		masterView.reloadTraces(traceCollection);

		add(new ProMTraceListMasterDetail<>(masterView, detailView));

	}
	

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
	
	
}
