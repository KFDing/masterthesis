package org.processmining.plugins.InductiveMiner.mining.logs;

import java.util.BitSet;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class IMTrace implements Iterable<XEvent> {

	private final int XTraceIndex;
	private final int IMTraceIndex;
	public final BitSet outEvents;
	private final IMLog log;

	public IMTrace(int XTraceIndex, int IMTraceIndex, BitSet outEvents, IMLog log) {
		assert (XTraceIndex >= 0);

		this.XTraceIndex = XTraceIndex;
		this.IMTraceIndex = IMTraceIndex;
		this.outEvents = outEvents;
		this.log = log;
	}

	private XTrace getXTrace() {
		return log.getTraceWithIndex(XTraceIndex);
	}

	/**
	 * 
	 * @return Whether the trace contains no events.
	 */
	public boolean isEmpty() {
		int next = outEvents.nextClearBit(0);
		return next == -1 || next >= getXTrace().size();
	}

	/**
	 * @return The number of events in the trace.
	 */
	public int size() {
		return getXTrace().size() - outEvents.cardinality();
	}

	public IMEventIterator iterator() {
		return new IMEventIterator(0, Integer.MAX_VALUE);
	}

	/**
	 * Returns a sublist. This is O(n) in the number of events in the trace.
	 * 
	 * @param from
	 *            index at which the sub list starts. Inclusive.
	 * @param to
	 *            index at which the sub list ends. Exclusive.
	 * @return
	 */
	public Iterable<XEvent> subList(final int from, final int to) {
		return new IMEventIterable(from, to);
	}

	@Deprecated
	public XEvent get(int index) {
		Iterator<XEvent> it = iterator();
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it.next();
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		for (XEvent e : this) {
			result.append(log.classify(this, e));
			result.append(" ");
			result.append(log.getLifeCycle(e).toString());
			result.append(",");
		}
		return result.toString();
	}

	public XAttributeMap getAttributes() {
		return getXTrace().getAttributes();
	}
	
	public int getIMTraceIndex() {
		return IMTraceIndex;
	}

	public class IMEventIterable implements Iterable<XEvent> {
		int from;
		int to;

		public IMEventIterable(int from, int to) {
			this.from = from;
			this.to = to;
		}

		public IMEventIterator iterator() {
			return new IMEventIterator(from, to);
		}
	}

	public class IMEventIterator implements Iterator<XEvent> {
		int from;
		int to;
		int now;
		int next;
		int counter;

		@Override
		public IMEventIterator clone() {
			IMEventIterator result = new IMEventIterator(0, to);
			result.from = from;
			result.to = to;
			result.now = now;
			result.next = next;
			result.counter = counter;
			return result;
		}

		public IMEventIterator(int from, int to) {
			now = -1;
			this.to = Math.min(to, getXTrace().size());
			next = outEvents.nextClearBit(0) < this.to ? outEvents.nextClearBit(0) : -1;
			counter = 0;

			//walk to from
			for (int i = 0; i < from; i++) {
				progress();
			}
		}

		public boolean hasNext() {
			return next != -1 && next < getXTrace().size() && counter < to;
		}

		/**
		 * Remove the current XEvent (= last given by next).
		 */
		public void remove() {
			outEvents.set(now);
		}

		public void removeAll() {
			while (hasNext()) {
				next();
				remove();
			}
		}

		public XEvent next() {
			progress();
			return getXTrace().get(now);
		}
		
		public XEvent get() {
			return getXTrace().get(now);
		}

		private void progress() {
			now = next;
			next = outEvents.nextClearBit(next + 1);
			counter++;
		}

		/**
		 * 
		 * @return the event class of the current (i.e. last returned by next())
		 *         event.
		 */
		public XEventClass classify() {
			return log.classify(IMTrace.this, getXTrace().get(now));
		}

		/**
		 * Split the trace such that the part before the current XEvent moves to
		 * a new trace. This iterator will not be altered. The newly trace will
		 * not be encountered by any trace iterator that was created before
		 * calling split().
		 * 
		 * @return the newly created trace
		 */
		public IMTrace split() {
			//copy this trace completely
			IMTrace newTrace = log.copyTrace(IMTrace.this, outEvents);

			//in the new trace, remove all events from now
			newTrace.outEvents.set(now, getXTrace().size());

			//in this trace, remove all events before now
			outEvents.set(0, now);

			return newTrace;
		}

		/**
		 * Return a new iterable that iterates from the current position
		 * (including) to the given iterator (exclusive)
		 * 
		 * @param it
		 * @return
		 */
		public Iterable<XEvent> getUntil(IMEventIterator it) {
			final int startAt = now;
			final int to = it.counter - 1;
			final int newCounter = counter - 1;
			return new Iterable<XEvent>() {
				public IMEventIterator iterator() {
					IMEventIterator result = new IMEventIterator(0, to);
					result.next = startAt;
					result.counter = newCounter;
					return result;
				}
			};
		}

		public boolean hasPrevious() {
			int previous = outEvents.previousClearBit(now - 1);
			return previous >= 0 && counter >= from;
		}

		public XEvent previous() {
			next = now;
			now = outEvents.previousClearBit(now - 1);
			counter--;
			return getXTrace().get(now);
		}
		
		public boolean isAtSameEvent(IMEventIterator other) {
			return other.now == now;
		}
	}

	public int getXTraceIndex() {
		return XTraceIndex;
	}

}
