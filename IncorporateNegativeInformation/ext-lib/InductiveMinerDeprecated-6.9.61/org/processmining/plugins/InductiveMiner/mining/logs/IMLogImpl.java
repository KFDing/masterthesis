package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

import gnu.trove.list.array.TIntArrayList;

public class IMLogImpl implements IMLog {

	/*
	 * Memory-lightweight implementation of a filtering system.
	 */

	protected final XLog xLog;
	private final BitSet outTraces;
	private final BitSet[] outEvents;

	protected final TIntArrayList addedTraces;
	private final List<BitSet> addedTracesOutEvents;

	protected XEventClassifier activityClassifier;
	protected XLifeCycleClassifier lifeCycleClassifier;

	/**
	 * Create an IMlog from an XLog.
	 * 
	 * @param xLog
	 */
	public IMLogImpl(XLog xLog, XEventClassifier activityClassifier, XLifeCycleClassifier lifeCycleClassifier) {
		this.xLog = xLog;
		outTraces = new BitSet(xLog.size());
		outEvents = new BitSet[xLog.size()];
		for (int i = 0; i < xLog.size(); i++) {
			outEvents[i] = new BitSet();
		}

		addedTraces = new TIntArrayList();
		addedTracesOutEvents = new ArrayList<>();

		this.activityClassifier = activityClassifier;
		this.lifeCycleClassifier = lifeCycleClassifier;
	}

	/**
	 * Clone an existing IMlog
	 * 
	 * @param log
	 */
	public IMLogImpl(IMLogImpl log) {
		this.xLog = log.xLog;
		outTraces = (BitSet) log.outTraces.clone();
		outEvents = new BitSet[xLog.size()];
		for (int i = 0; i < xLog.size(); i++) {
			outEvents[i] = (BitSet) log.outEvents[i].clone();
		}

		addedTraces = new TIntArrayList(log.addedTraces);
		addedTracesOutEvents = new ArrayList<>(addedTraces.size());
		for (int i = 0; i < addedTraces.size(); i++) {
			addedTracesOutEvents.add((BitSet) log.addedTracesOutEvents.get(i).clone());
		}

		this.activityClassifier = log.activityClassifier;
		this.lifeCycleClassifier = log.lifeCycleClassifier;
	}

	public IMLog clone() {
		return new IMLogImpl(this);
	}

	/**
	 * Classify an event
	 * 
	 * @return
	 */
	public XEventClass classify(IMTrace IMTrace, XEvent event) {
		return new XEventClass(activityClassifier.getClassIdentity(event), 0);
	}

	public XEventClassifier getClassifier() {
		return activityClassifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.activityClassifier = classifier;
	}

	public Transition getLifeCycle(XEvent event) {
		return lifeCycleClassifier.getLifeCycleTransition(event);
	}

	public XLifeCycleClassifier getLifeCycleClassifier() {
		return lifeCycleClassifier;
	}

	public void setLifeCycleClassifier(XLifeCycleClassifier classifier) {
		this.lifeCycleClassifier = classifier;
	}

	public XTrace getTraceWithIndex(int traceIndex) {
		return xLog.get(traceIndex);
	}

	/**
	 * Return the number of traces in the log
	 * 
	 * @return
	 */
	public int size() {
		return (xLog.size() - outTraces.cardinality()) + addedTraces.size();
	}

	public IMTrace copyTrace(IMTrace trace, BitSet traceOutEvents) {
		int index = trace.getXTraceIndex();
		assert (index >= 0);

		addedTraces.add(index);
		BitSet newOutEvents = (BitSet) traceOutEvents.clone();
		addedTracesOutEvents.add(newOutEvents);
		return new IMTrace(index, -addedTraces.size(), newOutEvents, this);
	}

	public IMTrace copyTrace(IMTrace trace) {
		int index = trace.getXTraceIndex();
		assert (index >= 0);

		addedTraces.add(index);
		BitSet newOutEvents = (BitSet) trace.outEvents.clone();
		addedTracesOutEvents.add(newOutEvents);
		return new IMTrace(index, -addedTraces.size(), newOutEvents, this);
	}

	public Iterator<IMTrace> iterator() {
		final IMLogImpl t = this;
		return new Iterator<IMTrace>() {

			int next = init();
			int now = next - 1;

			private int init() {
				if (addedTraces.isEmpty()) {
					//start with normal traces
					return outTraces.nextClearBit(0) < xLog.size() ? outTraces.nextClearBit(0) : xLog.size();
				} else {
					//start with added traces
					return -addedTraces.size();
				}
			}

			public boolean hasNext() {
				return next < xLog.size();
			}

			public void remove() {
				if (now >= 0) {
					//we are in the normal traces
					outTraces.set(now);
				} else {
					//we are in the added traces
					int x = -now - 1;
					addedTraces.removeAt(x);
					addedTracesOutEvents.remove(x);
				}
			}

			public IMTrace next() {
				now = next;
				if (next < -1) {
					//we are in the added traces
					next = next + 1;
				} else {
					//we are in the normal traces
					next = outTraces.nextClearBit(next + 1);
				}

				if (now < 0) {
					//we are in the added traces
					return new IMTrace(addedTraces.get(-now - 1), now, addedTracesOutEvents.get(-now - 1), t);
				} else {
					//we are in the normal traces
					return new IMTrace(now, now, outEvents[now], t);
				}
			}
		};
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (IMTrace trace : this) {
			result.append(trace.toString());
			result.append("\n");
		}
		return result.toString();
	}

	public XLog toXLog() {
		XAttributeMap map = new XAttributeMapImpl();
		XLog result = new XLogImpl(map);
		for (IMTrace trace : this) {
			if (trace.isEmpty()) {
				result.add(new XTraceImpl(map));
			} else {
				XTrace xTrace = new XTraceImpl(map);
				for (XEvent e : trace) {
					xTrace.add(new XEventImpl((XAttributeMap) e.getAttributes().clone()));
				}
				result.add(xTrace);
			}
		}

		return result;
	}

	/**
	 * Turns the IMLog into an XLog, and makes a new IMLog out of it. Use this
	 * method to reduce memory usage if the log becomes sparse.
	 * 
	 * @return the newly created IMLog, which has no connection anymore to the
	 *         original XLog.
	 */
	public IMLogImpl decoupleFromXLog() {
		XLog xLog = toXLog();
		return new IMLogImpl(xLog, activityClassifier, lifeCycleClassifier);
	}
}
