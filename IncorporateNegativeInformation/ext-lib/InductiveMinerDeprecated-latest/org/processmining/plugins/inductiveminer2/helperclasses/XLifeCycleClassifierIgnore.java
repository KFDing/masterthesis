package org.processmining.plugins.inductiveminer2.helperclasses;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier;

/**
 * Class to ignore life cycle transition altogether, by simply classifying each
 * event as 'complete'.
 * 
 * @author sander
 *
 */
public class XLifeCycleClassifierIgnore implements XLifeCycleClassifier {

	public void accept(XVisitor arg0, XLog arg1) {

	}

	public String getClassIdentity(XEvent arg0) {
		return "complete";
	}

	public String[] getDefiningAttributeKeys() {
		return new String[0];
	}

	public String name() {
		return "ignore life cycle transition";
	}

	public boolean sameEventClass(XEvent arg0, XEvent arg1) {
		return true;
	}

	public void setName(String arg0) {

	}

	public Transition getLifeCycleTransition(XEvent event) {
		return Transition.complete;
	}

	public Transition getLifeCycleTransition(String transition) {
		return Transition.complete;
	}

}
