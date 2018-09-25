/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2008 TU/e Eindhoven * and is licensed under the * LGPL
 * License, Version 1.0 * by Eindhoven University of Technology * Department of
 * Information Systems * http://www.processmining.org * *
 ***********************************************************/

package org.processmining.plugins.ding.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.collection.MultiSet;
import org.processmining.framework.util.collection.TreeMultiSet;
import org.processmining.models.connections.petrinets.behavioral.BoundednessInfoConnection;
import org.processmining.models.connections.petrinets.behavioral.CoverabilityGraphConnection;
import org.processmining.models.connections.petrinets.behavioral.CoverabilitySetConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.MarkingsetNetConnection;
import org.processmining.models.connections.petrinets.behavioral.UnboundedPlacesConnection;
import org.processmining.models.connections.petrinets.behavioral.UnboundedSequencesConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.analysis.CoverabilitySet;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation;
import org.processmining.models.graphbased.directed.petrinet.analysis.NetAnalysisInformation.UnDetBool;
import org.processmining.models.graphbased.directed.petrinet.analysis.UnboundedPlacesSet;
import org.processmining.models.graphbased.directed.petrinet.analysis.UnboundedSequences;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.semantics.petrinet.CTMarking;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;

/**
 * Class to analyze whether a given PetriNet is bounded Based on Murata, Tadao.
 * Petri Nets:Properties, Analysis, and Applications. Proceedings of the IEEE
 * vol. 77, No.4, April 1989 a net is bounded iff omega notation does not appear
 * in a any node labels in coverability graph
 * 
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Dec 13, 2008
 */
@Plugin(name = "Analyze Boundedness", returnLabels = { "Boundedness info", "Unbounded Places", "Unbounded Sequences" }, returnTypes = {
		NetAnalysisInformation.BOUNDEDNESS.class, UnboundedPlacesSet.class, UnboundedSequences.class }, parameterLabels = {
		"Net", "Marking", "Coverability Set", "Semantics" }, userAccessible = true)
public class BoundednessAnalyzer {

	// variant with only petri net as input
	@PluginVariant(variantLabel = "Analyze Boundedness", requiredParameterLabels = { 0 })
	public Object[] analyzeBoundednessPetriNet(PluginContext context, Petrinet net) throws ConnectionCannotBeObtained {
		Marking state = context.tryToFindOrConstructFirstObject(Marking.class, InitialMarkingConnection.class,
				InitialMarkingConnection.MARKING, net);
		return analyzeBoundednessPetriNet(context, net, state);
	}

	// variant with only petri net and marking as input
	@PluginVariant(variantLabel = "Analyze Boundedness", requiredParameterLabels = { 0, 1 })
	public Object[] analyzeBoundednessPetriNet(PluginContext context, Petrinet net, Marking state)
			throws ConnectionCannotBeObtained {
		return analyzeBoundednessPetriNet(context, net, state, PetrinetSemanticsFactory
				.regularPetrinetSemantics(Petrinet.class));
	}

	// variant with only petri net, marking, and semantic as input
	@PluginVariant(variantLabel = "Analyze Boundedness", requiredParameterLabels = { 0, 1, 3 })
	public Object[] analyzeBoundednessPetriNet(PluginContext context, Petrinet net, Marking state,
			PetrinetSemantics semantics) throws ConnectionCannotBeObtained {
		context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context, net, state);
		semantics.initialize(net.getTransitions(), new Marking(state));

		// check if there is already generated coverability set
		CoverabilitySet coverabilitySet = null;
		coverabilitySet = context.tryToFindOrConstructFirstObject(CoverabilitySet.class,
				CoverabilitySetConnection.class, CoverabilitySetConnection.MARKINGS, net, state, semantics);

