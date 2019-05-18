package org.processmining.plugins.InductiveMiner.mining.logs;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

public interface XLifeCycleClassifier extends XEventClassifier {

	public static enum Transition {
		start, complete, other
	}

	public Transition getLifeCycleTransition(XEvent event);

	public Transition getLifeCycleTransition(String transition);
}
