package org.processmining.plugins.InductiveMiner.mining;

import java.util.Iterator;

import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2processTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.InductiveMiner.mining.interleaved.MaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter.LogSplitResult;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogStartEndComplete;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;

/**
 * Do not directly call this class, use one of the plug-ins from the
 * InductiveMiner.plugins folder
 * 
 * @author sleemans
 *
 */
public class Miner {

	public static ProcessTree mine(IMLog log, MiningParameters parameters, Canceller canceller) {
		//repair life cycle if necessary
		if (parameters.isRepairLifeCycle()) {
			log = new LifeCycles(parameters.isDebug()).preProcessLog(log);
		}

		if (parameters.isProcessStartEndComplete()) {
			log = IMLogStartEndComplete.fromIMLog(log);
		}

		//create process tree
		ProcessTree tree = new ProcessTreeImpl();
		MinerState minerState = new MinerState(parameters, canceller);
		Node root = mineNode(log, tree, minerState);

		if (canceller.isCancelled()) {
			minerState.shutdownThreadPools();
			return null;
		}

		root.setProcessTree(tree);
		tree.setRoot(root);

		if (canceller.isCancelled()) {
			minerState.shutdownThreadPools();
			return null;
		}

		debug("discovered tree " + tree.getRoot(), minerState);

		//reduce the tree
		if (parameters.getReduceParameters() != null) {
			try {
				//reduce the tree
				EfficientTree eTree = ProcessTree2EfficientTree.convert(tree);
				EfficientTreeReduce.reduce(eTree, parameters.getReduceParameters());
				tree = EfficientTree2processTree.convert(eTree);
				debug("after reduction " + tree.getRoot(), minerState);
			} catch (UnknownTreeNodeException | ReductionFailedException e) {
				e.printStackTrace();
			}
		}

		minerState.shutdownThreadPools();

		if (canceller.isCancelled()) {
			return null;
		}

		return tree;
	}

	public static Node mineNode(IMLog log, ProcessTree tree, MinerState minerState) {

		//construct basic information about log
		IMLogInfo logInfo = minerState.parameters.getLog2LogInfo().createLogInfo(log);

		//output information about the log
		debug("\nMine epsilon=" + logInfo.getDfg().getNumberOfEmptyTraces() + ", " + logInfo.getActivities(),
				minerState);
		//debug(log, minerState);

		//find base cases
		Node baseCase = findBaseCases(log, logInfo, tree, minerState);
		if (baseCase != null) {

			baseCase = postProcess(baseCase, log, logInfo, minerState);

			debug(" discovered node " + baseCase, minerState);
			return baseCase;
		}

		if (minerState.isCancelled()) {
			return null;
		}

		//find cut
		Cut cut = findCut(log, logInfo, minerState);

		if (minerState.isCancelled()) {
			return null;
		}

		if (cut != null && cut.isValid()) {
			//cut is valid

			debug(" chosen cut: " + cut, minerState);

			//split logs
			LogSplitResult splitResult = splitLog(log, logInfo, cut, minerState);

			if (minerState.isCancelled()) {
				return null;
			}

			//make node
			Block newNode;
			try {
				newNode = newNode(cut.getOperator());
			} catch (UnknownTreeNodeException e) {
				e.printStackTrace();
				return null;
			}
			addNode(tree, newNode);

			//recurse
			if (cut.getOperator() != Operator.loop) {
				for (IMLog sublog : splitResult.sublogs) {
					Node child = mineNode(sublog, tree, minerState);

					if (minerState.isCancelled()) {
						return null;
					}

					addChild(newNode, child, minerState);
				}
			} else {
				//loop needs special treatment:
				//ProcessTree requires a ternary loop
				Iterator<IMLog> it = splitResult.sublogs.iterator();

				//mine body
				IMLog firstSublog = it.next();
				{
					Node firstChild = mineNode(firstSublog, tree, minerState);

					if (minerState.isCancelled()) {
						return null;
					}

					addChild(newNode, firstChild, minerState);
				}

				//mine redo parts by, if necessary, putting them under an xor
				Block redoXor;
				if (splitResult.sublogs.size() > 2) {
					redoXor = new Xor("");
					addNode(tree, redoXor);
					addChild(newNode, redoXor, minerState);
				} else {
					redoXor = newNode;
				}
				while (it.hasNext()) {
					IMLog sublog = it.next();
					Node child = mineNode(sublog, tree, minerState);

					if (minerState.isCancelled()) {
						return null;
					}

					addChild(redoXor, child, minerState);
				}

				//add tau as third child
				{
					Node tau = new AbstractTask.Automatic("tau");
					addNode(tree, tau);
					addChild(newNode, tau, minerState);
				}
			}

			Node result = postProcess(newNode, log, logInfo, minerState);

			debug(" discovered node " + result, minerState);
			return result;

		} else {
			//cut is not valid; fall through
			Node result = findFallThrough(log, logInfo, tree, minerState);

			result = postProcess(result, log, logInfo, minerState);

			debug(" discovered node " + result, minerState);
			return result;
		}
	}

