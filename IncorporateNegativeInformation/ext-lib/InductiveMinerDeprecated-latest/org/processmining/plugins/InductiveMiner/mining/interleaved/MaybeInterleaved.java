package org.processmining.plugins.InductiveMiner.mining.interleaved;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

public class MaybeInterleaved extends AbstractBlock.Xor {
	public MaybeInterleaved(String name) {
		super(name);
	}

	public MaybeInterleaved(UUID id, String name) {
		super(id, name);
	}

	public MaybeInterleaved(String name, List<Edge> incoming, List<Edge> outgoing) {
		super(name, incoming, outgoing);
	}

	public MaybeInterleaved(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
		super(id, name, incoming, outgoing);
	}

	public MaybeInterleaved(Block.Xor b) {
		super(b);
	}
	
	@Override
	public String toStringShort() {
		return "?In";
	}
}
