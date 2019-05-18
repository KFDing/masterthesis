package org.processmining.plugins.InductiveMiner.mining.interleaved;

import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.set.hash.THashSet;

public class FootPrint {

	public static class DfgUnfoldedNode {
		final Graph<String> directlyFollowsGraph;
		final Graph<String> concurrencyGraph;
		final MultiSet<String> startActivities;
		final MultiSet<String> endActivities;
		boolean allowsEmptyTrace;

		public boolean equals(DfgUnfoldedNode other) {
			if (allowsEmptyTrace != other.allowsEmptyTrace) {
				return false;
			}

			if (!startActivities.toSet().equals(other.startActivities.toSet())
					|| !endActivities.toSet().equals(other.endActivities.toSet())) {
				return false;
			}

			if (!(new THashSet<String>(Arrays.asList(directlyFollowsGraph.getVertices())).equals(new THashSet<String>(
					Arrays.asList(other.directlyFollowsGraph.getVertices()))))) {
				return false;
			}

			//compare directly follows graph activities
			compareActivities(directlyFollowsGraph.getVertices(), other.directlyFollowsGraph.getVertices());

			//compare directly follows graphs
			for (long e1 : directlyFollowsGraph.getEdges()) {
				if (!other.directlyFollowsGraph.containsEdge(directlyFollowsGraph.getEdgeSource(e1),
						directlyFollowsGraph.getEdgeTarget(e1))) {
					return false;
				}
			}
			for (long e1 : other.directlyFollowsGraph.getEdges()) {
				if (!directlyFollowsGraph.containsEdge(other.directlyFollowsGraph.getEdgeSource(e1),
						other.directlyFollowsGraph.getEdgeTarget(e1))) {
					return false;
				}
			}

			//compare concurrency graphs
			compareActivities(concurrencyGraph.getVertices(), other.concurrencyGraph.getVertices());
			for (long e1 : concurrencyGraph.getEdges()) {
				if (!other.concurrencyGraph.containsEdge(concurrencyGraph.getEdgeSource(e1),
						concurrencyGraph.getEdgeTarget(e1))) {
					return false;
				}
			}
			for (long e1 : other.concurrencyGraph.getEdges()) {
				if (!concurrencyGraph.containsEdge(other.concurrencyGraph.getEdgeSource(e1),
						other.concurrencyGraph.getEdgeTarget(e1))) {
					return false;
				}
			}

			return true;
		}

		public DfgUnfoldedNode() {
			directlyFollowsGraph = GraphFactory.create(String.class, 1);
			concurrencyGraph = GraphFactory.create(String.class, 1);
			startActivities = new MultiSet<>();
			endActivities = new MultiSet<>();
		}

		public void absorb(Graph<String> otherDfg, Graph<String> otherConcurrency) {
			directlyFollowsGraph.addVertices(otherDfg.getVertices());
			for (long e : otherDfg.getEdges()) {
				directlyFollowsGraph.addEdge(otherDfg.getEdgeSource(e), otherDfg.getEdgeTarget(e), 1);
			}

			concurrencyGraph.addVertices(otherConcurrency.getVertices());
			for (long e : otherConcurrency.getEdges()) {
				concurrencyGraph.addEdge(otherConcurrency.getEdgeSource(e), otherConcurrency.getEdgeTarget(e), 1);
			}
		}
	}

	public static DfgUnfoldedNode makeDfg(UnfoldedNode unode) {
		if (unode.getNode() instanceof Manual) {
			return makeDfgActivity(unode);
		} else if (unode.getNode() instanceof Automatic) {
			return makeDfgTau(unode);
		} else if (unode.getNode() instanceof Xor || unode.getNode() instanceof Def) {
			return makeDfgXor(unode);
		} else if (unode.getNode() instanceof Seq) {
			return makeDfgSeq(unode);
		} else if (unode.getNode() instanceof Interleaved) {
			return makeDfgInterleaved(unode);
		} else if (unode.getNode() instanceof And) {
			return makeDfgAnd(unode);
		} else if (unode.getNode() instanceof XorLoop || unode.getNode() instanceof DefLoop) {
			return makeDfgLoop(unode);
		}
		return null;
	}

