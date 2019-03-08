package org.processmining.incorporatenegativeinformation.models;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AttributeLogFilter {

	public static final String NONE = "none";
	public static final String TRACE_ATTRIBUTE = "trace attribute";
	public static final String EVENT_ATTRIBUTE = "trace with an event having this attribute";

	public String attribute_filterOn = TRACE_ATTRIBUTE;

	public boolean attribute_include = true;
	public String attribute_key = null;
	public Set<String> attribute_values = new HashSet<String>();
	public Set<String> attribute_group_values = new HashSet<String>();
	
	
	public XLog log;

	public AttributeLogFilter() {
		setDefaultValues();
	}
	
	public AttributeLogFilter(XLog log) {
		this.log = log;
		setDefaultValues();
	}
	
	public AttributeLogFilter(String filterOn, String key, Set<String> values) {
		attribute_filterOn = filterOn;
		attribute_key = key;
		attribute_values.addAll(values);
	}
	
	public void setAttributeKey(String key) {
		attribute_key = key;
	}

	public void setFilterOn(String filterOn) {
		attribute_filterOn = filterOn;
	}
	
	public void setAttributeValue(Set<String> values) {
		attribute_values.addAll(values);
	}
	
	public void setDefaultValues() {
		this.attribute_filterOn = NONE;
	}

	public boolean satisfies(XAttributeMap attributes) {
		if (!attributes.containsKey(attribute_key)) {
			return false;
		}
		XAttribute attr = attributes.get(attribute_key);
		// the only way to get the value consistently out of all the attribute subclasses
		String attr_value = attr.toString();

		// here we specify it to all values on it, we need to generate all the list of them,
		// but they should be in a group, not like this, only return true, is ok
		
		return attribute_values.contains(attr_value);
	}

	public boolean keepTraceOnAttributes(XTrace trace) {

		if (attribute_filterOn == TRACE_ATTRIBUTE) {
			XAttributeMap attributes = trace.getAttributes();
			if (satisfies(attributes)) {
				return attribute_include;
			} else {
				return !attribute_include;
			}

		} else if (attribute_filterOn == EVENT_ATTRIBUTE) {
			for (XEvent e : trace) {
				XAttributeMap attributes = e.getAttributes();
				if (satisfies(attributes)) {
					if (attribute_include)
						return true;
					else
						return false;
				}
			}
			if (attribute_include)
				return false;
			else
				return true;
		}
		return false;
	}


	public boolean keepTrace(XTrace trace) {

		if (attribute_filterOn != NONE) {
			if (!keepTraceOnAttributes(trace))
				return false;
		}
		
		return true;
	}

	/** 
	 * used to find the 
	 * @param t
	 * @return
	 */
	public String findGroup(XTrace trace) {
		// TODO  we need to return the att -value it contains
		if (attribute_filterOn != NONE) {
			if (attribute_filterOn == TRACE_ATTRIBUTE) {
				XAttributeMap attributes = trace.getAttributes();
				XAttribute attr = attributes.get(attribute_key);
				// the only way to get the value consistently out of all the attribute subclasses
				String attr_value = attr.toString();
				return attr_value;
			}
		}
		return null;
	}

	// here we create the groupAttributeValues
	public Set<String> groupAttributeValues() {
		return attribute_group_values;
	}
	
	// get all the attributes of filter  
	public String toString() {
		String label="";
		label += attribute_filterOn +":" +attribute_key + ":" ;
		return label;
	}

}