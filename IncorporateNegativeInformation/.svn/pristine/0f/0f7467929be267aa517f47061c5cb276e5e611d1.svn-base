package org.processmining.incorporatenegativeinformation.help;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.incorporatenegativeinformation.models.LabeledTraceVariant;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;



/**
 * This class includes the basic information about Event log 
 * and provide utilities for operation
 * @author dkf
 *
 */
public class EventLogUtilities {

	
	public static List<XEventClass> transferTrace( XLog log, XTrace trace, XEventClassifier classfier) {
		// we need to add the classfier for it..
		List<XEventClass> seq = new ArrayList<XEventClass>();
		
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
		XEventClass eventClass = null;
		for (XEvent event : trace) {
			// log.getClassifiers().get(0).getClassIdentity(event);
			eventClass = info.getNameClasses().getClassOf(event);
			seq.add(eventClass);	
		}
		return seq;
	} 
	
	/**
     * we build one map between XEventClasses and Transtitions in Petri Net, if we use the event classifie
     * we should stick on it, else, we should use the other ones..
     * @param eventClasses
     * @param transitions
     * @return
     */
	public static Map<XEventClass, Transition> getEventTransitionMap(XLog log, Petrinet net, XEventClassifier classifier) {
		// too complex, so now, I will just change back original ones.
		Map<XEventClass, Transition> map = new HashMap<XEventClass, Transition>();
		Collection<Transition> transitions = net.getTransitions();
		XEventClasses classes = null;
		
		if(classifier != null && log.getClassifiers().contains(classifier)) 
			classes = XLogInfoFactory.createLogInfo(log).getEventClasses(classifier);
		else
			classes = XLogInfoFactory.createLogInfo(log).getNameClasses();
		
		XEventClass tauClassinLog = new XEventClass(Configuration.Tau_CLASS, classes.size());
		
		boolean match;
		for (Transition transition : transitions) {
			match = false;
			for (XEventClass eventClass : classes.getClasses()) { // transition.getLabel()
				// here we need to create a mapping from event log to graphs 
				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(eventClass, transition);
					match = true;
					break;
				}
			}
			if(! match) {
				// here sth not so good about numm eventClass, we can create one eventClass marked to be tau in log
				
				map.put(tauClassinLog, transition);
			}
		}
		// three cases: silent transition
		// in net but not shown in event, how to match them??? Then return null
		// in event log but not in net  // return null .
		return map; 
	}
	
	
	
	public static void exportSingleLog(XLog log, String targetName) throws IOException {
		FileOutputStream out = new FileOutputStream(targetName);
		XSerializer logSerializer = new XesXmlSerializer();
		logSerializer.serialize(log, out);
		out.close();
	}
	/**
	 * here we summary the log information, including variant and then show it in a visualizer
	 * how should we use it?? If randomness is uncontrolled, but we need to control it.. 
	 * @param log
	 * @return variants information, a new class to contain it ..
	 */
	public static List<TraceVariant> getTraceVariants( XLog log) {
		
		List<TraceVariant> variants = new ArrayList<TraceVariant>();
		XEventClass eventClass = null;
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
	
		// this step I need to get it from index from log
		for (int idx = 0; idx < log.size(); idx++) {
				XTrace trace = log.get(idx);
				
				List<XEventClass> toTraceClass = new ArrayList<XEventClass>();
				for (XEvent toEvent : trace) {
					// eventClass = info.getEventClasses().getClassOf(toEvent);
					eventClass = info.getNameClasses().getClassOf(toEvent);
					toTraceClass.add(eventClass);	
				}
				
				int i = 0;
				for(; i< variants.size();i++) {
					// how to add the new variant into list
					if((variants.get(i).getTraceVariant()).equals(toTraceClass)) {
						variants.get(i).addTrace(trace, idx);
						// it should add also information on the trace
						break;
					}
				}
				if (i==variants.size()) {
					// not found in it, then we need to add it into the list
					variants.add(new TraceVariant(toTraceClass,trace, idx));
				}	
			}
		return variants;
	} 
	/**
	 * create the labeled TraceVariants
	 * @param log
	 * @param classifier 
	 * @return
	 */
	public static List<LabeledTraceVariant> variants ;
	public static List<LabeledTraceVariant> getLabeledTraceVariants( XLog log, XEventClassifier classifier) {
		
		if(variants == null) {
			variants = new ArrayList<LabeledTraceVariant>();
			System.out.println("First time to create variant");
		}
		else { 
			System.out.println("Other time to use variant");
			return variants;
		}
		XEventClass eventClass = null;
		XLogInfo info = XLogInfoFactory.createLogInfo(log);
	
		// this step I need to get it from index from log
		for (int idx = 0; idx < log.size(); idx++) {
				XTrace trace = log.get(idx);
				boolean isPos = false;
				
				XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
				if(attr == null || attr.getValue()) {
					isPos = true;
				}
				
				List<XEventClass> toTraceClass = new ArrayList<XEventClass>();
				for (XEvent toEvent : trace) {
					// eventClass = info.getEventClasses().getClassOf(toEvent);
					eventClass = info.getNameClasses().getClassOf(toEvent);
					toTraceClass.add(eventClass);	
				}
				
				int i = 0;
				for(; i< variants.size();i++) {
					// how to add the new variant into list
					if((variants.get(i).getTraceVariant()).equals(toTraceClass)) {
						variants.get(i).addTrace(trace, idx, isPos);
						// it should add also information on the trace
						break;
					}
				}
				if (i==variants.size()) {
					// not found in it, then we need to add it into the list
					variants.add(new LabeledTraceVariant(toTraceClass,trace, idx, isPos));
				}	
			}
		return variants;
	}
	
	public static void assignVariantLabel(TraceVariant variant, String attr_name, Boolean is_true) {
		// for each trace in the variant, we create an attribution with name of attr_name, value isPos
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (XTrace trace : variant.getTrace_list()) {
		    XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, is_true, null);
		    trace.getAttributes().put(attr.getKey(), attr);
		}	
	}
	
	public static void  assignVariantLabel(TraceVariant variant, String attr_name, double prob) {
		//System.out.println(variant.getCount());
		//System.out.println("prob in assign " + prob);
		List<Integer> posidx_list = SamplingUtilities.sample(variant.getCount(), prob);
		//System.out.println(posidx_list);
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		for (int idx =0; idx < variant.getCount(); idx++) {
			XTrace trace = variant.getTrace_list().get(idx);
			if(posidx_list.contains(idx)) {
				XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, true, null);
			    trace.getAttributes().put(attr.getKey(), attr);
			}else {
				XAttributeBoolean attr = factory.createAttributeBoolean(attr_name, false, null);
			    trace.getAttributes().put(attr.getKey(), attr);
			}
		}
	}
	
	/**
	 * THis plugin is to add throughtime as one attribute in trace and then assign label into it
	 * @param context
	 * @param log
	 * @return labeled_log
	 */
	public static XLog assignThroughTimeAttribute(XLog log) {
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
	
	public static void assignVariantListLabel(List<TraceVariant> variants, double overlap_rate, double pos_rate ) {
		// for fit variants
		// overlap 0.3: get total num of variants + random index + number w.r.t. to 0.3; greater than 0.3 
		List<Integer> nolidx_list = SamplingUtilities.sample(variants.size(), 1 - overlap_rate);
		// we have odidx_list, and also we have nolidx_list, 
		// they should then decide to assign different prob to pos and neg
		List<Integer> nolpos_list = SamplingUtilities.sample(nolidx_list.size(), pos_rate);
	
		for(int idx=0; idx<nolidx_list.size(); idx++) {
			TraceVariant variant = variants.get(nolidx_list.get(idx));
			// assign pos to nooverlap variant
			if(nolpos_list.contains(idx)) {
				EventLogUtilities.assignVariantLabel(variant, Configuration.POS_LABEL, true);
			}else {
				// assign neg to nooverlap variants
				EventLogUtilities.assignVariantLabel(variant, Configuration.POS_LABEL, false);
			}
		}
		// then overlap
		// if could happen that all olidx_list is empty, so what to do then??? 
		List<Integer> olidx_list = new ArrayList<Integer>();
		for(int i=0;i<variants.size();i++)
			if(!nolidx_list.contains(i))
				olidx_list.add(i);
		
		for(int idx=0; idx< olidx_list.size();idx++) {
			TraceVariant variant = variants.get(olidx_list.get(idx));
			// overlap variant, we need to assign both but according to different threshold
			EventLogUtilities.assignVariantLabel(variant, Configuration.POS_LABEL, pos_rate);
		}
		
	}

	public static void deleteVariantFromLog(TraceVariant var, XLog log) {
		// delete var from log, now don't sure about the effect if we only delete tracelist
		Iterator liter = var.getTrace_list().iterator();
		while(liter.hasNext()) {
			XTrace trace = (XTrace) liter.next();
			log.remove(trace);
			liter.remove();
		}
		// how about the traceVariant ??? 
	}

}
