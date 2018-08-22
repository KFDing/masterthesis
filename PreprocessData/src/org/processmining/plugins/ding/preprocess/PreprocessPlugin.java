package org.processmining.plugins.ding.preprocess;
/**
 * This class is used to preprocess event log before it is passed to other application. 
 * Preprocess includes: 
 * == throughtime of each trace
   == whole costs of each trace
*  == empty but just add the attribute to it..
*  In the end it could divide event log into different sets w.r.t. performance
*  
*  First Task of it is to add additional attributes into the event log
*  
*  
 * @author dkf
 * date: 14. August 2018 
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.utils.XUtils;


@Plugin(
		name = "Label Event Log",
		parameterLabels = {"Event log"}, 
		returnLabels = { "Laleled Log"},
		returnTypes = {XLog.class}, 
		userAccessible = true,
		help = "add additional label information into the event log "
		)

public class PreprocessPlugin {
	
    
	/**
	 * This plugin assign label information randomly to event log to each trace and generate new event log 
	 * @param context  : nothing but to create the connection use...however, later use it 
	 * @param log : event log
	 * @return labeled event log 
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label Randomly on Event Log",  requiredParameterLabels = { 0})
	public XLog assignRandomLabel(UIPluginContext context, XLog log) {
		// how to achieve it ?? 
		
		// do we need to consider the different variants?? No, we don't consider it 
		// to simplify it, we just consider the variant at first and then each trace???
		// -- random generator, if it is greater than a threshold, then assign ??? 
		// -- read and write to event attribute.. That's all..
		XLog label_log = (XLog)log.clone();
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean("label", false, null));
		// create random object
	    Random randomno = new Random();

		for (XTrace trace : label_log) {
			 // get next next boolean value 
		    boolean value = randomno.nextBoolean();
		    
		    XAttributeBoolean attr = factory.createAttributeBoolean(Configuration.LABEL_NAME, value, null);
		    trace.getAttributes().put(attr.getKey(), attr);
		}	
		return  label_log;
	}
	/**
	 * THis plugin is to add throughtime as one attribute in trace and then assign label into it
	 * @param context
	 * @param log
	 * @return labeled_log
	 */
	public XLog assignThroughTimeAttribute(XLog log) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (XTrace trace : log) {
			// timestamp from one event> .. Literal 
			Date start_time = null, end_time = null, current_time;  
			XEvent event;
			Iterator titer = trace.iterator();
			if(titer.hasNext()) {
				event =  (XEvent) titer.next();
				start_time = end_time =  XUtils.getTimestamp(event);
			}
			while (titer.hasNext()) {
				event =  (XEvent) titer.next();
				current_time= XUtils.getTimestamp(event);
				 if(current_time.before(start_time))
					 start_time = current_time;
				 if(current_time.after(end_time))
					 end_time = current_time;
			}
			// in Milliseconds format
			long throughput_time = end_time.getTime() - start_time.getTime();
			
			XAttributeDiscrete attr = factory.createAttributeDiscrete(Configuration.TP_TIME, throughput_time, null); 
			trace.getAttributes().put(attr.getKey(), attr);
		}	
		return  log;
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Assign Label w.r.t. Throughputtime",  requiredParameterLabels = { 0})
	public XLog assignLabel(UIPluginContext context, XLog log) {
		XLog label_log = (XLog)log.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		// how to decide the throughputtime of each trace?? 
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean("label", false, null));
		
		assignThroughTimeAttribute(label_log);

		// if the throughputtime is over the percentage of 70%, then we assign above it wrong..
		// double percentage = 0.7;
		
		// here we need to create the data sets.. Index, and throughputtime
		// kind of complex, so I need to push it back.. Now,just to get the mean value
		ArrayList<Long> time_list = new ArrayList<Long>();
		double sum = 0.0; int count = 0;
		for (XTrace trace : label_log) {
			// if it has throughputtime attribute, then we put them into a list 
			if(trace.getAttributes().containsKey(Configuration.TP_TIME)) {
				XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(Configuration.TP_TIME);
				// time_list.add(attr.getKey());
				time_list.add(attr.getValue());
				sum += attr.getValue();
				count++;
			}	
		}
		// find the mean value and split it into two sets// how to make sure that they have the same traverse order??
		double mean = sum/count;
		for (XTrace trace : label_log) {
			if(trace.getAttributes().containsKey(Configuration.TP_TIME)) {
				XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(Configuration.TP_TIME);
				if(attr.getValue() > mean) {
					// label it into one class 
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.LABEL_NAME, false, null);
				    trace.getAttributes().put(nattr.getKey(), nattr);
				}else {
					// label it into another class
					XAttributeBoolean nattr = factory.createAttributeBoolean(Configuration.LABEL_NAME, true, null);
				    trace.getAttributes().put(nattr.getKey(), nattr);
				}
			}		
		}
		return  label_log;
	}
}
