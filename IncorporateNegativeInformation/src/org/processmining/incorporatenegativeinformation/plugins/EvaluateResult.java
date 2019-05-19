package org.processmining.incorporatenegativeinformation.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.incorporatenegativeinformation.algorithms.PNReplayer;
import org.processmining.incorporatenegativeinformation.connections.BaselineConnection;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * This class includes the methods to evaluate the result by creating the
 * confusion matrix. -- inputs: model + event log with labels -- process: first
 * to conformance checking and remember the allowed behavior and not allowed
 * behavoir then create the confusion matrix or other evaluation metrics.
 * 
 * modifed data : 08.05.2019. The reasons are the model can not deal with repeated events
 * in the model. 
 * @author dkf date: 16.08.2018
 */
@Plugin(name = "Naive Check Conformance of Petri net and event log", level = PluginLevel.Regular, returnLabels = {
		"Conformance Matrix" }, returnTypes = {
				ArrayList.class }, parameterLabels = { "Labeled Log", "Petri net", "Marking", "Final Marking" }, userAccessible = true)
public class EvaluateResult {

	private static final int POS_IDX = 0;
	private static final int NEG_IDX = 1;


	// the first one is to check conformance of Petri net and event log
	/**
	 * It should return one list of values in a object
	 * 
	 * @param context
	 * @param log
	 * @param net
	 * @param marking
	 * @return
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive CC with AcceptingPetriNet", requiredParameterLabels = { 0, 1 })
	public ArrayList<Integer> naiveCheckPNPlugin(PluginContext context, XLog log, AcceptingPetriNet anet) { // Marking 
		
		return naiveCheckPN(log, anet);
	}
	
	

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive CC No Marking", requiredParameterLabels = { 0, 1 })
	public ArrayList<Integer> naiveCheckPNPlugin(PluginContext context, XLog log, Petrinet net) { // Marking 
		Marking initmarking = NetUtilities.guessInitialMarking(net);
		Set<Marking> finalmarking = NetUtilities.guessFinalMarking(net);
		return naiveCheckPN(log, net, initmarking, finalmarking);
	}

	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com")
	@PluginVariant(variantLabel = "Petrinet Naive Conformance Checking", requiredParameterLabels = { 0, 1, 2, 3 })
	public ArrayList<Integer> naiveCheckPNPlugin(PluginContext context, XLog log, Petrinet net, Marking initmarking, Marking finalMarking) { // Marking 
		// given log, should we first to organize them into variants and then do such stuff??? 
		// not really, because anyway we need to check one trace by another...How about we store such trace variants,
		// and compare them, if they matches, so we know if they get matched , or not 
		// we need to build the mapping for transitions and event log classifier\

		Collection<BaselineConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(BaselineConnection.class, context, log);
			for (BaselineConnection connection : connections) {
				if (connection.getObjectWithRole(BaselineConnection.TYPE).equals(BaselineConnection.BASELINE_PN)
						// this is not the same , because the model we used to build differs this one
						// && connection.getObjectWithRole(BaselineConnection.LOG).equals(log)
						&& ((Petrinet) connection.getObjectWithRole(BaselineConnection.PN)).equals(net)) {
					return connection.getObjectWithRole(BaselineConnection.CM_RESULT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		Set<Marking> fSet = new HashSet<>();
		fSet.add(finalMarking);
		return naiveCheckPN(log, net, initmarking, fSet);

	}

	public static ArrayList<Integer> naiveCheckPN(XLog log, AcceptingPetriNet anet) { // Marking 
		return naiveCheckPN(log, anet.getNet(), anet.getInitialMarking(), anet.getFinalMarkings());
	}
	
	public static ArrayList<Integer> naiveCheckPN(XLog log, Petrinet net, Marking initmarking, Set<Marking> finalMarking) { // Marking 
		// given log, should we first to organize them into variants and then do such stuff??? 
		// not really, because anyway we need to check one trace by another...How about we store such trace variants,
		// and compare them, if they matches, so we know if they get matched , or not 
		// we need to build the mapping for transitions and event log classifier\

		if (initmarking == null || initmarking.size() < 1) {
			System.out.println("set the final marking at first");
			initmarking = NetUtilities.guessInitialMarking(net);
		}
		if(finalMarking == null || finalMarking.size() < 1) {
			System.out.println("set the final marking at first");
			finalMarking = NetUtilities.guessFinalMarking(net);
		}
		
		PNReplayer replayer = new PNReplayer(log, net, initmarking, finalMarking);
		PNRepResult pnRepResult = replayer.replay();
		
		ArrayList<Integer> confusion_matrix = new ArrayList<Integer>();
		for (int i = 0; i < Configuration.CONFUSION_MATRIX_SIZE; i++) {
			confusion_matrix.add(0);
		}

		
		for (SyncReplayResult variantAlignment : pnRepResult) {
			// variantAlignment.getTraceIndex(); <- index of the log of the trace that has the variant
			Set<Integer> allTraceIdxOfThisVariant = variantAlignment.getTraceIndex();
			int[] nums = getPosNegNum(log, allTraceIdxOfThisVariant);
			if(replayer.fitTraceVariant(variantAlignment)) {
				// fit traces
				confusion_matrix.set(Configuration.ALLOWED_POS_IDX,
						confusion_matrix.get(Configuration.ALLOWED_POS_IDX) + nums[POS_IDX]);

				confusion_matrix.set(Configuration.ALLOWED_NEG_IDX,
						confusion_matrix.get(Configuration.ALLOWED_NEG_IDX) + nums[NEG_IDX]);
				
			}else {
				// unfit traces
				confusion_matrix.set(Configuration.NOT_ALLOWED_POS_IDX,
						confusion_matrix.get(Configuration.NOT_ALLOWED_POS_IDX) + nums[POS_IDX]);
				// confusion_matrix[Configuration.NOT_ALLOWED_POS_IDX] ++;
				//}else {
				confusion_matrix.set(Configuration.NOT_ALLOWED_NEG_IDX,
						confusion_matrix.get(Configuration.NOT_ALLOWED_NEG_IDX) + nums[NEG_IDX]);
			}
			
		}
		
		return confusion_matrix;
	}



	private static int[] getPosNegNum(XLog log, Set<Integer> allTraceIdxOfThisVariant) {
		// TODO Auto-generated method stub
		int[] nums = new int [2];
		for(int idx:allTraceIdxOfThisVariant ) {
			XTrace trace = log.get(idx);
			
			XAttributeBoolean attr = (XAttributeBoolean) trace.getAttributes().get(Configuration.POS_LABEL);
			if (attr == null || attr.getValue()) {
				nums[POS_IDX]++;
			}else {
				nums[NEG_IDX]++;
			}
		}
		
		return nums;
	}

}
