package org.processmining.plugins.ding.baseline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
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
		
		boolean match;
		for (Transition transition : transitions) {
			match = false;
			for (XEventClass eventClass : classes.getClasses()) {
				// here we need to create a mapping from event log to graphs
				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
					map.put(eventClass, transition);
					match = true;
					break;
				}
			}
			if(! match) {
				map.put(null, transition);
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
	
	
}