	private static DfgUnfoldedNode makeDfgActivity(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Manual);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.directlyFollowsGraph.addVertex(unode.getNode().getName());
		result.concurrencyGraph.addVertex(unode.getNode().getName());
		result.startActivities.add(unode.getNode().getName());
		result.endActivities.add(unode.getNode().getName());
		result.allowsEmptyTrace = false;
		return result;
	}

	private static DfgUnfoldedNode makeDfgTau(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Automatic);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = true;
		return result;
	}

	private static DfgUnfoldedNode makeDfgXor(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Def || unode.getNode() instanceof Xor);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = false;

		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);

			DfgUnfoldedNode subResult = makeDfg(uChild);

			result.allowsEmptyTrace = result.allowsEmptyTrace || subResult.allowsEmptyTrace;

			result.absorb(subResult.directlyFollowsGraph, subResult.concurrencyGraph);
			result.startActivities.addAll(subResult.startActivities);
			result.endActivities.addAll(subResult.endActivities);
		}
		return result;
	}

	private static DfgUnfoldedNode makeDfgSeq(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Seq);
		DfgUnfoldedNode result = new DfgUnfoldedNode();
		result.allowsEmptyTrace = true;
		int i = 0;
		MultiSet<String> childEndActivities = new MultiSet<>();
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);
			DfgUnfoldedNode subResult = makeDfg(uChild);
			result.allowsEmptyTrace = result.allowsEmptyTrace && subResult.allowsEmptyTrace;

			//copy the dfg
			result.absorb(subResult.directlyFollowsGraph, subResult.concurrencyGraph);

			//make the connections between the last child and this child (the multiset takes care of taus)
			for (String from : childEndActivities) {
				for (String to : subResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}

			//if this child can yield the empty trace, keep the child's start activities
			if (!subResult.allowsEmptyTrace) {
				childEndActivities = new MultiSet<>();
			}
			childEndActivities.addAll(subResult.endActivities);

			//if this is the first child, copy the start activities
			if (i == 0) {
				result.startActivities.addAll(subResult.startActivities);
			}

			//if this is the last child, copy the end activities
			if (i == unode.getBlock().getChildren().size() - 1) {
				result.endActivities.addAll(subResult.endActivities);
			}

			i++;
		}

		return result;
	}

	private static DfgUnfoldedNode makeDfgInterleaved(UnfoldedNode unode) {
		assert (unode.getNode() instanceof Interleaved);
		DfgUnfoldedNode result = new DfgUnfoldedNode();

		result.allowsEmptyTrace = true;
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);
			DfgUnfoldedNode subResult = makeDfg(uChild);
			result.allowsEmptyTrace = result.allowsEmptyTrace && subResult.allowsEmptyTrace;

			//make all intra-child connections
			for (String from : result.endActivities) {
				for (String to : subResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}
			for (String from : subResult.endActivities) {
				for (String to : result.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}

			//copy the dfg
			result.absorb(subResult.directlyFollowsGraph, subResult.concurrencyGraph);
			result.startActivities.addAll(subResult.startActivities);
			result.endActivities.addAll(subResult.endActivities);
		}
		return result;
	}

	private static DfgUnfoldedNode makeDfgAnd(UnfoldedNode unode) {
		assert (unode.getNode() instanceof And);
		DfgUnfoldedNode result = new DfgUnfoldedNode();

		result.allowsEmptyTrace = true;
		for (Node child : unode.getBlock().getChildren()) {
			UnfoldedNode uChild = unode.unfoldChild(child);
			DfgUnfoldedNode subResult = makeDfg(uChild);
			result.allowsEmptyTrace = result.allowsEmptyTrace && subResult.allowsEmptyTrace;

			//make all intra-child connections
			for (String from : result.directlyFollowsGraph.getVertices()) {
				for (String to : subResult.directlyFollowsGraph.getVertices()) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
					result.directlyFollowsGraph.addEdge(to, from, 1);
					result.concurrencyGraph.addEdge(from, to, 1);
					result.concurrencyGraph.addEdge(to, from, 1);
				}
			}

			//copy the dfg
			result.absorb(subResult.directlyFollowsGraph, subResult.concurrencyGraph);
			result.startActivities.addAll(subResult.startActivities);
			result.endActivities.addAll(subResult.endActivities);
		}

		return result;
	}

	private static DfgUnfoldedNode makeDfgLoop(UnfoldedNode unode) {
		assert (unode.getNode() instanceof XorLoop || unode.getNode() instanceof DefLoop);
		DfgUnfoldedNode result = new DfgUnfoldedNode();

		//process the first child
		UnfoldedNode body = unode.unfoldChild(unode.getBlock().getChildren().get(0));
		DfgUnfoldedNode bodyResult = makeDfg(body);
		result.allowsEmptyTrace = bodyResult.allowsEmptyTrace;
		result.startActivities.addAll(bodyResult.startActivities);
		result.endActivities.addAll(bodyResult.endActivities);
		result.absorb(bodyResult.directlyFollowsGraph, bodyResult.concurrencyGraph);

		//process the redo child
		UnfoldedNode redo = unode.unfoldChild(unode.getBlock().getChildren().get(1));
		DfgUnfoldedNode redoResult = makeDfg(redo);
		result.absorb(redoResult.directlyFollowsGraph, redoResult.concurrencyGraph);

		//connect to the body
		for (String from : bodyResult.endActivities) {
			for (String to : redoResult.startActivities) {
				result.directlyFollowsGraph.addEdge(from, to, 1);
			}
		}
		for (String from : redoResult.endActivities) {
			for (String to : bodyResult.startActivities) {
				result.directlyFollowsGraph.addEdge(from, to, 1);
			}
		}

		//if the body allows for the empty trace, the start/end activities of the redo children are global
		if (bodyResult.allowsEmptyTrace) {
			result.startActivities.addAll(redoResult.startActivities);
			result.endActivities.addAll(redoResult.endActivities);

			//moreover, connect all redo end to start
			for (String from : redoResult.endActivities) {
				for (String to : redoResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}
		}

		//if one redo allows for the empty trace,
		if (redoResult.allowsEmptyTrace) {
			//connect all body ends to starts
			for (String from : bodyResult.endActivities) {
				for (String to : bodyResult.startActivities) {
					result.directlyFollowsGraph.addEdge(from, to, 1);
				}
			}
		}

		return result;
	}

	public static boolean compareActivities(String[] a, String[] b) {
		for (String activity : a) {
			boolean found = false;
			for (String otherActivity : b) {
				if (activity.equals(otherActivity)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		for (String otherActivity : a) {
			boolean found = false;
			for (String activity : b) {
				if (activity.equals(otherActivity)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
