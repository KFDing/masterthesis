package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import org.processmining.plugins.InductiveMiner.Sets;

public class CombineSequence {

	private CombineSequence() {

	}

	public static UpToKSuccessorMatrix combine(UpToKSuccessorMatrix A, UpToKSuccessorMatrix B) {

		UpToKSuccessorMatrix C = new UpToKSuccessorMatrix(Sets.union(A.getActivities(), B.getActivities()));

		//a -> a are copied
		for (String a1 : A.getActivities()) {
			for (String a2 : A.getActivities()) {
				C.feedKSuccessor(a1, a2, A.getKSuccessor(a1, a2));
				C.feedKSuccessor(a2, a1, A.getKSuccessor(a2, a1));
			}
		}

		//b -> b are copied
		for (String b1 : B.getActivities()) {
			for (String b2 : B.getActivities()) {
				C.feedKSuccessor(b1, b2, B.getKSuccessor(b1, b2));
				C.feedKSuccessor(b2, b1, B.getKSuccessor(b2, b1));
			}
		}

		//S-a are copied
		for (String a : A.getActivities()) {
			C.feedKSuccessor(null, a, A.getKSuccessor(null, a));
		}

		//a-E go through B
		for (String a : A.getActivities()) {
			C.feedKSuccessor(a, null, A.getKSuccessor(a, null) + B.getKSuccessor(null, null) - 1);
		}

		//b-E are copied
		for (String b : B.getActivities()) {
			C.feedKSuccessor(b, null, B.getKSuccessor(b, null));
		}

		//S-b go through A
		for (String b : B.getActivities()) {
			C.feedKSuccessor(null, b, A.getKSuccessor(null, null) + B.getKSuccessor(null, b) - 1);
		}

		//a -> b are summed
		for (String a : A.getActivities()) {
			for (String b : B.getActivities()) {
				C.feedKSuccessor(a, b, A.getKSuccessor(a, null) + B.getKSuccessor(null, b) - 1);
			}
		}

		//S -> E is the sum of the two
		C.feedKSuccessor(null, null, A.getKSuccessor(null, null) + B.getKSuccessor(null, null) - 1);

		return C;

	}
}
