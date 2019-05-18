package org.processmining.plugins.InductiveMiner.mining.interleaved;

import org.processmining.plugins.InductiveMiner.conversion.ReduceTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParameters;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.Miner;
import org.processmining.plugins.InductiveMiner.mining.interleaved.FootPrint.DfgUnfoldedNode;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Node;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractBlock;

public class DetectInterleaved {

	public static Block remove(MaybeInterleaved node, EfficientTreeReduceParameters reduceParameters) throws UnknownTreeNodeException {
		
		//reduce children
		ReduceTree.reduceChildrenOf(node, reduceParameters);

		Block twoSequence = findSequences(node);
		if (twoSequence != null) {
			return twoSequence;
		}
		
		Block two = removeTwo(node);
		if (two != null) {
			return two;
		}

		return node;
	}

	public static Block removeTwo(MaybeInterleaved node) {
		//this method only works for n=2
		if (node.getChildren().size() != 2) {
			return null;
		}

		//check whether all children are sequences
		for (Node child : node.getChildren()) {
			if (!(child instanceof Seq)) {
				return null;
			}
		}

		//check whether footprints are equal
		UnfoldedNode grandChildA1 = new UnfoldedNode(((Block) node.getChildren().get(0)).getChildren().get(0));
		UnfoldedNode grandChildA2 = new UnfoldedNode(((Block) node.getChildren().get(0)).getChildren().get(1));
		UnfoldedNode grandChildB1 = new UnfoldedNode(((Block) node.getChildren().get(1)).getChildren().get(0));
		UnfoldedNode grandChildB2 = new UnfoldedNode(((Block) node.getChildren().get(1)).getChildren().get(1));

		DfgUnfoldedNode A1 = FootPrint.makeDfg(grandChildA1);
		DfgUnfoldedNode A2 = FootPrint.makeDfg(grandChildA2);
		DfgUnfoldedNode B1 = FootPrint.makeDfg(grandChildB1);
		DfgUnfoldedNode B2 = FootPrint.makeDfg(grandChildB2);

		if (!(A1.equals(B2) || A2.equals(B1))) {
			return null;
		}

		//just pick the first children
		Block newNode = new Interleaved("");
		Miner.addNode(node.getProcessTree(), newNode);

		for (Node grandChild : ((Block) node.getChildren().get(0)).getChildren()) {
			newNode.addChild(grandChild);
		}

		return newNode;
	}

	/**
	 * Detect special case: maybeInterleaved(sequence(a, b, c, d, e),
	 * sequence(d, e, a, b, c))
	 * 
	 * @return
	 */
	public static Block findSequences(MaybeInterleaved node) {
		//this method only works for n=2
		if (node.getChildren().size() != 2) {
			return null;
		}

		//check whether all children are sequences
		for (Node child : node.getChildren()) {
			if (!(child instanceof Seq)) {
				return null;
			}
		}

		Seq seqA = (Seq) node.getChildren().get(0);
		Seq seqB = (Seq) node.getChildren().get(1);
		
		if (seqA.getChildren().size() != seqB.getChildren().size()) {
			return null;
		}

		UnfoldedNode grandChildB1 = new UnfoldedNode(seqB.getChildren().get(0));
		DfgUnfoldedNode B1 = FootPrint.makeDfg(grandChildB1);

		//search for the split point (i.e. c -> d)
		int splitPointA = -1;
		for (int i = 1; i < seqA.getChildren().size(); i++) {
			DfgUnfoldedNode Ai = FootPrint.makeDfg(new UnfoldedNode(seqA.getChildren().get(i)));
			if (Ai.equals(B1) && checkSplitPoint(seqA, seqB, i)) {
				splitPointA = i;
				break;
			}
		}
		if (splitPointA == -1) {
			return null;
		}

		//split and return: int(seq(a, b, c), seq(d, e))
		//just pick the first children
		Block newNode = new Interleaved("");
		Miner.addNode(node.getProcessTree(), newNode);
		
		Seq newSeqA = new AbstractBlock.Seq("");
		Miner.addNode(node.getProcessTree(), newSeqA);
		newNode.addChild(newSeqA);
		for (int i = 0; i < splitPointA; i++) {
			newSeqA.addChild(seqA.getChildren().get(i));
		}
		
		Seq newSeqB = new AbstractBlock.Seq("");
		Miner.addNode(node.getProcessTree(), newSeqB);
		newNode.addChild(newSeqB);
		for (int i = splitPointA; i < seqA.getChildren().size(); i++) {
			newSeqB.addChild(seqA.getChildren().get(i));
		}

		return newNode;
	}

	/**
	 * Detect special case: maybeInterleaved(sequence(a, b, c, *d, e),
	 * sequence(d, e, * a, b, c))
	 * 
	 * @return
	 */
	private static boolean checkSplitPoint(Seq seqA, Seq seqB, int splitPointA) {
		int splitPointB = seqA.getChildren().size() - splitPointA;

		//compare the part before the split in A
		for (int i = 0; i < splitPointA; i++) {
			DfgUnfoldedNode Ai = FootPrint.makeDfg(new UnfoldedNode(seqA.getChildren().get(i)));
			DfgUnfoldedNode Bi = FootPrint.makeDfg(new UnfoldedNode(seqB.getChildren().get(splitPointB + i)));
			if (!Ai.equals(Bi)) {
				return false;
			}
		}

		//compare the part before the split in B
		for (int i = 0; i < splitPointB; i++) {
			DfgUnfoldedNode Ai = FootPrint.makeDfg(new UnfoldedNode(seqA.getChildren().get(splitPointA + i)));
			DfgUnfoldedNode Bi = FootPrint.makeDfg(new UnfoldedNode(seqB.getChildren().get(i)));
			if (!Ai.equals(Bi)) {
				return false;
			}
		}

		return true;
	}
}