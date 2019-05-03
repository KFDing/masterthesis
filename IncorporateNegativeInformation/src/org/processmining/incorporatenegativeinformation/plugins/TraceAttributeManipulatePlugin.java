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
 * -- add one trace attribute call KPI from pos_outcome
 * -- add this trace into trace attribute and also into event attributes
 * It is not so easy to generalize the codes.. lalanana
 * @author dkf
 *
 */
@Plugin(name = "Manipulate Trace Attribute with Converting and Adding", parameterLabels = { "a log" }, //
returnLabels = { "modified log" }, returnTypes = {
		XLog.class }, userAccessible = true, help = "Adapted Filter traces and individual events from the log based on the presence or "
				+ "absence of attributes with particular values.", mostSignificantResult = 1)
public class TraceAttributeManipulatePlugin {
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Add Trace Attribute", requiredParameterLabels = { 0 })
	public XLog addTraceFromEvent(PluginContext context, XLog log) {
		XLog nlog = (XLog) log.clone(); 
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		nlog.getGlobalTraceAttributes().add(factory.createAttributeDiscrete(Configuration.KPI_LABEL, 0, null));
		// createAttributeBoolean(Configuration.KPI_LABEL, true, null));
		// should we independent on it ?? or not?
		String eventAttrKey = "DA_11_NUM";
		for(XTrace trace: nlog) {
			if(trace.getAttributes().containsKey(Configuration.POS_LABEL)) {
				XAttributeBoolean attrPos = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				XAttributeDiscrete attrKPI = factory.createAttributeDiscrete(Configuration.KPI_LABEL, 0, null);
				if(attrPos.getValue()) {
					attrKPI.setValue(1);
				}else {
					attrKPI.setValue(0);
				}
				for(XEvent event: trace) {
					event.getAttributes().put(attrKPI.getKey(), attrKPI);
				}
				trace.getAttributes().put(attrKPI.getKey(), attrKPI);
			}
		}
		return nlog;
	}
	
}
