package org.processmining.incorporatenegativeinformation.help;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.connections.petrinets.behavioral.AbstractSemanticConnection;
import org.processmining.models.connections.petrinets.behavioral.BehavioralAnalysisInformationConnection;
import org.processmining.models.connections.petrinets.behavioral.BoundednessInfoConnection;
import org.processmining.models.connections.petrinets.behavioral.CoverabilityGraphConnection;
import org.processmining.models.connections.petrinets.behavioral.DeadMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.ReachabilitySetConnection;
import org.processmining.models.connections.petrinets.behavioral.StateSpaceConnection;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation.UnDetBool;
import org.processmining.models.graphbased.directed.petrinet.analysis.ReachabilitySet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.CTMarking;
import org.processmining.models.semantics.petrinet.InhibitorNetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.ResetInhibitorNetSemantics;
import org.processmining.models.semantics.petrinet.ResetNetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;

@Plugin(name = "Construct Reachability Graph of a Petri Net", returnLabels = { "Reachability graph", "Reachability Set",
		"Initial states", "Final states" }, returnTypes = { ReachabilityGraph.class, ReachabilitySet.class,
				StartStateSet.class,
				AcceptStateSet.class }, parameterLabels = { "Net", "Marking", "Semantics", "Coverability Graph" })
public class RepairTSGenerator {

