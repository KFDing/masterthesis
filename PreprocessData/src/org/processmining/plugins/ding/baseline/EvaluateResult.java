package org.processmining.plugins.ding.baseline;

import java.util.ArrayList;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.ding.preprocess.Configuration;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

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
	
	// to check the conformance of process tress and event log
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Process Tree Naive Conformance Checking",  requiredParameterLabels = { 0, 1})
	public ArrayList<Integer> naiveCheckPT(UIPluginContext context, XLog log, ProcessTree tree) throws NotYetImplementedException, InvalidProcessTreeException {
		// should we try to write code to check the replay of it ?? No, we just transform it into Petri net 
		// and use the method above
		
		@SuppressWarnings("deprecation")
		PetrinetWithMarkings net  = ProcessTree2Petrinet.convert(tree);
		return naiveCheckPN(context, log, net.petrinet, net.initialMarking); // , net.initialMarking
	}

	
	// the first one is to check conformance of Petri net and event log
	/**
	 * It should return one list of values in a object
	 * @param context
	 * @param log
	 * @param net
	 * @param marking
	 * @return
	 */
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Petrinet Naive Conformance Checking",  requiredParameterLabels = { 0,1,2})
	public ArrayList<Integer> naiveCheckPN(UIPluginContext context, XLog log, Petrinet net, Marking marking ) { // Marking 
		// given log, should we first to organize them into variants and then do such stuff??? 
		// not really, because anyway we need to check one trace by another...How about we store such trace variants,
		// and compare them, if they matches, so we know if they get matched , or not 
		// we need to build the mapping for transitions and event log classifier\
		ArrayList<Integer> confusion_matrix = new ArrayList<Integer>();
		for(int i =0 ; i< Configuration.CONFUSION_MATRIX_SIZE;i++) {
			confusion_matrix.add(0);
		}
		/*
		MiningParameters parameters = null;
		Collection<BaselineConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(BaselineConnection.class, context, log);
			for (BaselineConnection connection : connections) {
				if ( connection.getObjectWithRole(BaselineConnection.TYPE).equals(BaselineConnection.BASELINE_PN)
						// this is not the same , because the model we used to build differs this one
						// && connection.getObjectWithRole(BaselineConnection.LOG).equals(log)
						&& ((Petrinet)connection.getObjectWithRole(BaselineConnection.PN)).equals(net) 
						) {
					parameters = connection.getObjectWithRole(BaselineConnection.MINING_PARAMETERS);
				}
			}
		}catch (ConnectionCannotBeObtained e) {
		}
		*/
		XEventClassifier classifier =null; //  = parameters.getClassifier();  // = net.getAttributeMap().get("XEventClassifier");
		// main problem is the mapping from event, how should we do ?? 
		Map<XEventClass, Transition> maps = EventLogUtilities.getEventTransitionMap(log, net , classifier);
		
		for(XTrace trace: log) {
			
			XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.LABEL_NAME);
			// transfer trace into sequence of eventClasses
			// there is no KPIs outcome from it, then we only get the allowed behavior
			// but do we need to check if they fit??? Or in default they fit the model.. Should we check or not??
			// -- if they fit the model, anyway, they don't have any information?? They should have information 
			// on the positive and negative ones, it's a requirement.!! 
			if(NetUtilities.fitPN(net, marking, EventLogUtilities.transferTrace(log, trace, classifier), maps)) {
				// with pos_outcome
				if(attr !=null && attr.getValue()) {
					confusion_matrix.set(Configuration.ALLOWED_POS_IDX, confusion_matrix.get(Configuration.ALLOWED_POS_IDX)+1);
					// confusion_matrix[Configuration.ALLOWED_POS_IDX] ++;
				}else {
					confusion_matrix.set(Configuration.ALLOWED_NEG_IDX, confusion_matrix.get(Configuration.ALLOWED_NEG_IDX)+1);
					// confusion_matrix[Configuration.ALLOWED_NEG_IDX] ++;
				}
			}else {
				// with pos_outcome
				if(attr !=null && attr.getValue()) {
					confusion_matrix.set(Configuration.NOT_ALLOWED_POS_IDX, confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX)+1);
					// confusion_matrix[Configuration.NOT_ALLOWED_POS_IDX] ++;
				}else {
					confusion_matrix.set(Configuration.NOT_ALLOWED_NEG_IDX, confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX)+1);
					// confusion_matrix[Configuration.NOT_ALLOWED_NEG_IDX] ++;
				}
			}
			
		}
		return confusion_matrix;
		// here we should create the html file, somehow to show the result?? 
		/*
		String result = "";
		for(int i=0;i < Configuration.CONFUSION_MATRIX_SIZE;i++){
			result += confusion_matrix[i];
		}
		return "Confusion matrix result is : " + result ;
		*/
	}
	
}
