package org.processmining.plugins.InductiveMiner.mining.cuts.IMc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;


public class SATResult {
	private final Cut cut;
	private final double probability;

	public SATResult(Set<XEventClass> sigma1, Set<XEventClass> sigma2, double probability, Operator operator) {
		if (sigma1 == null || sigma1 == null) {
			cut = null;
		} else {
			Collection<Set<XEventClass>> partition = new ArrayList<Set<XEventClass>>();
			partition.add(sigma1);
			partition.add(sigma2);
			cut = new Cut(operator, partition);
		}
		this.probability = probability;
	}
	
	public SATResult(SATResult copyFrom) {
		this.cut = copyFrom.cut;
		this.probability = copyFrom.probability;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("probability " + probability);
		if (cut != null && cut.isValid()) {
			s.append(", " + cut);
		}
		return s.toString();
	}
	
	public Cut getCut() {
		return cut;
	}
	
	public double getProbability() {
		return probability;
	}
}
