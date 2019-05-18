package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single;

import gnu.trove.map.hash.THashMap;

import java.math.BigInteger;
import java.util.HashMap;
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

public class SATSolveSingleLoop extends SATSolveSingle {

	public SATSolveSingleLoop(CutFinderIMinInfo info) {
		super(info);
	}

	public SATResult solveSingle(int cutSize, double bestAverageTillNow) {
		//debug(" solve loop with cut size " + cutSize + " and probability " + bestAverageTillNow);

		Graph<XEventClass> graph = info.getGraph();
		Probabilities probabilities = info.getProbabilities();

		//compute number of edges in the cut
		int numberOfEdgesInCut = (countNodes - cutSize) * cutSize;

		//initialise startA, endA, startB, endB
		Map<XEventClass, Node> startBody = new THashMap<XEventClass, Node>();
		Map<XEventClass, Node> endBody = new THashMap<XEventClass, Node>();
		Map<XEventClass, Node> startRedo = new THashMap<XEventClass, Node>();
		Map<XEventClass, Node> endRedo = new THashMap<XEventClass, Node>();
		for (XEventClass a : nodes) {
			startBody.put(a, newNodeVar(a));
			endBody.put(a, newNodeVar(a));
			startRedo.put(a, newNodeVar(a));
			endRedo.put(a, newNodeVar(a));
		}

		//edges
		Map<Pair<XEventClass, XEventClass>, Edge> singleLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		Map<Pair<XEventClass, XEventClass>, Edge> indirectLoopEdge2var = new HashMap<Pair<XEventClass, XEventClass>, Edge>();
		for (int i = 0; i < countNodes; i++) {
			for (int j = i + 1; j < countNodes; j++) {
				XEventClass aI = nodes[i];
				XEventClass aJ = nodes[j];
				singleLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
				singleLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aJ, aI), newEdgeVar(aI, aJ));
				indirectLoopEdge2var.put(new Pair<XEventClass, XEventClass>(aI, aJ), newEdgeVar(aI, aJ));
			}
		}

		try {
			//constraint: exactly cutSize nodes are cut
			{
				int[] clause = new int[countNodes];
				int k = 0;
				for (int i = 0; i < countNodes; i++) {
					XEventClass aI = nodes[i];
					clause[k] = node2var.get(aI).getVarInt();
					k++;
				}
				solver.addExactly(new VecInt(clause), cutSize);
			}

			//constraint: each edge is in at most one category
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					int S1 = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
					int S2 = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();
					int N = indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

					int[] clause = { S1, S2, N };
					solver.addAtMost(new VecInt(clause), 1);
				}
			}

			//constraint: (cut(a) <=> cut(b)) <=> (-indirect(a, b) and -single(a, b) and -single(b, a))
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];

					int A = node2var.get(aI).getVarInt();
					int B = node2var.get(aJ).getVarInt();
					int C = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();
					int D = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt();
					int E = indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt();

					addClause(A, B, -C);
					addClause(A, B, -D);
					addClause(A, B, -E);

					addClause(-A, -B, -C);
					addClause(-A, -B, -D);
					addClause(-A, -B, -E);

					addClause(-A, B, C, D, E);
					addClause(A, -B, C, D, E);
				}
			}

			//constraint: single(a, b) <=> (endBody(a) and startRedo(b)) or (endRedo(a) and startBody(b))
			for (XEventClass a : nodes) {
				for (XEventClass b : nodes) {
					if (a != b) {
						int A = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(a, b)).getVarInt();

						int B = endBody.get(a).getVarInt();
						int C = startRedo.get(b).getVarInt();
						int D = endRedo.get(a).getVarInt();
						int E = startBody.get(b).getVarInt();

						addClause(-A, B, D);
						addClause(-A, B, E);
						addClause(-A, C, D);
						addClause(-A, C, E);

						addClause(A, -B, -C);
						addClause(A, -D, -E);
					}
				}
			}

			//constraint: |startRedo|, |endRedo| > 0
			{
				int[] clauseStartRedo = new int[nodes.length];
				int[] clauseEndRedo = new int[nodes.length];
				for (int i = 0; i < nodes.length; i++) {
					clauseStartRedo[i] = startRedo.get(nodes[i]).getVarInt();
					clauseEndRedo[i] = endRedo.get(nodes[i]).getVarInt();
				}
				solver.addAtLeast(new VecInt(clauseStartRedo), 1);
				solver.addAtLeast(new VecInt(clauseEndRedo), 1);
			}

			//constraint: startB(a) or endB(a) => -cut(a)
			for (XEventClass a : graph.getVertices()) {
				int A = node2var.get(a).getVarInt();
				int B = startRedo.get(a).getVarInt();
				int C = endRedo.get(a).getVarInt();

				addClause(-A, -B);
				addClause(-A, -C);
			}

			//constraint: Start(a) <=> startBody(a)
			//constraint: Start(a) => cut(a)
			for (XEventClass a : nodes) {
				int A = startBody.get(a).getVarInt();
				if (info.getDfg().isStartActivity(a)) {
					addClause(A);

					int B = node2var.get(a).getVarInt();
					addClause(B);
				} else {
					addClause(-A);
				}
			}

			//constraint: End(a) <=> endBody(a)
			//constraint: End(a) => cut(a)
			for (XEventClass a : nodes) {
				int A = endBody.get(a).getVarInt();
				if (info.getDfg().isEndActivity(a)) {
					addClause(A);

					int B = node2var.get(a).getVarInt();
					addClause(B);
				} else {
					addClause(-A);
				}
			}

			//objective function: highest probabilities for edges
			VecInt clause = new VecInt();
			IVec<BigInteger> coefficients = new Vec<BigInteger>();
			for (int i = 0; i < countNodes; i++) {
				for (int j = i + 1; j < countNodes; j++) {
					XEventClass aI = nodes[i];
					XEventClass aJ = nodes[j];
					//direct
					clause.push(singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
					BigInteger pab = probabilities.getProbabilityLoopSingleB(info, aI, aJ);
					coefficients.push(pab.multiply(BigInteger.valueOf(2)).negate());
					
					//direct
					clause.push(singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI)).getVarInt());
					BigInteger pab2 = probabilities.getProbabilityLoopSingleB(info, aJ, aI);
					coefficients.push(pab2.multiply(BigInteger.valueOf(2)).negate());

					//indirect
					clause.push(indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ)).getVarInt());
					BigInteger ind = probabilities.getProbabilityLoopIndirectB(info, aI, aJ);
					coefficients.push(ind.negate());
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
				String single = "";
				String indirect = "";
				double sumProbability = 0;
				for (int i = 0; i < countNodes; i++) {
					for (int j = i + 1; j < countNodes; j++) {
						XEventClass aI = nodes[i];
						XEventClass aJ = nodes[j];

						//single edge
						{
							Edge e = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
							if (e.isResult()) {
								double p = probabilities.getProbabilityLoopSingle(info, aI, aJ);
								single += e.toString() + " (" + p + "), ";
								sumProbability += p;
							}
						}

						//single edge
						{
							Edge e = singleLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aJ, aI));
							if (e.isResult()) {
								double p = probabilities.getProbabilityLoopSingle(info, aJ, aI);
								single += e.toString() + " (" + p + "), ";
								sumProbability += p;
							}
						}

						//indirect edge
						Edge se = indirectLoopEdge2var.get(new Pair<XEventClass, XEventClass>(aI, aJ));
						if (se.isResult()) {
							double p = probabilities.getProbabilityLoopIndirect(info, aI, aJ);
							indirect += se.toString() + " (" + p + "), ";
							sumProbability += p;
						}
					}
				}

				double averageProbability = sumProbability / numberOfEdgesInCut;
				SATResult result2 = new SATResult(result.getLeft(), result.getRight(), averageProbability, Operator.loop);

				//debug
				String sa = "";
				String ea = "";
				String sb = "";
				String eb = "";
				for (XEventClass e : graph.getVertices()) {
					if (startBody.get(e).isResult()) {
						sa += e.toString() + ", ";
					}
					if (endBody.get(e).isResult()) {
						ea += e.toString() + ", ";
					}
					if (startRedo.get(e).isResult()) {
						sb += e.toString() + ", ";
					}
					if (endRedo.get(e).isResult()) {
						eb += e.toString() + ", ";
					}
				}

				debug("  " + result2.toString());
				debug("   single edges " + single);
				debug("   indirect edges " + indirect);
				debug("   start body " + sa);
				debug("   end body " + ea);
				debug("   start redo " + sb);
				debug("   end redo " + eb);
				debug("   sum probability " + sumProbability);

				return result2;
			} else {
				debug("  no solution");
			}
		} catch (TimeoutException e) {
			debug("  timeout");
		} catch (ContradictionException e) {
			debug("  inconsistent problem");
		}
		return null;
	}

}
