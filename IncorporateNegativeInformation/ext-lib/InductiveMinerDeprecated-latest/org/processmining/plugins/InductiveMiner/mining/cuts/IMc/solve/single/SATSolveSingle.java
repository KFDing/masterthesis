package org.processmining.plugins.InductiveMiner.mining.cuts.IMc.solve.single;

import gnu.trove.map.hash.THashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.CutFinderIMinInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMc.SATResult;
import org.sat4j.core.VecInt;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;

public abstract class SATSolveSingle {

	protected static abstract class Var {
		private final int varInt;
		private boolean result;

		public Var(int varInt) {
			this.varInt = varInt;
		}

		public int getVarInt() {
			return varInt;
		}

		@Override
		public abstract String toString();

		public boolean isResult() {
			return result;
		}

		public void setResult(boolean result) {
			this.result = result;
		}
	}

	protected static class Node extends Var {
		private final XEventClass activity;

		public Node(int varInt, XEventClass activity) {
			super(varInt);
			this.activity = activity;
		}

		public XEventClass getActivity() {
			return activity;
		}

		public String toString() {
			return activity.toString();
		}
	}

	protected static class Edge extends Var {
		private final XEventClass from;
		private final XEventClass to;

		public Edge(int varInt, XEventClass from, XEventClass to) {
			super(varInt);
			this.from = from;
			this.to = to;
		}

		public String toString() {
			return from.toString() + "->" + to.toString();
		}

	}

	protected Map<Integer, Var> varInt2var;
	protected IPBSolver solver;
	protected Map<XEventClass, Node> node2var;
	protected XEventClass[] nodes;
	protected int countNodes;
	
	private int varCounter;

	protected final CutFinderIMinInfo info;

	public SATSolveSingle(CutFinderIMinInfo info) {
		varInt2var = new THashMap<Integer, Var>();
		varCounter = 1;
		solver = SolverFactory.newDefaultOptimizer();
		this.info = info;

		countNodes = info.getActivities().size();

		//initialise nodes
		node2var = new THashMap<XEventClass, Node>();
		for (XEventClass a : info.getActivities()) {
			node2var.put(a, newNodeVar(a));
		}

		//make a list of nodes instead of the set
		nodes = new XEventClass[countNodes];
		{
			int i = 0;
			for (XEventClass a : info.getActivities()) {
				nodes[i] = a;
				i++;
			}
		}
	}

	public abstract SATResult solveSingle(int cutSize, double bestAverageTillNow);
	
	protected Pair<Set<XEventClass>, Set<XEventClass>> compute() throws TimeoutException {
		IProblem problem = solver;
		if (problem.isSatisfiable()) {

			//process resulting model
			int[] model = problem.model();
			for (int i = 0; i < model.length; i++) {
				int x = model[i];
				varInt2var.get(Math.abs(x)).setResult(x > 0);
			}

			//debug nodes
			Set<XEventClass> sigma1 = new HashSet<XEventClass>();
			Set<XEventClass> sigma2 = new HashSet<XEventClass>();
			for (XEventClass a : node2var.keySet()) {
				if (node2var.get(a).isResult()) {
					sigma1.add(a);
				} else {
					sigma2.add(a);
				}
			}
			solver.clearLearntClauses();
			solver.reset();
			//debug("  solution: " + sigma1 + ", " + sigma2);
			return new Pair<Set<XEventClass>, Set<XEventClass>>(sigma1, sigma2);
		} else {
			//debug("  no solution");
		}
		return null;
	}
	
	protected Edge newEdgeVar(XEventClass a, XEventClass b) {
		Edge var = new Edge(varCounter, a, b);
		varInt2var.put(varCounter, var);
		varCounter++;
		return var;
	}
	
	protected Node newNodeVar(XEventClass a) {
		Node var = new Node(varCounter, a);
		varInt2var.put(varCounter, var);
		varCounter++;
		return var;
	}
	
	protected void debug(String x) {
		if (info.isDebug()) {
			System.out.println(x);
		}
	}
	
	protected void addClause(int... ints) throws ContradictionException {
		solver.addClause(new VecInt(ints));
	}
}