		return analyzeBoundednessPetriNet(context, net, state, coverabilitySet, semantics);
	}

	// variant with petri net, marking, and coverability graph as input
	@PluginVariant(variantLabel = "Analyze Boundedness", requiredParameterLabels = { 0, 1, 2 })
	public Object[] analyzeBoundednessPetriNet(PluginContext context, Petrinet net, Marking state,
			CoverabilitySet covSet, PetrinetSemantics semantics) throws ConnectionCannotBeObtained {
		return analyzeBoundednessPetriNetInternal(context, net, state, covSet, semantics);
	}

	/**
	 * Analyze boundedness of a petri net
	 * 
	 * @param context
	 *            Context of the petri net
	 * @param net
	 *            net to be analyzed
	 * @param state
	 *            Initial state (initial marking)
	 * @param covSet
	 *            Coverability set of this petri net and marking
	 * @param semantics
	 *            Semantic of this petri net
	 * @return An array of three objects: 1. type NetAnalysisInformation, info
	 *         about boundedness of the net 2. type UnboundedPlacesSet, contains
	 *         set of set of unbounded places 3. type UnboudnedSequences,
	 *         contains set of unbounded sequences
	 * @throws Exception
	 */
	private Object[] analyzeBoundednessPetriNetInternal(PluginContext context, PetrinetGraph net, Marking state,
			CoverabilitySet covSet, PetrinetSemantics semantics) throws ConnectionCannotBeObtained {
		// check connection between coverability graph, net, and marking
		context.getConnectionManager().getFirstConnection(MarkingsetNetConnection.class, context, net, covSet,
				semantics);

		semantics.initialize(net.getTransitions(), state);
		Object[] result = analyzeBoundednessAssumingConnection(context, net, state, covSet, semantics);
		context.addConnection(new BoundednessInfoConnection(net, state, semantics, (NetAnalysisInformation.BOUNDEDNESS) result[0]));
		context.addConnection(new UnboundedPlacesConnection(net, (UnboundedPlacesSet) result[1], state, semantics));
		context.addConnection(new UnboundedSequencesConnection(net, (UnboundedSequences) result[2], state, semantics));
		context.getFutureResult(0).setLabel("Boundedness Analysis of " + net.getLabel());
		context.getFutureResult(1).setLabel("Unbounded Places of " + net.getLabel());
		context.getFutureResult(2).setLabel("Unbounded Sequences of " + net.getLabel());
		return result;

	}

	/**
	 * Static method to check boundedness, given a coverability graph
	 * 
	 * @param graph
	 *            Coverability graph
	 * @return true if the net is bounded, false if it is not
	 */
	public static boolean isBounded(CoverabilityGraph graph) {
		boolean boundedness = true;
		Iterator<?> it = graph.getStates().iterator();
		while (boundedness && it.hasNext()) {
			CTMarking mark = (CTMarking) it.next();
			if (!mark.getOmegaPlaces().isEmpty()) {
				boundedness = false;
			}
		}
		return boundedness;
	}

	/**
	 * Analyze boundedness without further checking of connection
	 * 
	 * @param net
	 *            net to be analyzed
	 * @param state
	 *            Initial state (initial marking)
	 * @param covSet
	 *            Coverability set
	 * @return NetAnalysisInformation about boundedness of this net
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws CancellationException
	 */
	private Object[] analyzeBoundednessAssumingConnection(PluginContext context, PetrinetGraph net, Marking state,
			CoverabilitySet covSet, PetrinetSemantics semantics) throws ConnectionCannotBeObtained {

		// if there is an omega in coverability graph, the graph is not bounded
		boolean boundedness = true;
		SortedSet<Place> unboundedPlaces = new TreeSet<Place>();
		int bound = 0;
		Iterator<CTMarking> it = covSet.iterator();
		while (it.hasNext()) {
			CTMarking mark = it.next();
			if (!mark.getOmegaPlaces().isEmpty()) {
				boundedness = false;
				unboundedPlaces.addAll(mark.getOmegaPlaces());
			} else {
				for (Place p : mark) {
					bound = Math.max(bound, mark.occurrences(p));
				}
			}
		}

		NetAnalysisInformation.BOUNDEDNESS boundednessRes = new NetAnalysisInformation.BOUNDEDNESS();
		UnboundedPlacesSet result2 = new UnboundedPlacesSet();
		result2.add(unboundedPlaces);
		UnboundedSequences result3;

		if (boundedness) {
			boundednessRes.setValue(UnDetBool.TRUE);
			result3 = new UnboundedSequences();
		} else {
			boundednessRes.setValue(UnDetBool.FALSE);
			CoverabilityGraph cg = null;
			cg = context.tryToFindOrConstructFirstObject(CoverabilityGraph.class, CoverabilityGraphConnection.class,
					CoverabilityGraphConnection.STATEPACE, net, state, semantics);

			result3 = getUnboundedSequences(net, state, cg);
		}
		// add connection
		return new Object[] { boundednessRes, result2, result3 };
	}

	private UnboundedSequences getUnboundedSequences(PetrinetGraph net, Marking initialState,
			CoverabilityGraph coverabilityGraph) {
		UnboundedSequences sequences = new UnboundedSequences();
		Collection<State> greenStates = new HashSet<State>();
		Collection<State> yellowStates = new HashSet<State>();
		Collection<State> redStates = new HashSet<State>();

		/**
		 * First, color all states green.
		 */
		greenStates.addAll(coverabilityGraph.getNodes());
		/**
		 * Second, color all unbounded states and their predecessors red.
		 */
		for (State state : coverabilityGraph.getNodes()) {
			CTMarking marking = (CTMarking) state.getIdentifier();
			if (marking.hasOmegaPlace()) {
				colorBackwards(coverabilityGraph, state, redStates, greenStates, initialState);
			}
		}
		/**
		 * Third, color all red predecessors of green states yellow.
		 */
		for (org.processmining.models.graphbased.directed.transitionsystem.Transition edge : coverabilityGraph
				.getEdges()) {
			if (greenStates.contains(edge.getTarget())) {
				colorBackwards(coverabilityGraph, edge.getSource(), yellowStates, redStates, initialState);
			}
		}

		if (yellowStates.isEmpty()) {
			/**
			 * No yellow states, hence no green state, hence unboundedness
			 * inevitable. Put all transitions in the sequence to visualize
			 * this.
			 */
			MultiSet<PetrinetNode> sequence = new TreeMultiSet<PetrinetNode>();
			sequence.addAll(net.getTransitions());
			sequences.add(sequence);
		} else {
			for (org.processmining.models.graphbased.directed.transitionsystem.Transition edge : coverabilityGraph
					.getEdges()) {
				if (yellowStates.contains(edge.getSource()) && redStates.contains(edge.getTarget())) {
					MultiSet<PetrinetNode> sequence = new TreeMultiSet<PetrinetNode>();
					sequence.addAll((Marking) edge.getSource().getIdentifier());
					sequence.add((Transition) edge.getIdentifier());
					sequences.add(sequence);
				}
			}
		}

		return sequences;
	}

	private void colorBackwards(CoverabilityGraph graph, State state, Collection<State> newCollection,
			Collection<State> oldCollection, Marking initialState) {
		if (oldCollection.contains(state)) {
			oldCollection.remove(state);
			newCollection.add(state);
			if (((Marking) state.getIdentifier()).compareTo(initialState) != 0) {
				for (org.processmining.models.graphbased.directed.transitionsystem.Transition edge : graph
						.getInEdges(state)) {
					colorBackwards(graph, edge.getSource(), newCollection, oldCollection, initialState);
				}
			}
		}
	}

}
