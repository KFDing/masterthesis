package org.processmining.incorporatenegativeinformation.plugins;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.help.Configuration;

/**
 * this plugin is used to 
 * -- add one trace attribute to the event log
 * -- modify the trace attribute to different type, but I guess they have such plugin
 * -- modify the trace attribute according to event attribute
 * -- some regrex expression, is also fine..     
 * It is not so easy to generalize the codes.. lalanana
 * @author dkf
 *
 */
@Plugin(name = "Manipulate Trace Attribute", parameterLabels = { "a log" }, //
returnLabels = { "modified log" }, returnTypes = {
		XLog.class }, userAccessible = true, help = "Adapted Filter traces and individual events from the log based on the presence or "
				+ "absence of attributes with particular values.", mostSignificantResult = 1)
public class TraceAttributeManipulatePlugin {
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Add Trace Attribute", requiredParameterLabels = { 0 })
	public XLog addTraceFromEvent(PluginContext context, XLog log) {
		XLog nlog = (XLog) log.clone(); 
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		nlog.getGlobalTraceAttributes().add(factory.createAttributeBoolean(Configuration.POS_LABEL, true, null));
		// should we independent on it ?? or not?
		String eventAttrKey = "DA_11_NUM";
		for(XTrace trace: nlog) {
			XAttributeBoolean attrPos = factory.createAttributeBoolean(Configuration.POS_LABEL, true, null);
			// get event attribute
			XEvent event = trace.get(0);
			// show all the attributes
			if(event.getAttributes().containsKey(eventAttrKey)) {
				XAttributeDiscrete attr =  (XAttributeDiscrete) event.getAttributes().get(eventAttrKey);
				
				if(attr.getValue() == 0) {
					attrPos.setValue(false);
				}else {
					attrPos.setValue(true);
				}
			}
			trace.getAttributes().put(attrPos.getKey(), attrPos);
		}
		return nlog;
	}
	
}
