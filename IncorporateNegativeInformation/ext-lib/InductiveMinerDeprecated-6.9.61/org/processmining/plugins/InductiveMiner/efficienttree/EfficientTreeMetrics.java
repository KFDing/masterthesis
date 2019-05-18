package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.BitSet;

public class EfficientTreeMetrics {

	public static long getShortestTrace(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isActivity(node)) {
			return 1;
		} else if (tree.isTau(node)) {
			return 0;
		} else {
			if (tree.isXor(node)) {
				long result = Long.MAX_VALUE;
				for (int child : tree.getChildren(node)) {
					result = Math.min(result, getShortestTrace(tree, child));
				}
				return result;
			} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
				int result = 0;
				for (int child : tree.getChildren(node)) {
					result += getShortestTrace(tree, child);
				}
				return result;
			} else if (tree.isLoop(node)) {
				return getShortestTrace(tree, tree.getChild(node, 0)) + getShortestTrace(tree, tree.getChild(node, 2));
			} else {
				throw new UnknownTreeNodeException();
			}
		}
	}

	/**
	 * 
	 * @param tree
	 * @return a bitset that denotes whether a node can only produce the empty
	 *         trace.
	 */
	public static BitSet canOnlyProduceTau(EfficientTree tree) {
		BitSet result = new BitSet(tree.getMaxNumberOfNodes());
		for (int node = tree.getMaxNumberOfNodes() - 1; node >= 0; node--) {
			if (tree.isActivity(node)) {
				result.set(node, false);
			} else if (tree.isTau(node)) {
				result.set(node, true);
			} else if (tree.isOperator(node)) {
				boolean bresult = true;
				for (int child : tree.getChildren(node)) {
					bresult = bresult && result.get(child);
				}
				result.set(node, bresult);
			}
		}
		return result;
	}

	public static boolean canOnlyProduceTau(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isActivity(node)) {
			return false;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isOperator(node)) {
			for (int child : tree.getChildren(node)) {
				if (!canOnlyProduceTau(tree, child)) {
					return false;
				}
			}
			return true;
		}
		throw new UnknownTreeNodeException();
	}

	public static BitSet canProduceTau(EfficientTree tree) {
		BitSet result = new BitSet(tree.getMaxNumberOfNodes());
		for (int node = tree.getMaxNumberOfNodes() - 1; node >= 0; node--) {
			if (tree.isActivity(node)) {
				result.set(node, false);
			} else if (tree.isTau(node)) {
				result.set(node, true);
			} else if (tree.isXor(node)) {
				boolean bresult = false;
				for (int child : tree.getChildren(node)) {
					bresult = bresult || result.get(child);
				}
				result.set(node, bresult);
			} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
				boolean bresult = true;
				for (int child : tree.getChildren(node)) {
					bresult = bresult && result.get(child);
				}
				result.set(node, bresult);
			} else if (tree.isLoop(node)) {
				result.set(node, result.get(tree.getChild(node, 0)) && result.get(tree.getChild(node, 2)));
			}
		}
		return result;
	}

	public static boolean canProduceTau(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isActivity(node)) {
			return false;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isXor(node) || tree.isOr(node)) {
			boolean bresult = false;
			for (int child : tree.getChildren(node)) {
				bresult = bresult || canProduceTau(tree, child);
			}
			return bresult;
		} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
			boolean bresult = true;
			for (int child : tree.getChildren(node)) {
				bresult = bresult && canProduceTau(tree, child);
			}
			return bresult;
		} else if (tree.isLoop(node)) {
			return canProduceTau(tree, tree.getChild(node, 0)) && canProduceTau(tree, tree.getChild(node, 2));
		}
		throw new UnknownTreeNodeException();
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @param activity
	 * @return whether the given node can produce the trace -activity-
	 * @throws UnknownTreeNodeException
	 */
	public static boolean canProduceSingleActivity(EfficientTree tree, int node, int activity)
			throws UnknownTreeNodeException {
		if (tree.isTau(node)) {
			return false;
		} else if (tree.isActivity(node)) {
			return tree.getActivity(node) == activity;
		} else if (tree.isXor(node) || tree.isOr(node)) {
			for (int child : tree.getChildren(node)) {
				if (canProduceSingleActivity(tree, child, activity)) {
					return true;
				}
			}
			return false;
		} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)) {
			//gather information
			boolean[] canProduceTau = new boolean[tree.getNumberOfChildren(node)];
			boolean[] canProduceSingleActivity = new boolean[tree.getNumberOfChildren(node)];
			{
				int i = 0;
				for (int child : tree.getChildren(node)) {
					canProduceTau[i] = canProduceTau(tree, child);
					canProduceSingleActivity[i] = canProduceSingleActivity(tree, child, activity);
					i++;
				}
			}

			//check
			//one child has to be able to produce the single activity, while the others can produce tau
			for (int i = 0; i < tree.getNumberOfChildren(node); i++) {
				if (canProduceSingleActivity[i]) {
					boolean allTau = true;
					for (int j = 0; j < tree.getNumberOfChildren(node); j++) {
						if (j != i) {
							allTau = allTau && canProduceTau[j];
						}
					}
					if (allTau) {
						return true;
					}
				}
			}
			return false;
		} else if (tree.isLoop(node)) {
			int body = tree.getChild(node, 0);
			int redo = tree.getChild(node, 1);
			int exit = tree.getChild(node, 2);

			boolean bodyCanProduceTau = canProduceTau(tree, body);
			boolean exitCanProduceTau = canProduceTau(tree, exit);

			//case 1: body a, exit tau
			if (canProduceSingleActivity(tree, body, activity) && exitCanProduceTau) {
				return true;
			}

			//case 2: body tau, exit a
			if (bodyCanProduceTau && canProduceSingleActivity(tree, exit, activity)) {
				return true;
			}

			//case 3: body tau, redo a, exit tau
			if (bodyCanProduceTau && canProduceSingleActivity(tree, redo, activity) && exitCanProduceTau) {
				return true;
			}
			return false;
		}
		throw new UnknownTreeNodeException();
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @return whether each trace of the node has a length of at most one.
	 * @throws UnknownTreeNodeException
	 */
	public static boolean traceLengthAtMostOne(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isActivity(node)) {
			return true;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isOperator(node)) {
			if (tree.isXor(node)) {
				for (int child : tree.getChildren(node)) {
					if (!traceLengthAtMostOne(tree, child)) {
						return false;
					}
				}
				return true;
			} else if (tree.isSequence(node) || tree.isConcurrent(node) || tree.isInterleaved(node)
					|| tree.isOr(node)) {
				//one child can produce a singleton trace, the others cannot anymore then
				boolean singletonTraceChildSeen = false;
				for (int child : tree.getChildren(node)) {
					if (!onlyEmptyTrace(tree, node)) {
						//empty children are not worrying
						if (!traceLengthAtMostOne(tree, child)) {
							return false;
						}
						if (singletonTraceChildSeen) {
							return false;
						}
						singletonTraceChildSeen = true;
					}
				}
				return true;
			} else if (tree.isLoop(node)) {
				return onlyEmptyTrace(tree, node);
			}
		}
		throw new UnknownTreeNodeException();
	}

	/**
	 * 
	 * @param tree
	 * @param node
	 * @return whether each trace of the node has a length of at most zero.
	 * @throws UnknownTreeNodeException
	 */
	public static boolean onlyEmptyTrace(EfficientTree tree, int node) throws UnknownTreeNodeException {
		if (tree.isActivity(node)) {
			return false;
		} else if (tree.isTau(node)) {
			return true;
		} else if (tree.isOperator(node)) {
			for (int child : tree.getChildren(node)) {
				if (!onlyEmptyTrace(tree, child)) {
					return false;
				}
			}
			return true;
		}
		throw new UnknownTreeNodeException();
	}
}
