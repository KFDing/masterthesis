package org.processmining.plugins.InductiveMiner;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;

public class Classifiers {

	public static class ClassifierWrapper implements Comparable<ClassifierWrapper> {
		public final XEventClassifier classifier;
		public final String prefix;

		public ClassifierWrapper(String prefix, XEventClassifier classifier) {
			this.classifier = classifier;
			this.prefix = prefix;
		}

		public String toString() {
			return prefix + classifier.name();
		}

		public int compareTo(ClassifierWrapper o) {
			return toString().compareTo(o.toString());
		}
	}

	public static ClassifierWrapper[] getClassifiers(XLog log) {
		Set<ClassifierWrapper> classifiers = new TreeSet<>();

		if (log != null) {
			for (XEventClassifier c : log.getClassifiers()) {
				classifiers.add(new ClassifierWrapper("(log) ", c));
			}
			
			XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log);
			for (XEventClassifier c : xLogInfo.getEventClassifiers()) {
				classifiers.add(new ClassifierWrapper("(log info) ", c));
			}
		}

		ClassifierWrapper[] result = new ClassifierWrapper[classifiers.size() + 2];
		result[0] = new ClassifierWrapper("", new XEventNameClassifier());
		result[1] = new ClassifierWrapper("", new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier()));
		Iterator<ClassifierWrapper> it = classifiers.iterator();
		for (int i = 0; i < classifiers.size(); i++) {
			result[i + 2] = it.next();
		}
		return result;
	}
}
