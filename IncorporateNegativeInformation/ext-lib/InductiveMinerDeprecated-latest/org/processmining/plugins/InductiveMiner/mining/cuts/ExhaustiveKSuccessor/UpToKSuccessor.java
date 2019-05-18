package org.processmining.plugins.InductiveMiner.mining.cuts.ExhaustiveKSuccessor;

import gnu.trove.map.hash.THashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.Task;
import org.processmining.processtree.Task.Manual;

public class UpToKSuccessor {

	private UpToKSuccessor() {

	}

	public static UpToKSuccessorMatrix fromLog(IMLog log, IMLogInfo logInfo) {
		
		//make a list of names of event classes
		Set<String> h = new HashSet<String>();
		for (XEventClass a : logInfo.getActivities()) {
			h.add(toString(a));
		}
		UpToKSuccessorMatrix kSuccessors = new UpToKSuccessorMatrix(h);

		//walk trough the log
		Map<XEventClass, Integer> eventSeenAt;

		for (IMTrace trace : log) {

			int pos = 0;
			eventSeenAt = new THashMap<XEventClass, Integer>();

			for (XEvent e : trace) {
				XEventClass currentEvent = log.classify(trace, e);

				for (XEventClass seen : eventSeenAt.keySet()) {
					kSuccessors.feedKSuccessor(toString(seen), toString(currentEvent), pos - eventSeenAt.get(seen));
				}

				eventSeenAt.put(currentEvent, pos);
				kSuccessors.feedKSuccessor(null, toString(currentEvent), pos + 1);

				pos += 1;
			}

			for (XEventClass seen : eventSeenAt.keySet()) {
				kSuccessors.feedKSuccessor(toString(seen), null, pos - eventSeenAt.get(seen));
			}

			kSuccessors.feedKSuccessor(null, null, 1 + pos);
		}

		return kSuccessors;
	}

	public static UpToKSuccessorMatrix fromNode(Node node) throws UnknownTreeNodeException {

		if (node instanceof Task.Manual) {
			HashSet<String> activities = new HashSet<String>();
			String a = toString((Manual) node);
			activities.add(a);
			UpToKSuccessorMatrix result = new UpToKSuccessorMatrix(activities);
			result.feedKSuccessor(null, null, 2);
			result.feedKSuccessor(null, a, 1);
			result.feedKSuccessor(a, null, 1);
			return result;

		} else if (node instanceof Xor) {
			Xor xor = (Xor) node;

			Iterator<Node> it = xor.getChildren().iterator();
			UpToKSuccessorMatrix result = CombineXor.combine(fromNode(it.next()), fromNode(it.next()));
			while (it.hasNext()) {
				result = CombineXor.combine(result, fromNode(it.next()));
			}
			return result;

		} else if (node instanceof Seq) {
			Seq seq = (Seq) node;
			Iterator<Node> it = seq.getChildren().iterator();
			UpToKSuccessorMatrix result = CombineSequence.combine(fromNode(it.next()), fromNode(it.next()));
			while (it.hasNext()) {
				result = CombineSequence.combine(result, fromNode(it.next()));
			}
			return result;
		} else if (node instanceof XorLoop) {
			XorLoop loop = (XorLoop) node;
			return CombineLoop.combine(fromNode(loop.getChildren().get(0)), fromNode(loop.getChildren().get(1)));

		} else if (node instanceof And) {
			And parallel = (And) node;
			Iterator<Node> it = parallel.getChildren().iterator();
			UpToKSuccessorMatrix result = CombineParallel.combine(fromNode(it.next()), fromNode(it.next()));
			while (it.hasNext()) {
				result = CombineParallel.combine(result, fromNode(it.next()));
			}
			return result;

		} else {
			System.out.println(node.getClass().toString());
			throw new UnknownTreeNodeException();
		}
	}

	private static String toString(XEventClass e) {
		String n = e.toString();
		if (n.contains("+complete")) {
			n = n.substring(0, n.indexOf("+complete"));
		}
		return n;
	}

	private static String toString(Task.Manual node) {
		return node.getName();
	}
}
