package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree.NodeType;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Or;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;

public class ProcessTree2EfficientTree {

	/**
	 * Convert a process tree into a efficient tree
	 * 
	 * @param processTree
	 * @return
	 * @throws UnknownTreeNodeException
	 */
	public static EfficientTree convert(ProcessTree processTree) throws UnknownTreeNodeException {
		return convert(processTree.getRoot());
	}

	/**
	 * Convert a process tree node to a efficient tree
	 * 
	 * @param node
	 * @return
	 * @throws UnknownTreeNodeException
	 */
	public static EfficientTree convert(Node node) throws UnknownTreeNodeException {
		Triple<int[], TObjectIntMap<String>, String[]> t = tree2efficientTree(node);
		return EfficientTreeFactory.create(t.getA(), t.getB(), t.getC());
	}

	/**
	 * Convert a process tree into a efficient tree
	 * 
	 * @param node
	 * @return
	 * @throws UnknownTreeNodeException
	 */
	public static Triple<int[], TObjectIntMap<String>, String[]> tree2efficientTree(Node node)
			throws UnknownTreeNodeException {
		TIntArrayList efficientTree = new TIntArrayList();
		TObjectIntMap<String> activity2int = EfficientTreeImpl.getEmptyActivity2int();
		List<String> int2activity = new ArrayList<>();
		node2efficientTree(node, efficientTree, activity2int, int2activity);

		return Triple.of(efficientTree.toArray(new int[efficientTree.size()]), activity2int,
				int2activity.toArray(new String[int2activity.size()]));
	}

	private static void node2efficientTree(Node node, TIntArrayList efficientTree, TObjectIntMap<String> activity2int,
			List<String> int2activity2) throws UnknownTreeNodeException {
		if (node instanceof Automatic) {
			efficientTree.add(NodeType.tau.code);
		} else if (node instanceof Manual) {
			int max = int2activity2.size();
			String name = node.getName();
			if (!activity2int.containsKey(name)) {
				activity2int.put(name, max);
				int2activity2.add(name);
			}
			efficientTree.add(activity2int.get(name));
		} else if (node instanceof Xor || node instanceof Def) {
			node2efficientTreeChildren(NodeType.xor, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Seq) {
			node2efficientTreeChildren(NodeType.sequence, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Interleaved) {
			node2efficientTreeChildren(NodeType.interleaved, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof And) {
			node2efficientTreeChildren(NodeType.concurrent, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof XorLoop || node instanceof DefLoop) {
			node2efficientTreeChildren(NodeType.loop, node, efficientTree, activity2int, int2activity2);
		} else if (node instanceof Or) {
			node2efficientTreeChildren(NodeType.or, node, efficientTree, activity2int, int2activity2);
		} else {
			throw new UnknownTreeNodeException();
		}
	}

	private static void node2efficientTreeChildren(NodeType operator, Node node, TIntArrayList result,
			TObjectIntMap<String> activity2int, List<String> int2activity) throws UnknownTreeNodeException {
		result.add(operator.code - (EfficientTreeImpl.childrenFactor * ((Block) node).getChildren().size()));
		for (Node child : ((Block) node).getChildren()) {
			node2efficientTree(child, result, activity2int, int2activity);
		}
	}
}
