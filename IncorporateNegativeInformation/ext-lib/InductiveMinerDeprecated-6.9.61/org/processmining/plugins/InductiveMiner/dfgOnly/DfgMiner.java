package org.processmining.plugins.InductiveMiner.dfgOnly;

import java.util.Iterator;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.DfgSplitter.DfgSplitResult;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class DfgMiner {
	public static ProcessTree mine(Dfg dfg, DfgMiningParameters parameters, Canceller canceller) {
		//create process tree
		ProcessTree tree = new ProcessTreeImpl();
		DfgMinerState minerState = new DfgMinerState(parameters, canceller);
		Node root = mineNode(dfg, tree, minerState);

		root.setProcessTree(tree);
		tree.setRoot(root);

		if (canceller.isCancelled()) {
			return null;
		}

		debug(tree.getRoot(), minerState);

		//reduce the tree
		try {
			EfficientTree efficientTree = ProcessTree2EfficientTree.convert(tree);
			EfficientTreeReduce.reduce(efficientTree);
			tree = EfficientTree2processTree.convert(efficientTree);
		} catch (Exception e) {
			debug("reduction failed", minerState);
		}
		debug("after reduction " + tree.getRoot(), minerState);

		minerState.shutdownThreadPools();

		if (canceller.isCancelled()) {
			return null;
		}

		return tree;
	}

	public static Node mineNode(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {

		//find base cases
		Node baseCase = findBaseCases(dfg, tree, minerState);
		if (baseCase != null) {
			return baseCase;
		}

		//find cut
		Cut cut = findCut(dfg, minerState);
		if (cut != null && cut.isValid()) {
			//cut is valid

			debug(" chosen cut: " + cut, minerState);

			//split logs
			DfgSplitResult splitResult = splitLog(dfg, cut, minerState);

			//make node
			Block newNode = newNode(cut.getOperator());
			addNode(tree, newNode);

			//recurse
			if (cut.getOperator() != Operator.loop) {
				for (Dfg subDfg : splitResult.subDfgs) {
					Node child = mineNode(subDfg, tree, minerState);
					Miner.addChild(newNode, child, minerState);
				}
			} else {
				//loop needs special treatment:
				//ProcessTree requires a ternary loop
				Iterator<Dfg> it = splitResult.subDfgs.iterator();

				//mine body
				Dfg firstSubDfg = it.next();
				{
					Node firstChild = mineNode(firstSubDfg, tree, minerState);
					Miner.addChild(newNode, firstChild, minerState);
				}

				//mine redo parts by, if necessary, putting them under an xor
				Block redoXor;
				if (splitResult.subDfgs.size() > 2) {
					redoXor = new Xor("");
					addNode(tree, redoXor);
					Miner.addChild(newNode, redoXor, minerState);
				} else {
					redoXor = newNode;
				}
				while (it.hasNext()) {
					Dfg subDfg = it.next();
					Node child = mineNode(subDfg, tree, minerState);
					Miner.addChild(redoXor, child, minerState);
				}

				//add tau as third child
				{
					Node tau = new AbstractTask.Automatic("tau");
					addNode(tree, tau);
					Miner.addChild(newNode, tau, minerState);
				}
			}

			return newNode;

		} else {
			//cut is not valid; fall through
			return findFallThrough(dfg, tree, minerState);
		}
	}

	private static Block newNode(Operator operator) {
		if (operator == Operator.xor) {
			return new AbstractBlock.Xor("");
		} else if (operator == Operator.sequence) {
			return new AbstractBlock.Seq("");
		} else if (operator == Operator.concurrent) {
			return new AbstractBlock.And("");
		} else if (operator == Operator.loop) {
			return new AbstractBlock.XorLoop("");
		}
		return null;
	}

	public static void addNode(ProcessTree tree, Node node) {
		node.setProcessTree(tree);
		tree.addNode(node);
	}

	public static Node findBaseCases(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		Node n = null;
		Iterator<DfgBaseCaseFinder> it = minerState.getParameters().getDfgBaseCaseFinders().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().findBaseCases(dfg, tree, minerState);
		}
		return n;
	}

	public static Cut findCut(Dfg dfg, DfgMinerState minerState) {
		return minerState.getParameters().getDfgCutFinder().findCut(dfg, minerState);
	}

	public static Node findFallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		Node n = null;
		Iterator<DfgFallThrough> it = minerState.getParameters().getDfgFallThroughs().iterator();
		while (n == null && it.hasNext()) {
			n = it.next().fallThrough(dfg, tree, minerState);
		}
		return n;
	}

	public static DfgSplitResult splitLog(Dfg dfg, Cut cut, DfgMinerState minerState) {
		return minerState.getParameters().getDfgSplitter().split(dfg, cut, minerState);
	}

	public static void debug(Object x, DfgMinerState minerState) {
		if (minerState.getParameters().isDebug()) {
			System.out.println(x.toString());
		}
	}
}
