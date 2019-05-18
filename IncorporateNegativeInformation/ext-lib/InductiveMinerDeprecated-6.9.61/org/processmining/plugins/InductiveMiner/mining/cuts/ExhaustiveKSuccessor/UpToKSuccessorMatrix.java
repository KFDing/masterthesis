package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import java.util.Set;

import org.processmining.plugins.InductiveMiner.Matrix;
import org.processmining.plugins.InductiveMiner.Sets;

public class UpToKSuccessorMatrix {
	private Matrix<String, Integer> matrix;
	private final Set<String> activities;

	public UpToKSuccessorMatrix(Set<String> activities) {
		this.activities = activities;
		matrix = new Matrix<String, Integer>(Sets.toArray(String.class, activities), true);
	}

	public Integer getKSuccessor(String from, String to) {
		return matrix.get(from, to);
	}

	public void feedKSuccessor(String from, String to, Integer newValue) {
		Integer old = matrix.get(from, to);
		
		if (old == null || newValue < old) {
			matrix.set(from, to, newValue);
		}
	}

	public Set<String> getActivities() {
		return activities;
	}

	public String toString() {
		return matrix.toString();
	}

	public String toString(boolean useHTML) {
		return matrix.toString(useHTML);
	}
}
