package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;



public class DistanceEuclidian {

	private DistanceEuclidian() {
		
	}
	
	public static int computeDistance(UpToKSuccessorMatrix A, UpToKSuccessorMatrix B) {

		if (!A.getActivities().equals(B.getActivities())) {
			return -1;
		}

		int sum = 0;

		//normal cells
		for (String a1 : A.getActivities()) {
			for (String a2 : A.getActivities()) {
				if (A.getKSuccessor(a1, a2) != null && B.getKSuccessor(a1, a2) != null) {
					sum += Math.pow(A.getKSuccessor(a1, a2) - B.getKSuccessor(a1, a2), 2);
				} else if (A.getKSuccessor(a1, a2) != B.getKSuccessor(a1, a2)) {
					sum += 10;
				}
			}
		}
		
		//begin, end cells
		for (String a : A.getActivities()) {
			sum += Math.pow(A.getKSuccessor(null, a) - B.getKSuccessor(null, a), 2);
			sum += Math.pow(A.getKSuccessor(a, null) - B.getKSuccessor(a, null), 2);
		}
		
		//begin end
		sum += Math.pow(A.getKSuccessor(null, null) - B.getKSuccessor(null, null), 2);
		
		return sum;

	}

}
