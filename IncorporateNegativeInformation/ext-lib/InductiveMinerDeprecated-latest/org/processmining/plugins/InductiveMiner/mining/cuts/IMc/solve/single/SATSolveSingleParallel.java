package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single;

import gnu.trove.map.hash.THashMap;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.SATResult;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.probabilities.Probabilities;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.TimeoutException;

public class SATSolveSingleParallel extends SATSolveSingle {

	public SATSolveSingleParallel(CutFinderIMinInfo info) {
		super(info);
	}

	public SATResult solveSingle(int cutSize, double bestAverageTillNow) {
		//debug(" solve parallel with cut size " + cutSize + " and probability " + bestAverageTillNow);

		Graph<XEventClass> graph = info.getGraph();
		Probabilities probabilities = info.getProbabilities();

		//compute number of edges in the cut
		int numberOfEdgesInCut = (countNodes - cutSize) * cutSize;

		//edges
		Map<Pair<XEventClass, XEventClass>, Edge> edge2var = new THashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = i + 1; j < countNodes; j++) {
				XEventClass aI = nodes[i];
				XEventClass aJ = nodes[j];
				edge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
			}
		}

		try {
			//constraint: exactly cutSize nodes are cut
			{
				int[] clause = new int[countNodes];
				int i = 0;
				for (XEventClass a : graph.getVertices()) {
					clause[i] = node2var.get(a).getVarInt();
					i++;
				}
				solver.addExactly(new VecInt(clause), cutSize);
			}

			//constraint: edge is cut iff between two nodes on different sides of the cut
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					int A = node2var.get(aI).getVarInt();
					int B = node2var.get(aJ).getVarInt();
					int C = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

					addClause( A, B, -C);
					addClause(A, -B, C);
					addClause(-A, B, C);
					addClause(-A, -B, -C);
				}
			}

			//constraint: cut side has a start activity
			{
				int[] clause = new int[info.getDfg().getNumberOfStartActivitiesAsSet()];
				int i = 0;
				for (XEventClass e : info.getDfg().getStartActivities()) {
					clause[i] = node2var.get(e).getVarInt();
					i++;
				}
				solver.addAtLeast(new VecInt(clause), 1);
			}

			//constraint: cut side has an end activity
			{
				int[] clause = new int[info.getDfg().getNumberOfEndActivitiesAsSet()];
				int i = 0;
				for (XEventClass e : info.getDfg().getEndActivities()) {
					clause[i] = node2var.get(e).getVarInt();
					i++;
				}
				solver.addAtLeast(new VecInt(clause), 1);
			}

			//constraint: -cut side has a start activity
			{
				int[] clause = new int[info.getDfg().getNumberOfStartActivitiesAsSet()];
				int i = 0;
				for (XEventClass e : info.getDfg().getStartActivities()) {
					clause[i] = -node2var.get(e).getVarInt();
					i++;
				}
				solver.addAtLeast(new VecInt(clause), 1);
			}

			//constraint: cut side has an end activity
			{
				int[] clause = new int[info.getDfg().getNumberOfEndActivitiesAsSet()];
				int i = 0;
				for (XEventClass e : info.getDfg().getEndActivities()) {
					clause[i] = -node2var.get(e).getVarInt();
					i++;
				}
				solver.addAtLeast(new VecInt(clause), 1);
			}

			//objective function: least cost for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					clause.push(edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
					coefficients.push(probabilities.getProbabilityParallelB(info, aI, aJ).negate());
				}
			}
			ObjectiveFunction obj = new ObjectiveFunction(clause, coefficients);
			solver.setObjectiveFunction(obj);

			//constraint: better than best previous run
			BigInteger minObjectiveFunction = BigInteger.valueOf((long) (probabilities.doubleToIntFactor
					* bestAverageTillNow * numberOfEdgesInCut));
			solver.addAtMost(clause, coefficients, minObjectiveFunction.negate());

			//compute result
			Pair<Set<XEventClass>, Set<XEventClass>> result = compute();
			if (result != null) {

				//compute cost of cut
				//String x = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = i + 1; j < countNodes; j++) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];
						Edge e = edge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
						if (e.isResult()) {
							//x += e.toString() + " (" + probabilities.getProbabilityParallel(logInfo, aI, aJ) + "), ";
							sumProbability += probabilities.getProbabilityParallel(info, aI, aJ);
						}
					}
				}

				double averageProbability = sumProbability / numberOfEdgesInCut;
				SATResult result2 = new SATResult(result.getLeft(), result.getRight(), averageProbability, Operator.concurrent);

				debug("  " + result2.toString());
				//debug("   edges " + x);
				//debug("   sum probability " + sumProbability);

				return result2;
			} else {
				debug("  no solution");
			}
		} catch (ContradictionException e) {
			debug("  inconsistent problem");
		} catch (TimeoutException e) {
			debug("  timeout");
		}

		return null;
	}

}
