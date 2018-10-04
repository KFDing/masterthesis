package org.processmining.plugins.ding.transform;
/**
 * this class transfrom Petri net into dfg relation
 * -- first, we transfrom PN into ReachabilityGraph of transition system
 * -- create dfg from the transtions in ReachabilityGraph
 * -- assign the threshold of frequency, better to separate this function
 * @author dkf
 *
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.XLog2Dfg;

@Plugin(name = "Construct Dfg of a Petri Net", returnLabels = {"MyDfg", "Dfg from IMD" }, returnTypes = { Dfg.class, Dfg.class}, 
      parameterLabels = { "XLog","Net", "Marking" })

public class PN2DfgTransform {
	static Map<String, XEventClass> eventClassMap ;
	
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Kefang", email = "***@gmail.com", uiLabel = UITopiaVariant.USEVARIANT)
	@PluginVariant(variantLabel = "Transfrom PN to Dfg",  requiredParameterLabels = { 0 , 1, 2})
	public Object[] transform(UIPluginContext context, XLog log, Petrinet net, Marking marking) throws ConnectionCannotBeObtained {
		
		double threshold = 0.2;
		
		int num = (int) (threshold * XLogInfoFactory.createLogInfo(log).getNumberOfTraces());
		
		XLog2Dfg ld = new XLog2Dfg();
		Dfg logdfg = ld.log2Dfg( context, log);
		
		Dfg dfg = transformPN2Dfg(context, net, marking);
		
		setCardinality(dfg, num);
		
		return new Object[] {dfg, logdfg};
	}
	
	public static Dfg transformPN2Dfg(UIPluginContext context,Petrinet net, Marking marking) throws ConnectionCannotBeObtained {
		TSGenerator tsGenerator = new TSGenerator();
		Object[] result = tsGenerator.calculateTS(context, net, marking);
		if(result == null) {
			System.out.println("The result goes wrong of transition system");
			return null;
		}
		ReachabilityGraph ts = (ReachabilityGraph) result[0];
		
		// there is also the initial state, I will see which it represents
		StartStateSet startStates =  (StartStateSet) result[2];
		AcceptStateSet acceptingStates = (AcceptStateSet) result[3];
		
		Collection<org.processmining.models.graphbased.directed.petrinet.elements.Transition> ntransitions =  net.getTransitions();
		Object[] nt = ntransitions.toArray();
		Dfg dfg = new DfgImpl(); // here to change to delet the tau transition
		eventClassMap = new HashMap<String, XEventClass>();
		int idx = 0;
		for(int i=0;i<ntransitions.size(); i++) {
			// here how to transform transition into XEventClass?? 
			String key =((org.processmining.models.graphbased.directed.petrinet.elements.Transition)nt[i]).getLabel();
			if(!key.contains("tau")) {
				XEventClass eventClass = new XEventClass(key, idx++); // or we need to assign them later.. whatever, only concrete events matter
				eventClassMap.put(key, eventClass);
				// dfg.addActivity(eventClass); // here to add only the non- tau activity
			}
		}
		addStartEnd(dfg, ts, startStates, acceptingStates);
		
		addDirectFollow(dfg, ts);
		
		return dfg;
	}
	
	public static void addStartEnd(Dfg dfg, ReachabilityGraph rg,StartStateSet startStates, AcceptStateSet acceptingStates) {
		
		for(Object sid: startStates) {
			// then we need to get the 
			State ss = rg.getNode(sid);
			Collection<Transition> start_trans = rg.getOutEdges(ss);
			for(Transition t : start_trans) {
				if(!isTau(t)) {
					// here we need to build one corresponding relation with initialization at begin
					dfg.addStartActivity(eventClassMap.get(t.getLabel()), 0);
				}else {
					Collection<Transition> post_tau = getNonTauTransition(rg, t,false);
					for(Transition post_t : post_tau) {
						dfg.addStartActivity(eventClassMap.get(post_t.getLabel()), 0);
					}
				}
			}
		}
		for(Object fid: acceptingStates) {
			State ss = rg.getNode(fid);
			Collection<Transition> final_trans = rg.getInEdges(ss);
			for(Transition t : final_trans) {
				if(!isTau(t)) {
					// here we need to build one corresponding relation with initialization at begin
					dfg.addEndActivity(eventClassMap.get(t.getLabel()), 0);
				}else {
					Collection<Transition> pre_tau = getNonTauTransition(rg, t,true);
					for(Transition pre_t : pre_tau) {
						dfg.addEndActivity(eventClassMap.get(pre_t.getLabel()), 0);
					}
				}
			}
		}
	}
	
	public static void addDirectFollow(Dfg dfg, ReachabilityGraph rg) {
		// look it from the root, I think,it is a root, then do breadth search,
		// parent, and then check the children edges, one edge, one df relation
		// from the startposition, 
		// get the outEdge of this start, mark them beginning, actually not matter
		// after one the the edge, and reach one state of it.. Breadth search for it. 
		// do we need to record the tau and get it out of this?? 
		// if it is tau split, or somehow, we check the last activity of them..if it's not tau, then connect them together,
	    // if there is one tau, then back again to it, to find the last one, no tau there.
		Set<State> states = rg.getNodes();
		
		Collection<Transition> in_ts, out_ts ;
		// transition is the beginning transition, 
		for(State s: states) {
			in_ts = rg.getInEdges(s);
			out_ts = rg.getOutEdges(s);
			// after we get all the transitions from initial state, we go deep??
			for(Transition in_t: in_ts) {
				for(Transition out_t: out_ts) {
					// if the incoming activity is tau
					if(isTau(in_t)) {
						// trace back to the last state and get the non_tau Edges.
						Collection<Transition> pre_tau = getNonTauTransition(rg, in_t,true);
						// get a collection of all previous events
						
						if(pre_tau.isEmpty()) {
							// we may reach the initial state, then we do nothing, just go to next state
							break;
						}else {
							for(Transition pre_t : pre_tau) {
								addDf(dfg, pre_t, out_t);
							}
						}
					}else {
						if(!isTau(out_t)) {
							// add the df into dfg
							addDf(dfg, in_t, out_t);
						}
						// if it is tau, then we don't do anything, because for the pre_tau, it could find it
					}
				}
			}
			
		}
		
	}

	// hide the transform details into this function
	private static void addDf(Dfg dfg, Transition in_t, Transition out_t) {
		XEventClass source, target; 
		source = eventClassMap.get(in_t.getLabel());
		target = eventClassMap.get(out_t.getLabel());
		dfg.addDirectlyFollowsEdge(source, target, 0);
	}
	
	
	private static Collection<Transition> getNonTauTransition(ReachabilityGraph rg, Transition tau, boolean b) {
		
		if(b) {
			State state = tau.getSource();
			Collection<Transition> pre_ts = rg.getInEdges(state);
			for(Transition t: pre_ts ) {
				if(t.getLabel().contains("tau")) {
					pre_ts.remove(t);
					pre_ts.addAll(getNonTauTransition(rg, t, true));
				}	
			}
			return pre_ts;
		}else {
			State state = tau.getTarget();
			Collection<Transition> post_ts = rg.getOutEdges(state);
			for(Transition t: post_ts ) {
				if(isTau(t)) {
					post_ts.remove(t);
					post_ts.addAll(getNonTauTransition(rg, t, false));
				}	
			}
			return post_ts;
		}
	}
	private static boolean isTau(Transition t) {
		return t.getLabel().contains("tau");
	}
	
	public static void setCardinality(Dfg dfg, long cardinality) {
		for(long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			int sourceIdx = dfg.getDirectlyFollowsEdgeSourceIndex(idx);
			int targetIdx = dfg.getDirectlyFollowsEdgeTargetIndex(idx);
			
			dfg.addDirectlyFollowsEdge(sourceIdx, targetIdx, cardinality);
		}
		
		for(XEventClass startClass : dfg.getStartActivities()) {
			dfg.addStartActivity(startClass, cardinality);
		}
		
		for(XEventClass endClass : dfg.getEndActivities()) {
			dfg.addEndActivity(endClass, cardinality);
		}
	}
}