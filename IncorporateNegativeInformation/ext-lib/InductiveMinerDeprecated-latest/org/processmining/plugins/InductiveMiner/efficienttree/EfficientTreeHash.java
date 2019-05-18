package org.processmining.plugins.InductiveMiner.efficienttree;

import java.util.Arrays;

public class EfficientTreeHash {

	/**
	 * 
	 * @param efficientTree
	 * @return a language-based hash of this tree
	 */
	public static String hash(EfficientTree tree) {
		StringBuilder result = new StringBuilder();
		hash(tree, 0, result, false);
		return result.toString();
	}

	public static String hashOnIndices(EfficientTree tree) {
		StringBuilder result = new StringBuilder();
		hash(tree, 0, result, true);
		return result.toString();
	}
	
	public static String hash(EfficientTree tree, int node) {
		StringBuilder result = new StringBuilder();
		hash(tree, node, result, false);
		return result.toString();
	}

	private static int hash(EfficientTree tree, int node, StringBuilder result, boolean hashOnIndices) {
		if (tree.isActivity(node)) {
			if (hashOnIndices) {
				result.append(tree.getActivity(node));
			} else {
				result.append(tree.getActivityName(node));
			}
			return node + 1;
		} else if (tree.isTau(node)) {
			result.append("tau");
			return node + 1;
		} else if (tree.isOperator(node)) {
			if (tree.isXor(node)) {
				result.append("xor(");
				node = childHashesSort(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			} else if (tree.isSequence(node)) {
				result.append("seq(");
				node = childHashes(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			} else if (tree.isLoop(node)) {
				result.append("loop(");
				node = childHashes(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			} else if (tree.isConcurrent(node)) {
				result.append("and(");
				node = childHashesSort(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			} else if (tree.isInterleaved(node)) {
				result.append("int(");
				node = childHashesSort(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			} else if (tree.isOr(node)) {
				result.append("or(");
				node = childHashesSort(tree, node, result, hashOnIndices);
				result.append(")");
				return node;
			}
		}
		throw new RuntimeException("tree construct not implemented.");
	}

	private static int childHashesSort(EfficientTree tree, int node, StringBuilder result, boolean hashOnIndices) {
		int numberOfChildren = tree.getNumberOfChildren(node);

		//compute the child hashes
		String[] childHashes = new String[numberOfChildren];
		node++;
		for (int j = 0; j < numberOfChildren; j++) {
			StringBuilder builder = new StringBuilder();
			node = hash(tree, node, builder, hashOnIndices);
			childHashes[j] = builder.toString();
		}
		Arrays.sort(childHashes);
		// Format the ArrayList as a string, similar to implode
		result.append(childHashes[0]);
		for (int j = 1; j < childHashes.length; j++) {
			result.append(",");
			result.append(childHashes[j]);
		}

		return node;
	}

	private static int childHashes(EfficientTree tree, int node, StringBuilder result, boolean hashOnIndices) {
		int numberOfChildren = tree.getNumberOfChildren(node);
		node++;
		node = hash(tree, node, result, hashOnIndices);
		for (int j = 1; j < numberOfChildren; j++) {
			result.append(",");
			node = hash(tree, node, result, hashOnIndices);
		}

		return node;
	}
}
