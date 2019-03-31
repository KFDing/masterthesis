package org.processmining.incorporatenegativeinformation.models;

import java.awt.Color;

import org.deckfour.xes.id.XID;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.DefaultEvent;

final class XESEvent extends DefaultEvent {

	private final XID eventId;

	public XESEvent(XID eventId, Color color, String label, String topLabel, String bottomLabel, String bottomLabel2) {
		super(color, label, topLabel, bottomLabel, bottomLabel2);
		this.eventId = eventId;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof XESEvent))
			return false;
		XESEvent other = (XESEvent) obj;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		return true;
	}

}