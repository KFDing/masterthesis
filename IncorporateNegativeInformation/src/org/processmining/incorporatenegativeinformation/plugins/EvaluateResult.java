package org.processmining.incorporatenegativeinformation.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.connections.BaselineConnection;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.incorporatenegativeinformation.models.LabeledTraceVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * This class includes the methods to evaluate the result by creating the confusion matrix. 
 * -- inputs: model + event log with labels
 * -- process: 
 *         first to conformance checking and remember the allowed behavior and not allowed behavoir
 *         then create the confusion matrix or other evaluation metrics.
 * @author dkf
 * date: 16.08.2018
 */
@Plugin(name = "Naive Check Conformance of Petri net and event log", level = PluginLevel.Regular, returnLabels = {"Conformance Matrix" }, returnTypes = {
		ArrayList.class}, parameterLabels = { "Labeled Log","Petri net", "Marking"}, userAccessible = true)
public class EvaluateResult {
	
	// the first one is to check conformance of Petri net and event log
	/**
	 * It should return one list of values in a object
	 * @param context
	 * @param log
	 * @param net
	 * @param marking
	 * @return
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive CC with AcceptingPetriNet",  requiredParameterLabels = { 0,1})
	public ArrayList<Integer> naiveCheckPNPlugin(UIPluginContext context, XLog log, AcceptingPetriNet anet ) { // Marking 
		return naiveCheckPNPlugin(context, log, anet.getNet(), anet.getInitialMarking());
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive CC No Marking",  requiredParameterLabels = { 0, 1})
	public ArrayList<Integer> naiveCheckPNPlugin(UIPluginContext context, XLog log, Petrinet net ) { // Marking 
		Marking marking = NetUtilities.guessInitialMarking(net);
		return naiveCheckPNPlugin(context, log, net, marking);
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive Conformance Checking",  requiredParameterLabels = { 0,1,2})
	public ArrayList<Integer> naiveCheckPNPlugin(UIPluginContext context, XLog log, Petrinet net, Marking marking ) { // Marking 
		// given log, should we first to organize them into variants and then do such stuff??? 
		// not really, because anyway we need to check one trace by another...How about we store such trace variants,
		// and compare them, if they matches, so we know if they get matched , or not 
		// we need to build the mapping for transitions and event log classifier\
		
		Collection<BaselineConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(BaselineConnection.class, context, log);
			for (BaselineConnection connection : connections) {
				if ( connection.getObjectWithRole(BaselineConnection.TYPE).equals(BaselineConnection.BASELINE_PN)
						// this is not the same , because the model we used to build differs this one
						// && connection.getObjectWithRole(BaselineConnection.LOG).equals(log)
						&& ((Petrinet)connection.getObjectWithRole(BaselineConnection.PN)).equals(net) 
						) {
					return connection.getObjectWithRole(BaselineConnection.CM_RESULT);
				}
			}
		}catch (ConnectionCannotBeObtained e) {
		}
		
		
		return naiveCheckPN(log, net, marking);
		
	}
	
	
	public static ArrayList<Integer> naiveCheckPN(XLog log, Petrinet net, Marking marking ) { // Marking 
		// given log, should we first to organize them into variants and then do such stuff??? 
		// not really, because anyway we need to check one trace by another...How about we store such trace variants,
		// and compare them, if they matches, so we know if they get matched , or not 
		// we need to build the mapping for transitions and event log classifier\
		ArrayList<Integer> confusion_matrix = new ArrayList<Integer>();
		for(int i =0 ; i< Configuration.CONFUSION_MATRIX_SIZE;i++) {
			confusion_matrix.add(0);
		}
		
		XEventClassifier classifier =null; //  = parameters.getClassifier();  // = net.getAttributeMap().get("XEventClassifier");
		// main problem is the mapping from event, how should we do ?? 
		Map<XEventClass, Transition> maps = EventLogUtilities.getEventTransitionMap(log, net , classifier);
		
		// should we separate the event log into different variants and check the variants fit or not fit?? 
		/* 
		 * to achieve it, we need to define one pos number and neg number for traceVariant, to store its distribution 
		 * just extend the already existing methods 
		*/
		List<LabeledTraceVariant> variants = EventLogUtilities.getLabeledTraceVariants(log, classifier);
		
		for(LabeledTraceVariant variant: variants) {
			
			if(NetUtilities.fitPN(net, marking, variant.getTraceVariant(), maps)) {
				// with pos_outcome
				
				confusion_matrix.set(Configuration.ALLOWED_POS_IDX, confusion_matrix.get(Configuration.ALLOWED_POS_IDX)+ variant.getPosNum());
				
				confusion_matrix.set(Configuration.ALLOWED_NEG_IDX, confusion_matrix.get(Configuration.ALLOWED_NEG_IDX)+ variant.getNegNum());
					// confusion_matrix[Configuration.ALLOWED_NEG_IDX] ++;
				
			}else {
				// with pos_outcome
				//if(attr !=null && attr.getValue()) {
					confusion_matrix.set(Configuration.NOT_ALLOWED_POS_IDX, confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX)+ variant.getPosNum());
					// confusion_matrix[Configuration.NOT_ALLOWED_POS_IDX] ++;
				//}else {
					confusion_matrix.set(Configuration.NOT_ALLOWED_NEG_IDX, confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX)+variant.getNegNum());
					// confusion_matrix[Configuration.NOT_ALLOWED_NEG_IDX] ++;
				//}
			}
			
		}
		return confusion_matrix;
	}
	
}
