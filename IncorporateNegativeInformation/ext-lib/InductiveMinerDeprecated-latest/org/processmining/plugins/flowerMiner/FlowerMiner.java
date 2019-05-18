package org.processmining.plugins.flowerMiner;

import java.util.Collection;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeFactory;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeImpl;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import gnu.trove.map.TObjectIntMap;

@Plugin(name = "Mine Petri net using Flower Miner", returnLabels = { "Petri net", "Initial marking",
		"Final marking" }, returnTypes = { Petrinet.class, Marking.class,
				Marking.class }, parameterLabels = { "Log" }, userAccessible = true, level = PluginLevel.Regular)
public class FlowerMiner {

	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = { 0 })
	public Object[] mineDefaultPetrinet(PluginContext context, XLog log) {
		XEventClassifier classifier = MiningParameters.getDefaultClassifier();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClass dummy = new XEventClass("", 1);

		Object[] result = mineParametersPetrinet(logInfo, classifier, dummy);
		Petrinet petrinet = (Petrinet) result[0];
		Marking initialMarking = (Marking) result[1];
		Marking finalMarking = (Marking) result[2];
		TransEvClassMapping mapping = (TransEvClassMapping) result[3];

		//create connections
		//		TODO: re-enable once replayer package is updated
		//		context.addConnection(new LogPetrinetConnectionImpl(log, logInfo.getEventClasses(), petrinet, mapping));
		context.addConnection(new InitialMarkingConnection(petrinet, initialMarking));
		context.addConnection(new FinalMarkingConnection(petrinet, finalMarking));
		context.addConnection(new EvClassLogPetrinetConnection("classifier-log-petrinet connection", petrinet, log,
				classifier, mapping));

		return new Object[] { petrinet, initialMarking, finalMarking };
	}

	/*
	 * 'mines' a flower model returns array of Object (petrinet, initial
	 * marking, final marking, mapping)
	 */
	public static Object[] mineParametersPetrinet(XLogInfo logInfo, XEventClassifier classifier, XEventClass dummy) {

		Petrinet net = new PetrinetImpl("flower");
		Place source = net.addPlace("source");
		Place sink = net.addPlace("sink");
		Place stigma = net.addPlace("stigma");
		TransEvClassMapping mapping = new TransEvClassMapping(classifier, dummy);

		Transition start = net.addTransition("start");
		start.setInvisible(true);
		net.addArc(source, start);
		net.addArc(start, stigma);
		mapping.put(start, dummy);

		Transition end = net.addTransition("end");
		end.setInvisible(true);
		net.addArc(stigma, end);
		net.addArc(end, sink);
		mapping.put(end, dummy);

		for (XEventClass activity : logInfo.getEventClasses().getClasses()) {
			Transition t = net.addTransition(activity.toString());
			net.addArc(stigma, t);
			net.addArc(t, stigma);
			mapping.put(t, activity);
		}

		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		return new Object[] { net, initialMarking, finalMarking, mapping };
	}

	public static EfficientTree mine(XLogInfo logInfo) {
		Collection<XEventClass> classes = logInfo.getEventClasses().getClasses();
		Iterator<XEventClass> it = classes.iterator();

		//construct activity structures
		String[] int2activity = new String[classes.size()];
		TObjectIntMap<String> activity2int = EfficientTreeImpl.getEmptyActivity2int();
		for (int i = 0; i < classes.size(); i++) {
			XEventClass id = it.next();
			int2activity[i] = id.getId();
			activity2int.put(id.getId(), i);
		}

		//construct the tree
		int[] tree = new int[int2activity.length + 4];
		tree[0] = NodeType.loop.code - 3 * EfficientTreeImpl.childrenFactor;
		assert (tree[0] < 0);
		tree[1] = NodeType.xor.code - int2activity.length * EfficientTreeImpl.childrenFactor;
		assert (tree[1] < 0);
		for (int i = 0; i < int2activity.length; i++) {
			tree[2 + i] = i;
		}
		tree[2 + int2activity.length] = NodeType.tau.code;
		tree[3 + int2activity.length] = NodeType.tau.code;

		return EfficientTreeFactory.create(tree, activity2int, int2activity);
	}
}
