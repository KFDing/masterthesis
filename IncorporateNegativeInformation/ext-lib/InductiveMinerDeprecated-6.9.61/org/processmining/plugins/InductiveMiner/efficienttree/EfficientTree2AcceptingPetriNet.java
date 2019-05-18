package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.concurrent.atomic.AtomicInteger;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;

public class EfficientTree2AcceptingPetriNet {

	public static AtomicInteger placeCounter = new AtomicInteger();

	public static AcceptingPetriNet convert(EfficientTree tree) {
		Petrinet petriNet = new PetrinetImpl("converted from efficient tree");
		Place source = petriNet.addPlace("net source");
		Place sink = petriNet.addPlace("net sink");
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		int root = tree.getRoot();

		convertNode(petriNet, tree, root, source, sink);

		return AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet, initialMarking, finalMarking);
	}

	private static void convertNode(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		if (tree.isTau(node)) {
			convertTau(petriNet, tree, node, source, sink);
		} else if (tree.isActivity(node)) {
			convertTask(petriNet, tree, node, source, sink);
		} else if (tree.isConcurrent(node)) {
			convertAnd(petriNet, tree, node, source, sink);
		} else if (tree.isSequence(node)) {
			convertSeq(petriNet, tree, node, source, sink);
		} else if (tree.isXor(node)) {
			convertXor(petriNet, tree, node, source, sink);
		} else if (tree.isLoop(node)) {
			convertLoop(petriNet, tree, node, source, sink);
		} else if (tree.isOr(node)) {
			convertOr(petriNet, tree, node, source, sink);
		} else if (tree.isInterleaved(node)) {
			convertInterleaved(petriNet, tree, node, source, sink);
		} else {
			throw new RuntimeException("not implemented");
		}
	}

	private static void convertTau(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		Transition t = petriNet.addTransition("tau from tree");
		t.setInvisible(true);
		petriNet.addArc(source, t);
		petriNet.addArc(t, sink);
	}

	private static void convertTask(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		Transition t = petriNet.addTransition(tree.getActivityName(node));
		petriNet.addArc(source, t);
		petriNet.addArc(t, sink);
	}

	private static void convertXor(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		for (int child : tree.getChildren(node)) {
			convertNode(petriNet, tree, child, source, sink);
		}
	}

	private static void convertSeq(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		int last = tree.getNumberOfChildren(node);
		int i = 0;
		Place lastSink = source;
		for (int child : tree.getChildren(node)) {
			Place childSink;
			if (i == last - 1) {
				childSink = sink;
			} else {
				childSink = petriNet.addPlace("sink " + placeCounter.incrementAndGet());
			}

			convertNode(petriNet, tree, child, lastSink, childSink);
			lastSink = childSink;
			i++;
		}
	}

	private static void convertAnd(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		//add split tau
		Transition t1 = petriNet.addTransition("tau split");
		t1.setInvisible(true);
		petriNet.addArc(source, t1);

		//add join tau
		Transition t2 = petriNet.addTransition("tau join");
		t2.setInvisible(true);
		petriNet.addArc(t2, sink);

		//add for each child a source and sink place
		for (int child : tree.getChildren(node)) {
			Place childSource = petriNet.addPlace("source " + placeCounter.incrementAndGet());
			petriNet.addArc(t1, childSource);

			Place childSink = petriNet.addPlace("sink " + placeCounter.incrementAndGet());
			petriNet.addArc(childSink, t2);

			convertNode(petriNet, tree, child, childSource, childSink);
		}
	}

	private static void convertLoop(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		if (tree.getNumberOfChildren(node) != 3) {
			//a loop must have precisely three children: body, redo and exit
			throw new RuntimeException("A loop should have precisely three children");
		}

		Place middlePlace = petriNet.addPlace("middle " + placeCounter.incrementAndGet());

		//add an extra tau
		Transition t = petriNet.addTransition("tau start");
		t.setInvisible(true);
		petriNet.addArc(source, t);
		//replace the source
		source = petriNet.addPlace("replacement source " + placeCounter.incrementAndGet());
		petriNet.addArc(t, source);

		//body
		convertNode(petriNet, tree, tree.getChild(node, 0), source, middlePlace);
		//redo
		convertNode(petriNet, tree, tree.getChild(node, 1), middlePlace, source);
		//exit
		convertNode(petriNet, tree, tree.getChild(node, 2), middlePlace, sink);
	}

	private static void convertOr(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {

		Transition start = petriNet.addTransition("tau start");
		start.setInvisible(true);
		petriNet.addArc(source, start);

		Place notDoneFirst = petriNet.addPlace("notDoneFirst " + placeCounter.incrementAndGet());
		petriNet.addArc(start, notDoneFirst);

		Place doneFirst = petriNet.addPlace("doneFirst " + placeCounter.incrementAndGet());
		Transition end = petriNet.addTransition("tau finish");
		end.setInvisible(true);
		petriNet.addArc(doneFirst, end);
		petriNet.addArc(end, sink);

		for (int child : tree.getChildren(node)) {
			Place childSource = petriNet.addPlace("childSource " + placeCounter.incrementAndGet());
			petriNet.addArc(start, childSource);
			Place childSink = petriNet.addPlace("childSink " + placeCounter.incrementAndGet());
			petriNet.addArc(childSink, end);
			Place doChild = petriNet.addPlace("doChild " + placeCounter.incrementAndGet());

			//skip
			Transition skipChild = petriNet.addTransition("tau skipChild");
			skipChild.setInvisible(true);
			petriNet.addArc(childSource, skipChild);
			petriNet.addArc(skipChild, childSink);
			petriNet.addArc(skipChild, doneFirst);
			petriNet.addArc(doneFirst, skipChild);

			//first do
			Transition firstDoChild = petriNet.addTransition("tau firstDoChild");
			firstDoChild.setInvisible(true);
			petriNet.addArc(childSource, firstDoChild);
			petriNet.addArc(notDoneFirst, firstDoChild);
			petriNet.addArc(firstDoChild, doneFirst);
			petriNet.addArc(firstDoChild, doChild);

			//later do
			Transition laterDoChild = petriNet.addTransition("tau laterDoChild");
			laterDoChild.setInvisible(true);
			petriNet.addArc(childSource, laterDoChild);
			petriNet.addArc(laterDoChild, doChild);
			petriNet.addArc(laterDoChild, doneFirst);
			petriNet.addArc(doneFirst, laterDoChild);

			convertNode(petriNet, tree, child, doChild, childSink);
		}
	}

	private static void convertInterleaved(Petrinet petriNet, EfficientTree tree, int node, Place source, Place sink) {
		Transition start = petriNet.addTransition("tau start");
		start.setInvisible(true);
		petriNet.addArc(source, start);

		Place mileStone = petriNet.addPlace("milestone place " + placeCounter.incrementAndGet());
		petriNet.addArc(start, mileStone);

		Transition end = petriNet.addTransition("tau end");
		end.setInvisible(true);
		petriNet.addArc(source, end);
		petriNet.addArc(mileStone, end);
		petriNet.addArc(end, sink);

		for (int child : tree.getChildren(node)) {
			Place childTodo = petriNet.addPlace("child todo " + placeCounter.incrementAndGet());
			petriNet.addArc(start, childTodo);

			Transition startChild = petriNet.addTransition("tau start child");
			startChild.setInvisible(true);
			petriNet.addArc(childTodo, startChild);
			petriNet.addArc(mileStone, startChild);

			Place childSource = petriNet.addPlace("child source " + placeCounter.incrementAndGet());
			petriNet.addArc(startChild, childSource);

			Place childSink = petriNet.addPlace("child sink " + placeCounter.incrementAndGet());

			Transition endChild = petriNet.addTransition("tau end child");
			endChild.setInvisible(true);
			petriNet.addArc(childSink, endChild);
			petriNet.addArc(endChild, mileStone);

			Place childDone = petriNet.addPlace("child done " + placeCounter.incrementAndGet());
			petriNet.addArc(endChild, childDone);
			petriNet.addArc(childDone, end);

			convertNode(petriNet, tree, child, childSource, childSink);
		}
	}
}