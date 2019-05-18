package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.BitSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

public interface IMLog extends Iterable<IMTrace> {

	/*
	 * Memory-lightweight implementation of a filtering system.
	 */

	/**
	 * Clone this IMLog. The new one might be based on the same XLog as the old
	 * one.
	 * 
	 * @return
	 */
	public IMLog clone();

	/**
	 * Classify an event
	 * 
	 * @return
	 */
	public XEventClass classify(IMTrace IMTrace, XEvent event);

	public XEventClassifier getClassifier();
	
	public void setClassifier(XEventClassifier classifier);
	
	public Transition getLifeCycle(XEvent event);
	
	public XLifeCycleClassifier getLifeCycleClassifier();
	
	public void setLifeCycleClassifier(XLifeCycleClassifier lifeCycleClassifier);

	public XTrace getTraceWithIndex(int traceIndex);

	/**
	 * Return the number of traces in the log
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Copy a trace and return the copy.
	 * 
	 * @param trace
	 * @param traceOutEvents
	 *            A bitset showing for each event of the underlying XTrace
	 *            whether this event is still included.
	 * @return
	 */
	public IMTrace copyTrace(IMTrace trace, BitSet traceOutEvents);

	/**
	 * Copy a trace and return the copy.
	 * 
	 * @param trace
	 * @return
	 */
	public IMTrace copyTrace(IMTrace trace);

	public String toString();

	public XLog toXLog();

	/**
	 * Turns the IMLog into an XLog, and makes a new IMLog out of it. Use this
	 * method to reduce memory usage if the log becomes sparse.
	 * 
	 * @return the newly created IMLog, which has no connection anymore to the
	 *         original XLog.
	 */
	public IMLog decoupleFromXLog();
}