	private static Node postProcess(Node newNode, IMLog log, IMLogInfo logInfo, MinerState minerState) {
		for (PostProcessor processor : minerState.parameters.getPostProcessors()) {
			newNode = processor.postProcess(newNode, log, logInfo, minerState);
		}
		return newNode;
	}

	private static Block newNode(Operator operator) throws UnknownTreeNodeException {
		switch (operator) {
			case loop :
				return new AbstractBlock.XorLoop("");
			case concurrent :
				return new AbstractBlock.And("");
			case sequence :
				return new AbstractBlock.Seq("");
			case xor :
				return new AbstractBlock.Xor("");
			case maybeInterleaved :
				return new MaybeInterleaved("");
			case interleaved :
				return new Interleaved("");
			case or :
				return new AbstractBlock.Or("");
		}
		throw new UnknownTreeNodeException();
	}

	public static void addNode(ProcessTree tree, Node node) {
		node.setProcessTree(tree);
		tree.addNode(node);
	}

	public static Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		Node n = null;
		Iterator<BaseCaseFinder> it = minerState.parameters.getBaseCaseFinders().iterator();
		while (n == null && it.hasNext()) {

			if (minerState.isCancelled()) {
				return null;
			}

			n = it.next().findBaseCases(log, logInfo, tree, minerState);
		}
		return n;
	}

	public static Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut c = null;
		Iterator<CutFinder> it = minerState.parameters.getCutFinders().iterator();
		while (it.hasNext() && (c == null || !c.isValid())) {

			if (minerState.isCancelled()) {
				return null;
			}

			c = it.next().findCut(log, logInfo, minerState);
		}
		return c;
	}

	public static Node findFallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		Node n = null;
		Iterator<FallThrough> it = minerState.parameters.getFallThroughs().iterator();
		while (n == null && it.hasNext()) {

			if (minerState.isCancelled()) {
				return null;
			}

			n = it.next().fallThrough(log, logInfo, tree, minerState);
		}
		return n;
	}

	public static LogSplitResult splitLog(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
		LogSplitResult result = minerState.parameters.getLogSplitter().split(log, logInfo, cut, minerState);

		if (minerState.isCancelled()) {
			return null;
		}

		//merge the discarded events of this log splitting into the global discarded events list
		minerState.discardedEvents.addAll(result.discardedEvents);

		return result;
	}

	public static void debug(Object x, MinerState minerState) {
		if (minerState.parameters.isDebug()) {
			System.out.println(x.toString());
		}
	}

	public static void addChild(Block parent, Node child, MinerStateBase minerState) {
		if (!minerState.isCancelled() && parent != null && child != null) {
			parent.addChild(child);
		}
	}
}
