package org.processmining.incorporatenegativeinformation.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.dialogs.ui.AttributeLogFilter_UI;
import org.processmining.incorporatenegativeinformation.models.AttributeLogFilter;
/**
 * The original file is written by Dirk Fanhland, but modified by Kefang Ding to adapt needs. 
 * The output includes several logs which are based on the attributes splitting. 
 * Input is one log  + filter setting
 * Filter Setting: trace variant, attribute value(Specific, or based on all values?? group them together)
 * 
 * For the choices of item, and the satisfaction rules. Both changes, on it 
 * Delete the choise, filter by length!! Find its panel, and change them to one
 * But the parameters should be also different, but not so many.    
 * 
 * 
 * @author dkf
 *
 */
@Plugin(name = "Adpated Filter Log by Attributes",
	parameterLabels = { "a log", "filter settings"}, //
	returnLabels = { "filtered log" },
	returnTypes = { XLog.class }, 
	userAccessible = true,
	help = "Adapted Filter traces and individual events from the log based on the presence or absence of attributes with particular values.",
	mostSignificantResult = 1)
public class AttributeLogFilterPlugin {
	
	// take log and net as input and guess initial marking
	@UITopiaVariant(
			affiliation="RWTH Aachen",
			author="Kefang",
			email="***@gmail.com")
	@PluginVariant(variantLabel = "Adapted Filter Log by Attributes", requiredParameterLabels = { 0 })
	public XLog filterLog(UIPluginContext context, XLog log) {
		
		AttributeLogFilter filter = new AttributeLogFilter(log);
		AttributeLogFilter_UI ui = new AttributeLogFilter_UI(filter);
		if (ui.setParameters(context, filter) != InteractionResult.CANCEL)
			return filterLog(context, log, filter);
		else
			return cancel(context, "Canceled by user.");
				
	}
	// we need one method to define the if it is group or filter on one value
	private boolean isToFilter(AttributeLogFilter filter) {
		// we check the value of fitler values
		Set<String> attr_values = filter.attribute_values;
		if(attr_values.size() <1)
			return false;
		return true;
	}
	
	// even one log, I need to create one list to show them..
	@PluginVariant(variantLabel = "Filter Log by Attributes", requiredParameterLabels = { 0, 1 })
	public XLog filterLog(PluginContext context, XLog log, AttributeLogFilter filter) {

		if(!isToFilter(filter)) {
			return groupLog(context, log, filter).get(0);
		}
		
		XFactory f = XFactoryRegistry.instance().currentDefault();
		
		// create new log, copy original attributes
		XLog filtered = f.createLog(log.getAttributes());
		
		
		// HV: Copy log metadata.
		filtered.getExtensions().addAll(log.getExtensions());
		filtered.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		filtered.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		filtered.getClassifiers().addAll(log.getClassifiers());
		
		for (XTrace t : log) {
			if (filter.keepTrace(t)) {
				filtered.add(t);
			}
		}
		
		String attr_value = filter.attribute_values.toArray()[0].toString();
		
		String logName = XConceptExtension.instance().extractName(log);
		if (logName == null) logName = "log";
		logName = logName+" (filtered @ "+filter.toString() + attr_value+")";
		context.getFutureResult(0).setLabel(logName);
		
		return filtered;
	}

	// this method group log according to its attribute values, the first output is shown directly, but the others
	// are output directly into working space
	public List<XLog> groupLog(PluginContext context, XLog log, AttributeLogFilter filter) {

		XFactory f = XFactoryRegistry.instance().currentDefault();
		List<String> log_names = new ArrayList<>();
		List<XLog> logs = new ArrayList<XLog>();
		// we name logs by its attributes on it, and then put them together, so.. here we should test them already!!!
		// to show a list of logs  
		// get the list of attributes values.. according to log, not to filter
		Object[] attr_values = filter.groupAttributeValues().toArray();
		
		// only for one value and one log?? It can be a lot of confusing.. Let me travese log once
		for(int i=0; i< attr_values.length;i++) {
			String value = attr_values[i].toString();
			
			// create new log, copy original attributes
			XLog filtered = f.createLog(log.getAttributes());
			
			
			// HV: Copy log metadata.
			filtered.getExtensions().addAll(log.getExtensions());
			filtered.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
			filtered.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
			filtered.getClassifiers().addAll(log.getClassifiers());
			
			
			String logName = XConceptExtension.instance().extractName(log);
			if (logName == null) logName = "log";
			logName = logName+" (filtered @ "+ filter.toString() +value+")";
			
			// put the resulted log into working set
			log_names.add(logName);
			logs.add(filtered);
			
		}
		
		// for each trace, we find its corresponding event log, add them to this event log
		// we get the attribute value from it
		for (XTrace t : log) {
			// find the corresponding group key for it
			String group_key = filter.findGroup(t);
			int idx = findGroupIdx(group_key, log_names);
			logs.get(idx).add(t);
			
		}
		for(int i=1; i< logs.size(); i++) {
			context.getProvidedObjectManager().createProvidedObject(log_names.get(i), logs.get(i), XLog.class, context);
		}
		
		return logs;
	}
	
	
	

	private int findGroupIdx(String log_key, List<String> log_names) {
		// TODO Auto-generated method stub
		for(int i=0;i<log_names.size();i++) {
			if(log_names.get(i).contains(log_key))
				return i;
		}
		return -1;
	}

	protected static XLog cancel(PluginContext context, String message) {
		System.out.println("[AttributeFilter]: "+message);
		context.log(message);
		context.getFutureResult(0).cancel(true);
		return null;
	}
}
