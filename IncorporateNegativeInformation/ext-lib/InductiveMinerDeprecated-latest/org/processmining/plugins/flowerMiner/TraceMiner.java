package org.processmining.plugins.flowerMiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeFactory;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeImpl;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.processtree.ProcessTree;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;

@Plugin(name = "Mine Process tree using Trace Miner", returnLabels = { "Process Tree" }, returnTypes = {
		ProcessTree.class }, parameterLabels = { "Log" }, userAccessible = true, level = PluginLevel.Regular)
public class TraceMiner {

	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a flower Petri net", requiredParameterLabels = { 0 })
	public ProcessTree mineDefaultPetrinet(PluginContext context, XLog log) {
		return EfficientTree2processTree.convert(mineTraceModel(log, MiningParameters.getDefaultClassifier()));
	}

	public static EfficientTree mineTraceModel(XLog log, XEventClassifier classifier) {
		//initialise the tree
		final TObjectIntMap<String> activity2int = EfficientTreeImpl.getEmptyActivity2int();
		final ArrayList<String> int2activity = new ArrayList<>();
		final TIntArrayList tree = new TIntArrayList();
		if (!log.isEmpty()) {
			tree.add(NodeType.xor.code);

			Set<int[]> set = new TCustomHashSet<int[]>(new HashingStrategy<int[]>() {

				private static final long serialVersionUID = 5948289403059749878L;

				public int computeHashCode(int[] object) {
					return Arrays.hashCode(object);
				}

				public boolean equals(int[] o1, int[] o2) {
					return Arrays.equals(o1, o2);
				}
			});

			//parse the traces
			for (XTrace trace : log) {
				int[] iTrace = new int[trace.size()];
				int i = 0;
				for (XEvent event : trace) {
					String activity = classifier.getClassIdentity(event);
					int actIndex = activity2int.putIfAbsent(activity, int2activity.size());
					if (actIndex == activity2int.getNoEntryValue()) {
						iTrace[i] = int2activity.size();
						int2activity.add(activity);
					} else {
						iTrace[i] = actIndex;
					}
					i++;
				}

				if (!set.contains(iTrace)) {
					set.add(iTrace);

					//start the trace
					if (trace.isEmpty()) {
						tree.add(NodeType.tau.code);
					} else {
						tree.add(NodeType.sequence.code - trace.size() * EfficientTreeImpl.childrenFactor);
						for (XEvent event : trace) {
							String activity = classifier.getClassIdentity(event);
							int actIndex = activity2int.putIfAbsent(activity, int2activity.size());
							if (actIndex == activity2int.getNoEntryValue()) {
								tree.add(int2activity.size());
								int2activity.add(activity);
							} else {
								tree.add(actIndex);
							}
						}
					}
					tree.set(0, tree.get(0) - EfficientTreeImpl.childrenFactor);
				}
			}
		} else {
			tree.add(NodeType.tau.code);
		}

		//construct the tree
		String[] int2activity2 = new String[activity2int.size()];
		for (TObjectIntIterator<String> it = activity2int.iterator(); it.hasNext();) {
			it.advance();
			int2activity2[it.value()] = it.key();
		}

		return EfficientTreeFactory.create(tree.toArray(), activity2int, int2activity2);
	}
}
