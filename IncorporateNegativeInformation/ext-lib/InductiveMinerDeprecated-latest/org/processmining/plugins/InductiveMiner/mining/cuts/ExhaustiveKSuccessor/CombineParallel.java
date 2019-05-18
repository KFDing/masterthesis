package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import org.processmining.plugins.InductiveMiner.Sets;


public class CombineParallel {

	private CombineParallel() {
		
	}
	
	public static UpToKSuccessorMatrix combine(UpToKSuccessorMatrix A, UpToKSuccessorMatrix B) {
		
		UpToKSuccessorMatrix C = new UpToKSuccessorMatrix(Sets.union(A.getActivities(), B.getActivities()));
		
		C.feedKSuccessor(null, null, A.getKSuccessor(null, null) + B.getKSuccessor(null, null) - 1);
		
		//inter-cells are all 1
		for (String a : A.getActivities()) {
			for (String b : B.getActivities()) {
				C.feedKSuccessor(a, b, 1);
				C.feedKSuccessor(b, a, 1);
			}
		}
		
		// S to .. and .. to E are copied
		for (String a : A.getActivities()) {
			C.feedKSuccessor(null, a, A.getKSuccessor(null, a));
			C.feedKSuccessor(a, null, A.getKSuccessor(a, null));
		}
		for (String b : B.getActivities()) {
			C.feedKSuccessor(null, b, B.getKSuccessor(null, b));
			C.feedKSuccessor(b, null, B.getKSuccessor(b, null));
		}
		
		//intra-cells are copied
		for (String a1 : A.getActivities()) {
			for (String a2 : A.getActivities()) {
				C.feedKSuccessor(a1, a2, A.getKSuccessor(a1, a2));
			}
		}
		for (String b1 : B.getActivities()) {
			for (String b2 : B.getActivities()) {
				C.feedKSuccessor(b1, b2, B.getKSuccessor(b1, b2));
			}
		}
		
		return C;
		
	}

}
