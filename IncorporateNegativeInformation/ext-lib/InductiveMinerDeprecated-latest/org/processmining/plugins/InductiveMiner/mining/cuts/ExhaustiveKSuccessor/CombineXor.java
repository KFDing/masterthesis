package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import org.processmining.plugins.InductiveMiner.Sets;

public class CombineXor {

	private CombineXor() {

	}

	public static UpToKSuccessorMatrix combine(UpToKSuccessorMatrix A, UpToKSuccessorMatrix B) {

		UpToKSuccessorMatrix C = new UpToKSuccessorMatrix(Sets.union(A.getActivities(), B.getActivities()));

		//S-a and a-E are copied
		for (String a : A.getActivities()) {
			C.feedKSuccessor(null, a, A.getKSuccessor(null, a));
			C.feedKSuccessor(a, null, A.getKSuccessor(a, null));
		}

		//S-b and b-E are copied
		for (String b : B.getActivities()) {
			C.feedKSuccessor(null, b, B.getKSuccessor(null, b));
			C.feedKSuccessor(b, null, B.getKSuccessor(b, null));
		}

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

		//S -> E is the minimum of the two
		C.feedKSuccessor(null, null, min(A.getKSuccessor(null, null), B.getKSuccessor(null, null)));

		return C;

	}

	private static Integer min(Integer a, Integer b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		} else if (a < b) {
			return a;
		} else {
			return b;
		}
	}

}
