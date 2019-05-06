package org.processmining.incorporatenegativeinformation.algorithms;
/**
 * this class transfrom Petri net into dfg relation -- first, we transfrom PN
 * into ReachabilityGraph of transition system -- create dfg from the transtions
 * in ReachabilityGraph -- assign the threshold of frequency, better to separate
 * this function
 * 
 * @author dkf
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.incorporatenegativeinformation.help.RepairTSGenerator;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgImpl;

@Plugin(name = "Construct Dfg of a Petri Net", returnLabels = { "MyDfg", "Dfg from IMD" }, returnTypes = { Dfg.class,
		Dfg.class }, parameterLabels = { "XLog", "Net", "Marking" })

public class PN2DfgTransform {
	static Map<String, XEventClass> eventClassMap;
	static Map<XEventClass, org.processmining.models.graphbased.directed.petrinet.elements.Transition> etMap;
	
	
	public static Dfg transformPN2Dfg(PluginContext context, Petrinet net, Marking marking)
			throws ConnectionCannotBeObtained {
		RepairTSGenerator tsGenerator = new RepairTSGenerator();
		Object[] result = tsGenerator.calculateTS(context, net, marking);
		if (result == null) {
			System.out.println("The result goes wrong of transition system");
			return null;
		}
		ReachabilityGraph ts = (ReachabilityGraph) result[0];
		// ReachabilitySet rs = (ReachabilitySet)result[1];
		// there is also the initial state, I will see which it represents
		StartStateSet startStates = (StartStateSet) result[2];
		AcceptStateSet acceptingStates = (AcceptStateSet) result[3];

		Dfg dfg = new DfgImpl(); // here to change to delet the tau transition

		eventClassMap = new HashMap<String, XEventClass>();
		etMap = new HashMap<>();
		int idx = 0;
		for (org.processmining.models.graphbased.directed.petrinet.elements.Transition pTransition : net
				.getTransitions()) {
			// build two maps... 
			if (!pTransition.isInvisible() && !pTransition.getLabel().equals("")) {
				String key = pTransition.getLabel();
				XEventClass eventClass = new XEventClass(key, idx++); // or we need to assign them later.. whatever, only concrete events matter
				dfg.addActivity(eventClass); // here to add only the non- tau activity
				eventClassMap.put(key, eventClass);
				etMap.put(eventClass, pTransition);
			}

		}
		addStartEnd(dfg, ts, startStates, acceptingStates);

		addDirectFollow(dfg, ts);
		/*
		// filter the directly-follows relation to keep only the ones in net
		List<Long> edgesRemove = new ArrayList<>();
		for(long edgeIdx: dfg.getDirectlyFollowsEdges()) {
			XEventClass source = dfg.getConcurrencyEdgeSource(edgeIdx);
			XEventClass target = dfg.getConcurrencyEdgeTarget(edgeIdx);
			
			// if this connection exists in net, when we keep it
			// if not exist, but after checking, it is due to the silent transitions
			// we should hold it also...
			Arc arc = net.getArc(etMap.get(source), etMap.get(target));
			if(arc==null) {
				// we need to filter it out from the elements
				edgesRemove.add(edgeIdx);
			}
			
		}
		
		*/
		return dfg;
	}

	public static void addStartEnd(Dfg dfg, ReachabilityGraph rg, StartStateSet startStates,
			AcceptStateSet acceptingStates) {

		for (Object sid : startStates) {
			// then we need to get the 
			State ss = rg.getNode(sid);
			Collection<Transition> start_trans = rg.getOutEdges(ss);
			for (Transition t : start_trans) {
				if (!isTau(t)) {
					// here we need to build one corresponding relation with initialization at begin
					dfg.addStartActivity(eventClassMap.get(t.getLabel()), 1);
				} else {
					Collection<Transition> post_tau = BFS(t, rg, true);
					for (Transition post_t : post_tau) {
						dfg.addStartActivity(eventClassMap.get(post_t.getLabel()), 1);
					}
				}
			}
		}
		for (Object fid : acceptingStates) {
			State ss = rg.getNode(fid);
			Collection<Transition> final_trans = rg.getInEdges(ss);
			for (Transition t : final_trans) {
				if (!isTau(t)) {
					// here we need to build one corresponding relation with initialization at begin
					dfg.addEndActivity(eventClassMap.get(t.getLabel()), 1);
				} else {
					Collection<Transition> pre_tau = BFS(t, rg, false) ;
					for (Transition pre_t : pre_tau) {
						dfg.addEndActivity(eventClassMap.get(pre_t.getLabel()), 1);
					}
				}
			}
		}
	}

	/**
	 * we have modification to deal with silent transitions. We need to find out
	 * the silent transition t. something, not so nice to check, I'd like to
	 * say.. [S1]--> [tau, specific for one branch to connect?? ]-->[T1],
	 * specific connection for it so we connect the S1-->T1, and create the
	 * directly follows relation
	 * 
	 * but one missing part, if there are more than one silent transitions between them,
	 * then the connection gets lost. SO we need to check them links, 
	 * do it in the following way:
	 *   --- allow all silent transitions in the states and get the directly-follows relation'
	 *   --- for any connected to the silent transition, S1-->tau_i, tau_j --> T1, tau_i--> tau_j
	 *   --- we need to do the combination of them..
	 *   for S1-->tau_i,
	 *      find all [T1...Tn, tau_1,..tau_m] relation with tau_i, tau_i --> ??
	 *      if(it is [T1,T2...Tn]), then
	 *         connect S1-->Ti
	 *      if(in [tau_1...tau_m])
	 *         go breadth search for  tau_j
	 *         stops at un silent transitions
	 *          
	 * @param dfg
	 * @param rg
	 */
	public static void addDirectFollow(Dfg dfg, ReachabilityGraph rg) {
		Set<State> states = rg.getNodes();
		// rg.getEdges(); // get transitions from graph.. 
		Collection<Transition> in_ts, out_ts;
		// transition is the beginning transition, 
		for (State s : states) {
			in_ts = rg.getInEdges(s);
			out_ts = rg.getOutEdges(s);
			// after we get all the transitions from initial state, we go deep??
			for (Transition in_t : in_ts) {
				
				for (Transition out_t : out_ts) {
					 if (!isTau(in_t)&&!isTau(out_t))
						addDf(dfg, in_t, out_t);
					 else if (!isTau(in_t)&& isTau(out_t)) {
						 // in situation S1--> state --> tau_j
						 // here we do BFS search to find reachable targets
						 List<Transition> nTransitions = BFS(out_t, rg, true);
						 for(Transition nt: nTransitions) {
							 addDf(dfg, in_t, nt);
						 }
					 }
				}
			}

		}
	}
	
	// if we put the BFS directly on the transitions sytem
	// we want to get one state where the state can go to another states
	private static List<Transition> BFS(Transition source, ReachabilityGraph rg, boolean goPost) {
			List<Transition> nsTarget = new ArrayList<>();
			// mark if visited, source is a silent transition
			Map<Transition, Boolean> visited = new HashMap<>();
			LinkedList<Transition> queue = new LinkedList<>();
			visited.put(source, true);
			queue.add(source);
			
			while(queue.size()!=0) {
				source = queue.poll();
				// System.out.println(source.getLabel() + " : visited");
				
				// get the adjacent transitions of source, seems also not so easy to do it here.
				// either ts or dfg, no direct way, so just do it here
				if(goPost) {
					State state = source.getTarget();
					Collection<Transition> outEdges = rg.getOutEdges(state);
					
					for(Transition t: outEdges) {
						
						if(!visited.containsKey(t)){
							visited.put(t, true);
							
							if(isTau(t))
								queue.add(t);
							else
								nsTarget.add(t);
						}
						
					}
				}else {
					// from back to forward, to deal with the acception state
					State state = source.getSource();
					Collection<Transition> inEdges = rg.getInEdges(state);
					
					for(Transition t: inEdges) {
						
						if(!visited.get(t)) {
							visited.put(t, true);
							
							if(isTau(t))
								queue.add(t);
							else
								nsTarget.add(t);
						}
						
					}
				}
				
			}
			
			return nsTarget;
		}

	// hide the transform details into this function
	private static void addDf(Dfg dfg, Transition in_t, Transition out_t) {
		XEventClass source, target;
		source = eventClassMap.get(in_t.getLabel());
		target = eventClassMap.get(out_t.getLabel());
		if(!dfg.containsDirectlyFollowsEdge(source, target))
			dfg.addDirectlyFollowsEdge(source, target, 1);
	}

	private static boolean isTau(Transition t) {
		if (eventClassMap.containsKey(t.getLabel()))
			return false;
		return true;
	}
	
	private static boolean isTau(XEventClass t) {
		if (eventClassMap.containsValue(t))
			return false;
		return true;
	}

	public static void setCardinality(Dfg dfg, long cardinality) {
		for (long idx : dfg.getDirectlyFollowsEdges()) {
			//there is no direct way to change it, so what we can do it to remove and then add them again
			int sourceIdx = dfg.getDirectlyFollowsEdgeSourceIndex(idx);
			int targetIdx = dfg.getDirectlyFollowsEdgeTargetIndex(idx);

			dfg.addDirectlyFollowsEdge(sourceIdx, targetIdx, cardinality);
		}

		for (XEventClass startClass : dfg.getStartActivities()) {
			dfg.addStartActivity(startClass, cardinality);
		}

		for (XEventClass endClass : dfg.getEndActivities()) {
			dfg.addEndActivity(endClass, cardinality);
		}
	}

}
