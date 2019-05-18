package org.processmining.plugins.InductiveMiner.mining.cuts.IMa;

import gnu.trove.set.hash.THashSet;

import java.util.BitSet;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace.IMEventIterator;

public class ConcurrentOptionalOrLog {
	private final int numberOfComponents;
	private final THashSet<BitSet> traces;

	/**
	 * Make sure the components are 1 .... n (i.e. normalised).
	 * 
	 * @param log
	 * @param components
	 */
	public ConcurrentOptionalOrLog(IMLog log, Components<XEventClass> components) {
		numberOfComponents = components.getNumberOfComponents();
		traces = new THashSet<>();
		for (IMTrace trace : log) {
			BitSet traceOccurrence = new BitSet(components.getNumberOfComponents());
			for (IMEventIterator it = trace.iterator(); it.hasNext();) {
				it.next();
				traceOccurrence.set(components.getComponentOf(it.classify()));
			}
			traces.add(traceOccurrence);
		}
	}

	public ConcurrentOptionalOrLog(THashSet<BitSet> traces, int numberOfComponents) {
		this.numberOfComponents = numberOfComponents;
		this.traces = traces;
	}

	/**
	 * Reduce the event log by merging the two components.
	 * 
	 * @param oldLog
	 * @param old2new
	 *            A map which maps the old component to a new component
	 * @param newNumberOfComponents
	 * @return
	 */
	public static ConcurrentOptionalOrLog mergeConcurrent(ConcurrentOptionalOrLog oldLog, int[] old2new,
			int newNumberOfComponents) {
		THashSet<BitSet> newTraces = new THashSet<>();
		for (BitSet oldTrace : oldLog.traces) {
			BitSet newTrace = new BitSet(oldTrace.size());
			for (int i = oldTrace.nextSetBit(0); i >= 0; i = oldTrace.nextSetBit(i + 1)) {
				newTrace.set(old2new[i]);
			}
			newTraces.add(newTrace);
		}

		return new ConcurrentOptionalOrLog(newTraces, newNumberOfComponents);
	}

	public String toString() {
		return traces.toString();
	}

	public int getNumberOfComponents() {
		return numberOfComponents;
	}

	public THashSet<BitSet> getTraces() {
		return traces;
	}

}