	private static final int MAXSTATES = 25000;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A. Adriansyah", email = "a.adriansyah@tue.nl", pack = "PNAnalysis")
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public Object[] calculateTS(PluginContext context, Petrinet net, Marking state) throws ConnectionCannotBeObtained {
		return calculateTS(context, net, state, PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class));
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "A. Adriansyah", email = "a.adriansyah@tue.nl", pack = "PNAnalysis")
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public Object[] calculateTS(PluginContext context, ResetNet net, Marking state) throws ConnectionCannotBeObtained {
		return calculateTS(context, net, state, PetrinetSemanticsFactory.regularResetNetSemantics(ResetNet.class));
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public Object[] calculateTS(PluginContext context, InhibitorNet net, Marking state)
			throws ConnectionCannotBeObtained {
		return calculateTS(context, net, state,
				PetrinetSemanticsFactory.regularInhibitorNetSemantics(InhibitorNet.class));
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public Object[] calculateTS(PluginContext context, ResetInhibitorNet net, Marking state)
			throws ConnectionCannotBeObtained {
		return calculateTS(context, net, state,
				PetrinetSemanticsFactory.regularResetInhibitorNetSemantics(ResetInhibitorNet.class));
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 3 })
	public Object[] calculateTS(PluginContext context, ResetInhibitorNet net, Marking state, CoverabilityGraph graph)
			throws ConnectionCannotBeObtained {
		return buildAndConnect(context, net, state, graph);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 3 })
	public Object[] calculateTS(PluginContext context, Petrinet net, Marking state, CoverabilityGraph graph)
			throws ConnectionCannotBeObtained {
		return buildAndConnect(context, net, state, graph);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 3 })
	public Object[] calculateTS(PluginContext context, ResetNet net, Marking state, CoverabilityGraph graph)
			throws ConnectionCannotBeObtained {
		return buildAndConnect(context, net, state, graph);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 3 })
	public Object[] calculateTS(PluginContext context, InhibitorNet net, Marking state, CoverabilityGraph graph)
			throws ConnectionCannotBeObtained {
		return buildAndConnect(context, net, state, graph);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 2 })
	public Object[] calculateTS(PluginContext context, Petrinet net, Marking state, PetrinetSemantics semantics)
			throws ConnectionCannotBeObtained {
		semantics.initialize(net.getTransitions(), new Marking(state));
		return buildAndConnect(context, net, state, semantics, null);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 2 })
	public Object[] calculateTS(PluginContext context, ResetNet net, Marking state, ResetNetSemantics semantics)
			throws ConnectionCannotBeObtained {
		semantics.initialize(net.getTransitions(), new Marking(state));
		return buildAndConnect(context, net, state, semantics, null);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 2 })
	public Object[] calculateTS(PluginContext context, InhibitorNet net, Marking state, InhibitorNetSemantics semantics)
			throws ConnectionCannotBeObtained {
		semantics.initialize(net.getTransitions(), new Marking(state));
		return buildAndConnect(context, net, state, semantics, null);
	}

	@PluginVariant(requiredParameterLabels = { 0, 1, 2 })
	public Object[] calculateTS(PluginContext context, ResetInhibitorNet net, Marking state,
			ResetInhibitorNetSemantics semantics) throws ConnectionCannotBeObtained {
		semantics.initialize(net.getTransitions(), new Marking(state));
		return buildAndConnect(context, net, state, semantics, null);
	}

	private Object[] buildAndConnect(PluginContext context, PetrinetGraph net, Marking initial,
			CoverabilityGraph coverabilityGraph) throws ConnectionCannotBeObtained {

		CoverabilityGraphConnection connection = context.getConnectionManager()
				.getFirstConnection(CoverabilityGraphConnection.class, context, net, initial, coverabilityGraph);
		Semantics<Marking, Transition> sem = connection.getObjectWithRole(AbstractSemanticConnection.SEMANTICS);
		sem.initialize(net.getTransitions(), initial);
		return buildAndConnect(context, net, initial, sem, coverabilityGraph);
	}

	private Object[] buildAndConnect(PluginContext context, PetrinetGraph net, Marking initial,
			Semantics<Marking, Transition> semantics, CoverabilityGraph coverabilityGraph)
			throws ConnectionCannotBeObtained {
		context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net, initial);

		ReachabilityGraph ts = null;
		NetAnalysisInformation.BOUNDEDNESS info = null;

		try {
			BoundednessInfoConnection analysis = context.getConnectionManager()
					.getFirstConnection(BoundednessInfoConnection.class, context, net, initial, semantics);
			info = analysis.getObjectWithRole(BehavioralAnalysisInformationConnection.NETANALYSISINFORMATION);
		} catch (Exception e) {
			// No connections available
		}

		if ((info != null) && info.getValue().equals(UnDetBool.FALSE)) {
			// This net has been shows to be unbounded on this marking
			context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
					MessageLevel.ERROR);
			context.getFutureResult(0).cancel(true);
			// unreachable statement, but safe.
			return null;
		}
		// boolean bounded = (info != null);

		// if (coverabilityGraph == null && !bounded) {
		// // If boundedness is unknown, try to construct a coverability graph
		// coverabilityGraph =
		// context.tryToFindOrConstructFirstObject(CoverabilityGraph.class,
		// CoverabilityGraphConnection.class,
		// CoverabilityGraphConnection.STATEPACE, net, initial, semantics);
		// }

		if (coverabilityGraph != null) {// && !bounded) {
			if (!BoundednessAnalyzer.isBounded(coverabilityGraph)) {
				// This net has been shows to be unbounded on this marking
				context.log("The given net is unbounded on the given initial marking, no Statespace is constructed.",
						MessageLevel.ERROR);
				context.getFutureResult(0).cancel(true);
				// unreachable statement, but safe.
				return null;
			}
			// clone the graph and return
			Map<CTMarking, Marking> mapping = new HashMap<CTMarking, Marking>();

			ts = new ReachabilityGraph("StateSpace of " + net.getLabel());
			for (Object o : coverabilityGraph.getStates()) {
				CTMarking m = (CTMarking) o;
				Marking tsm = new Marking(m);
				ts.addState(tsm);
				mapping.put(m, tsm);
			}
			for (org.processmining.models.graphbased.directed.transitionsystem.Transition e : coverabilityGraph
					.getEdges()) {
				Marking source = mapping.get(e.getSource().getIdentifier());
				Marking target = mapping.get(e.getTarget().getIdentifier());
				ts.addTransition(source, target, e.getIdentifier());
			}

		}

		StartStateSet startStates = new StartStateSet();
		startStates.add(initial);

		if (ts == null) {
			ts = doBreadthFirst(context, net.getLabel(), initial, semantics, MAXSTATES);
		}
		if (ts == null) {
			// Problem with the reachability graph.
			context.getFutureResult(0).cancel(true);
			return null;
		}

		AcceptStateSet acceptingStates = new AcceptStateSet();
		for (State state : ts.getNodes()) {
			if (ts.getOutEdges(state).isEmpty()) {
				acceptingStates.add(state.getIdentifier());
			}
		}

		Marking[] markings = ts.getStates().toArray(new Marking[0]);
		ReachabilitySet rs = new ReachabilitySet(markings);

		context.addConnection(new ReachabilitySetConnection(net, initial, rs, semantics, "Reachability Set"));
		context.addConnection(new StateSpaceConnection(net, initial, ts, semantics));
		context.addConnection(new TransitionSystemConnection(ts, startStates, acceptingStates));
		context.addConnection(new DeadMarkingConnection(net, initial, acceptingStates, semantics));
		/*
		 * this is due to the return value doesn't exist in later use
		 * context.getFutureResult(0).setLabel("Reachability graph of " +
		 * net.getLabel());
		 * context.getFutureResult(1).setLabel("Reachability set of " +
		 * net.getLabel());
		 * context.getFutureResult(2).setLabel("Initial states of " +
		 * ts.getLabel());
		 * context.getFutureResult(3).setLabel("Accepting states of " +
		 * ts.getLabel());
		 */
		context.log(
				"Statespace size: " + ts.getStates().size() + " states and " + ts.getEdges().size() + " transitions.",
				MessageLevel.DEBUG);

		return new Object[] { ts, rs, startStates, acceptingStates };
	}

	private ReachabilityGraph doBreadthFirst(PluginContext context, String label, Marking state,
			Semantics<Marking, Transition> semantics, int max) {
		ReachabilityGraph ts = new ReachabilityGraph("StateSpace of " + label);
		ts.addState(state);
		Queue<Marking> newStates = new LinkedList<Marking>();
		newStates.add(state);
		do {
			newStates.addAll(extend(ts, newStates.poll(), semantics, context));
		} while (!newStates.isEmpty() && (ts.getStates().size() < max));
		if (!newStates.isEmpty()) {
			// This net has been shows to be unbounded on this marking
			context.log("The behaviour of the given net is has over " + max + " states. Aborting...",
					MessageLevel.ERROR);
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return ts;

	}

	private Set<Marking> extend(ReachabilityGraph ts, Marking state, Semantics<Marking, Transition> semantics,
			PluginContext context) {
		Set<Marking> newStates = new HashSet<Marking>();
		semantics.setCurrentState(state);
		for (Transition t : semantics.getExecutableTransitions()) {
			semantics.setCurrentState(state);
			try {
				/*
				 * [HV] The local variable info is never read
				 * ExecutionInformation info =
				 */semantics.executeExecutableTransition(t);
				// context.log(info.toString(), MessageLevel.DEBUG);
			} catch (IllegalTransitionException e) {
				context.log(e);
				assert (false);
			}
			Marking newState = semantics.getCurrentState();

			if (ts.addState(newState)) {
				newStates.add(newState);
				int size = ts.getEdges().size();
				if (size % 1000 == 0) {
					context.log("Statespace size: " + ts.getStates().size() + " states and " + ts.getEdges().size()
							+ " transitions.", MessageLevel.DEBUG);
				}
			}
			ts.addTransition(state, newState, t);
			semantics.setCurrentState(state);
		}
		return newStates;

	}
}
