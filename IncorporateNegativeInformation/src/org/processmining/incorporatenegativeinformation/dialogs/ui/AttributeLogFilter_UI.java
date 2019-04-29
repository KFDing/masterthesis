package org.processmining.incorporatenegativeinformation.dialogs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.incorporatenegativeinformation.models.AttributeLogFilter;

import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AttributeLogFilter_UI extends BorderPanel {

	private static final long serialVersionUID = 1L;

	public final static String DIALOG_TITLE = "Filter Log by Properties";
	public final static String ATTRIBUTE_DIALOG_TITLE = "Filter Log by Attributes";

	private XLog log;

	private JCheckBox attribute_filter_include_box;
	private ProMComboBox<String> attribute_filter_filter_on;
	private ProMComboBox<String> attribute_filter_log_attributes;
	private ProMComboBox<String> attribute_filter_log_values;

	@SuppressWarnings("unchecked")
	public AttributeLogFilter_UI(AttributeLogFilter filter) {
		super(0, 0);

		this.log = filter.log;

		SlickerTabbedPane tabs = SlickerFactory.instance().createTabbedPane("", WidgetColors.COLOR_LIST_BG,
				WidgetColors.COLOR_LIST_FG, Color.GREEN);
		setLayout(new BorderLayout());
		add(tabs);

		ProMPropertiesPanel attributePanel = new ProMPropertiesPanel(ATTRIBUTE_DIALOG_TITLE);
		tabs.addTab(ATTRIBUTE_DIALOG_TITLE, attributePanel);
		attribute_filter_filter_on = new ProMComboBox<String>(new Object[] { AttributeLogFilter.NONE,
				AttributeLogFilter.TRACE_ATTRIBUTE, AttributeLogFilter.EVENT_ATTRIBUTE });
		attributePanel.addProperty("filter on", attribute_filter_filter_on);
		attribute_filter_filter_on.addActionListener(new FilterOnListener());

		// use
		attribute_filter_log_attributes = new ProMComboBox<String>(
				getAttributes((String) attribute_filter_filter_on.getSelectedItem()));
		attributePanel.addProperty("attribute", attribute_filter_log_attributes);
		attribute_filter_log_attributes.addActionListener(new AttributeListener());

		Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
				(String) attribute_filter_log_attributes.getSelectedItem());
		// here to get the group atrribute
		filter.attribute_group_values.addAll(values);
		attribute_filter_log_values = new ProMComboBox<String>(values);
		MyComboBoxModel model = new MyComboBoxModel(MyComboBoxModel.NONE);
		attribute_filter_log_values.setModel(model);
		model.setSelectedItem(null);
		attributePanel.addProperty("value", attribute_filter_log_values);

		attribute_filter_include_box = SlickerFactory.instance().createCheckBox(null, true);
		attributePanel.addProperty("keep matching traces", attribute_filter_include_box);

		setFilterValues(filter);
	}

	/**
	 * Set values of controls based on values in the filter.
	 * 
	 * @param filter
	 */
	protected void setFilterValues(AttributeLogFilter filter) {
		attribute_filter_filter_on.setSelectedItem(filter.attribute_filterOn);
		attribute_filter_include_box.setSelected(filter.attribute_include);
		if (filter.attribute_key != null)
			attribute_filter_log_attributes.setSelectedItem(filter.attribute_key);
		if (filter.attribute_values != null)
			for (Object o : filter.attribute_values) {
				attribute_filter_log_values.setSelectedItem(o);
			}

	}

	/**
	 * display a dialog to ask user what to do
	 * 
	 * @param context
	 * @return
	 */
	protected InteractionResult getUserChoice(UIPluginContext context) {
		return context.showConfiguration(DIALOG_TITLE, this);
	}

	/**
	 * Populate filter object from settings in the panel.
	 * 
	 * @param filter
	 */
	@SuppressWarnings("unchecked")
	public void getFilterValues(AttributeLogFilter filter) {
		filter.attribute_include = attribute_filter_include_box.isSelected();
		filter.attribute_filterOn = (String) attribute_filter_filter_on.getSelectedItem();
		filter.attribute_key = (String) attribute_filter_log_attributes.getSelectedItem();
		filter.attribute_values.addAll((List<String>) attribute_filter_log_values.getSelectedItem());

		filter.attribute_group_values.clear();
		filter.attribute_group_values.addAll(attribute_group_values);
	}

	/**
	 * Open UI dialogue to populate the given configuration object with settings
	 * chosen by the user.
	 * 
	 * @param context
	 * @param config
	 * @return result of the user interaction
	 */
	public InteractionResult setParameters(UIPluginContext context, AttributeLogFilter filter) {
		InteractionResult wish = getUserChoice(context);
		if (wish != InteractionResult.CANCEL)
			getFilterValues(filter);
		return wish;
	}

	/**
	 * Listener to watch
	 * {@link AttributeLogFilter_UI#attribute_filter_filter_on} and store
	 * attribute names in
	 * {@link AttributeLogFilter_UI#attribute_filter_log_attributes} and updated
	 * {@link AttributeLogFilter_UI#attribute_filter_log_values} accordingly
	 */
	private class FilterOnListener implements ActionListener {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == attribute_filter_filter_on) {

				TreeSet<String> attributeNames = getAttributes((String) attribute_filter_filter_on.getSelectedItem());
				attribute_filter_log_attributes.setModel(new DefaultComboBoxModel(attributeNames.toArray()));

				Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
						(String) attribute_filter_log_attributes.getSelectedItem());

				MyComboBoxModel model = new MyComboBoxModel(values.toArray());
				attribute_filter_log_values.setModel(model);
				model.setSelectedItem(null);

				//				attribute_filter_log_values.setModel(new DefaultComboBoxModel(values.toArray()));
			}
		}
	}

	private Set<String> attribute_group_values = null;

	/**
	 * Listener to watch
	 * {@link AttributeLogFilter_UI#attribute_filter_log_attributes} and store
	 * attribute names in
	 * {@link AttributeLogFilter_UI#attribute_filter_log_values}.
	 */
	private class AttributeListener implements ActionListener {

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() == attribute_filter_log_attributes) {

				Set<String> values = getValues((String) attribute_filter_filter_on.getSelectedItem(),
						(String) attribute_filter_log_attributes.getSelectedItem());
				/*
				 * System.out.println(values.size() + " on AttributeLister");
				 * for(String value: values) System.out.println(value +
				 * " on AttributeLister");
				 */
				attribute_group_values = values;
				MyComboBoxModel model = new MyComboBoxModel(values.toArray());
				attribute_filter_log_values.setModel(model);
				model.setSelectedItem(null);

				//				attribute_filter_log_values.setModel(new DefaultComboBoxModel(values.toArray()));
			}
		}
	}

	/**
	 * Collect all attribute names of the selected category from
	 * {@link AttributeLogFilter_UI#log}
	 * 
	 * @param category
	 * @return
	 */
	private TreeSet<String> getAttributes(String category) {
		TreeSet<String> attributeNames = new TreeSet<String>();
		if (category == AttributeLogFilter.TRACE_ATTRIBUTE) {
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				attributeNames.addAll(attributes.keySet());
			}

		} else if (category == AttributeLogFilter.EVENT_ATTRIBUTE) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					XAttributeMap attributes = event.getAttributes();
					attributeNames.addAll(attributes.keySet());
				}
			}
		} else if (category == AttributeLogFilter.NONE) {
			attributeNames.add("<none>");
		}
		return attributeNames;
	}
	/**
	 * Collect all attribute values of the selected category and key from
	 * {@link AttributeLogFilter_UI#log}
	 * 
	 * @param category
	 * @return
	 */
	private Set<String> getValues(String category, String key) {
		// get the global trace attribute type and check if it has this
		TreeSet<String> values = new TreeSet<String>(new AlphanumComparator());
		List<Double> numValues = new ArrayList<Double>();
			// TreeSet<String> values = new TreeSet<String>(new AlphanumComparator());
		if (category == AttributeLogFilter.TRACE_ATTRIBUTE) {
			for (XTrace t : log) {
				XAttributeMap attributes = t.getAttributes();
				if (attributes.containsKey(key)) {
					
					XAttribute attr = attributes.get(key);
					
					if(attr instanceof XAttributeContinuous || attr instanceof XAttributeDiscrete) { //attr instanceof XAttributeDiscrete || 
						XAttributeContinuous tmp = (XAttributeContinuous) attributes.get(key); 
						numValues.add(tmp.getValue());
					}else {
						values.add(attributes.get(key).toString());
					}
					
				}
			}
			
			if(numValues.size() >0) {
				Collections.sort(numValues);
				String inputString =  JOptionPane.showInputDialog(this, "Give a threshold for negative label", 0.7);
						
				double percent = Double.parseDouble(inputString);
				
				int idx = (int) (numValues.size() * percent);
				System.out.println(percent + " idx : " +idx );
				values.add(numValues.get(idx).toString());
			}

		} else if (category == AttributeLogFilter.EVENT_ATTRIBUTE) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					XAttributeMap attributes = event.getAttributes();
					if (attributes.containsKey(key))
						values.add(attributes.get(key).toString());
				}
			}
		} else if (category == AttributeLogFilter.NONE) {
			values.add("<none>");
		}
		return values;
	
	}

}

class MyComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

	private static final long serialVersionUID = 1759722993311195116L;
	public static Object NONE = "none";
	List<String> values = new ArrayList<String>();
	List<String> selected = new ArrayList<String>();

	public MyComboBoxModel(Object... values) {
		for (Object object : values) {
			if (object == null || object == NONE || !(object instanceof String)) {
				continue;
			}
			this.values.add((String) object);
		}
	}

	@Override
	public int getSize() {
		return values.size();
	}

	@Override
	public String getElementAt(int index) {
		return values.get(index);
	}

	public void setSelectedItem(Object anItem) {
		if (anItem == null || anItem == NONE || !(anItem instanceof String)) {
			if (selected.isEmpty())
				return;
			selected.clear();
		} else {
			boolean removed = selected.remove(anItem);
			if (!removed) {
				selected.add((String) anItem);
			}
		}
		fireContentsChanged(this, -1, -1);
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

}
