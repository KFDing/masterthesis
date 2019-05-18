package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIM implements CutFinder {
	
	private static List<CutFinder> cutFinders = new ArrayList<CutFinder>(Arrays.asList(
			new CutFinderIMExclusiveChoice(),
			new CutFinderIMSequence(),
			new CutFinderIMConcurrentWithMinimumSelfDistance(),
			new CutFinderIMLoop(),
			new CutFinderIMConcurrent()
			));

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		Cut c = null;
		Iterator<CutFinder> it = cutFinders.iterator();
		while (it.hasNext() && (c == null || !c.isValid())) {
			c = it.next().findCut(log, logInfo, minerState);
		}
		return c;
	}
	
}
