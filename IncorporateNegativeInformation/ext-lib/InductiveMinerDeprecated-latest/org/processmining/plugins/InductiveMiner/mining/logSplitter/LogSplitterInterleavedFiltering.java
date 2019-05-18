package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

public class LogSplitterInterleavedFiltering implements LogSplitter {

	public LogSplitResult split(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {

		Components<XEventClass> components = new Components<>(cut.getPartition());
		IMLog[] sublogs = new IMLog[components.getNumberOfComponents()];
		List<Iterator<IMTrace>> iterators = new ArrayList<>(components.getNumberOfComponents());
		for (int i = 0; i < components.getNumberOfComponents(); i++) {
			sublogs[i] = log.clone();
			iterators.add(sublogs[i].iterator());
		}
		MultiSet<XEventClass> noise = new MultiSet<>();

		for (IMTrace trace : log) {

			if (minerState.isCancelled()) {
				return null;
			}

			//initialise the result array
			int[] inChild = new int[trace.size()];

			findChild(inChild, 0, trace.size(), components, trace, log);

			//split the trace
			for (int component = 0; component < components.getNumberOfComponents(); component++) {
				IMTrace subtrace = iterators.get(component).next();
				int i = 0;
				for (IMEventIterator it = subtrace.iterator(); it.hasNext();) {
					it.next();
					if (components.getComponentOf(it.classify()) != component) {
						it.remove();
					}
				}
			}
		}

		return new LogSplitResult(Arrays.asList(sublogs), noise);
	}

	/**
	 * Fills the result array: sets each element to the index of the component
	 * it belongs to, or -1 if it is noise.
	 * 
	 * @param result
	 */
	private static void findChild(int[] result, int from, int to, Components<XEventClass> components, IMTrace trace,
			IMLog log) {
		//get the maximum sublist
		int maxRunValue = 0;
		int maxRunComponent = 0;
		int maxRunStart = 0;
		int maxRunEnd = 0;
		{
			int[] values = new int[components.getNumberOfComponents()];
			int[] startOfRun = new int[components.getNumberOfComponents()];
			int component;
			for (int i = from; i < to; i++) {
				if (result[i] != -1) {
					XEventClass activity = log.classify(trace, trace.get(i));
					component = components.getComponentOf(activity);

					if (values[component] < 0) {
						//this starts a new run
						values[component] = 1;
						startOfRun[component] = i;
					} else {
						//this continues an existing run
						values[component]++;
					}

					//decrease the other runs
					for (int j = 0; j < components.getNumberOfComponents(); j++) {
						if (j != component) {
							values[j]--;
						}
					}

					if (values[component] > maxRunValue) {
						maxRunComponent = component;
						maxRunValue = values[component];
						maxRunStart = startOfRun[component];
						maxRunEnd = i;
					}
				}
			}
		}

		//walk again over the trace to denote the children
		for (int i = maxRunStart; i <= maxRunEnd; i++) {
			if (maxRunStart <= i && i <= maxRunEnd) {
				if (components.getComponentOf(log.classify(trace, trace.get(i))) == maxRunComponent) {
					result[i] = maxRunComponent;
				} else {
					result[i] = -1;
				}
			} else if (components.getComponentOf(log.classify(trace, trace.get(i))) == maxRunComponent) {
				//other occurrences of traces are noise
				result[i] = -1;
			}
		}

		//recurse on the side-things
		if (maxRunStart > from) {
			findChild(result, from, maxRunStart - 1, components, trace, log);
		}
		if (maxRunEnd + 1 < to) {
			findChild(result, maxRunEnd + 1, to, components, trace, log);
		}
	}
}
