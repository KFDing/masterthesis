package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.BitSet;

import org.processmining.plugins.InductiveMiner.Pair;

public class EfficientTreeReduce {

	public static void reduce(EfficientTree tree) throws ReductionFailedException, UnknownTreeNodeException {
		reduce(tree, new EfficientTreeReduceParameters(false, false));
	}

	public static void reduce(EfficientTree tree, EfficientTreeReduceParameters reduceParameters)
			throws ReductionFailedException, UnknownTreeNodeException {
		//filter epsilon subtrees
		{
			BitSet map = EfficientTreeMetrics.canOnlyProduceTau(tree);
			for (int node = tree.getMaxNumberOfNodes() - 1; node >= 0; node--) {
				if (map.get(node) && tree.isOperator(node)) {
					EfficientTreeUtils.replaceNodeWithTau(tree, node);
				}
			}
		}

		//filter superfluous taus under xor, and, seq
		{
			BitSet canProduceTau = EfficientTreeMetrics.canProduceTau(tree);
			Pair<BitSet, int[]> p = isSuperfluousTau(tree, canProduceTau);
			BitSet map = p.getA();
			int[] parents = p.getB();
			for (int node = tree.getMaxNumberOfNodes() - 1; node >= 0; node--) {
				if (map.get(node)) {
					EfficientTreeUtils.removeChild(tree, parents[node], node);
				}
			}
		}
		//this code works, but does not make reducing faster in repeated experiments
		//update: it does massively on trace models

		//apply other filters
		while (reduceOne(tree, reduceParameters)) {

		}

		if (!EfficientTreeUtils.isConsistent(tree)) {
			throw new ReductionFailedException();
		}

	}

	private static boolean reduceOne(EfficientTree tree, EfficientTreeReduceParameters reduceParameters)
			throws UnknownTreeNodeException, ReductionFailedException {
		boolean changed = false;

		for (int node = 0; node < tree.getMaxNumberOfNodes(); node++) {
			if (tree.isOperator(node)) {
				EfficientTreeReductionRule[] rules;
				if (tree.isXor(node)) {
					rules = reduceParameters.getRulesXor();
				} else if (tree.isSequence(node)) {
					rules = reduceParameters.getRulesSequence();
				} else if (tree.isLoop(node)) {
					rules = reduceParameters.getRulesLoop();
				} else if (tree.isConcurrent(node)) {
					rules = reduceParameters.getRulesConcurrent();
				} else if (tree.isInterleaved(node)) {
					rules = reduceParameters.getRulesInterleaved();
				} else if (tree.isOr(node)) {
					rules = reduceParameters.getRulesOr();
				} else {
					throw new UnknownTreeNodeException();
				}

				for (EfficientTreeReductionRule rule : rules) {
					changed = changed | rule.apply(tree, node);;
//					if (!EfficientTreeUtils.isConsistent(tree)) {
//						throw new ReductionFailedException();
//					}
				}
			}
		}

		return changed;
	}

	public static Pair<BitSet, int[]> isSuperfluousTau(EfficientTree tree, BitSet canProduceTau) {
		BitSet superfluous = new BitSet(tree.getMaxNumberOfNodes());
		int[] parents = new int[tree.getMaxNumberOfNodes()];
		for (int node = tree.getMaxNumberOfNodes() - 1; node >= 0; node--) {
			if (tree.isSequence(node) || tree.isConcurrent(node)) {
				//any tau under a sequence or parallel can be removed
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {
						superfluous.set(child, true);
						parents[child] = node;
					}
				}
			} else if (tree.isXor(node)) {
				//see whether there's a child that can produce epsilon
				boolean childProducingEpsilon = false;
				for (int child : tree.getChildren(node)) {
					if (!tree.isTau(child)) {
						childProducingEpsilon |= canProduceTau.get(child);
					}
				}

				//walk again through the children and mark removal
				for (int child : tree.getChildren(node)) {
					if (tree.isTau(child)) {
						//if we have preserved a child that can produce epsilon already; we can remove this tau
						if (childProducingEpsilon) {
							superfluous.set(child, true);
							parents[child] = node;
						} else {
							childProducingEpsilon = true;
						}
					}
				}
			}
		}
		return Pair.of(superfluous, parents);
	}

	public static class ReductionFailedException extends Exception {
		private static final long serialVersionUID = -7417483651057438248L;
	}
}
