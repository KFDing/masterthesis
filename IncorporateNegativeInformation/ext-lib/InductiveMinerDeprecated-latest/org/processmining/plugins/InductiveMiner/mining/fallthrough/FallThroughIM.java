package org.processmining.plugins.InductiveMiner.mining.fallthrough;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class FallThroughIM implements FallThrough {

	private static List<FallThrough> fallThroughs = new ArrayList<FallThrough>(Arrays.asList(
			new FallThroughActivityOncePerTraceConcurrent(true),
			new FallThroughActivityConcurrent(),
			new FallThroughTauLoopStrict(false),
			new FallThroughTauLoop(false),
			new FallThroughFlowerWithoutEpsilon(),
			new FallThroughFlowerWithEpsilon()
			));
	
	public Node fallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
		Node n = null;
		Iterator<FallThrough> it = fallThroughs.iterator();
		while (n == null && it.hasNext()) {

			if (minerState.isCancelled()) {
				return null;
			}

			n = it.next().fallThrough(log, logInfo, tree, minerState);
		}
		return n;
	}
}
