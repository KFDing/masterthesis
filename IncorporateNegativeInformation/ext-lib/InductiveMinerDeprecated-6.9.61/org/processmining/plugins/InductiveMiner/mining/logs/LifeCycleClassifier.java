package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;

public class LifeCycleClassifier implements XLifeCycleClassifier {

	public void accept(XVisitor visitor, XLog log) {
		/*
		 * First call.
		 */
		visitor.visitClassifierPre(this, log);
		/*
		 * Last call.
		 */
		visitor.visitClassifierPost(this, log);
	}

	public String getClassIdentity(XEvent event) {
		XAttribute attribute = event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION);
		if (attribute != null) {
			return attribute.toString().trim().toLowerCase();
		}
		return "complete";
	}

	public String[] getDefiningAttributeKeys() {
		return new String[] { XLifecycleExtension.KEY_TRANSITION };
	}

	public String name() {
		return "Lifecycle transition case independent";
	}

	public boolean sameEventClass(XEvent eventA, XEvent eventB) {
		return getClassIdentity(eventA).equals(getClassIdentity(eventB));
	}

	public void setName(String arg0) {

	}

	public Transition getLifeCycleTransition(XEvent event) {
		return getLifeCycleTransition(getClassIdentity(event));
	}

	public Transition getLifeCycleTransition(String transition) {
		switch (transition) {
			case "complete" :
				return Transition.complete;
			case "start" :
				return Transition.start;
			default :
				return Transition.other;
		}
	}

}
