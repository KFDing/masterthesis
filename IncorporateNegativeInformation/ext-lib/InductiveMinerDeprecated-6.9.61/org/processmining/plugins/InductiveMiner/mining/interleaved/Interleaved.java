package org.processmining.plugins.InductiveMiner.mining.interleaved;

import java.util.List;
import java.util.UUID;

import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.impl.AbstractBlock;

public class Interleaved extends AbstractBlock.And {
	public Interleaved(String name) {
		super(name);
	}

	public Interleaved(UUID id, String name) {
		super(id, name);
	}

	public Interleaved(String name, List<Edge> incoming, List<Edge> outgoing) {
		super(name, incoming, outgoing);
	}

	public Interleaved(UUID id, String name, List<Edge> incoming, List<Edge> outgoing) {
		super(id, name, incoming, outgoing);
	}

	public Interleaved(Block.And b) {
		super(b);
	}
	
	@Override
	public String toStringShort() {
		return "Int";
	}
}
