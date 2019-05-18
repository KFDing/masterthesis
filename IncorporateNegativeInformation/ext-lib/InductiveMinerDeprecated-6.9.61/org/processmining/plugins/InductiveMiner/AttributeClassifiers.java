package org.processmining.plugins.InductiveMiner;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XLog;

public class AttributeClassifiers {

	/**
	 * A class that represents either a classifier or an attribute.
	 * 
	 * @author sleemans
	 *
	 */
	public static class AttributeClassifier implements Comparable<AttributeClassifier> {

		private final XEventClassifier classifier;
		private final String attribute;

		public AttributeClassifier(String attribute) {
			this.classifier = null;
			this.attribute = attribute;
		}

		public AttributeClassifier(XEventClassifier classifier) {
			this.classifier = classifier;
			this.attribute = null;
		}

		@Override
		public String toString() {
			if (classifier != null) {
				return classifier.toString();
			}
			return attribute;
		}

		public int compareTo(AttributeClassifier other) {
			if (other.classifier == null && classifier != null) {
				return -1;
			} else if (other.classifier != null && classifier == null) {
				return 1;
			} else if (other.classifier != null) {
				return classifier.toString().compareTo(other.classifier.toString());
			} else {
				return attribute.compareTo(other.attribute);
			}
		}

		public boolean isClassifier() {
			return classifier != null;
		}

		public XEventClassifier getClassifier() {
			return classifier;
		}

		public boolean isAttribute() {
			return attribute != null;
		}

		public String getAttribute() {
			return attribute;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
			result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttributeClassifier other = (AttributeClassifier) obj;
			if (attribute == null) {
				if (other.attribute != null)
					return false;
			} else if (!attribute.equals(other.attribute))
				return false;
			if (classifier == null) {
				if (other.classifier != null)
					return false;
			} else if (!classifier.equals(other.classifier))
				return false;
			return true;
		}
	}

	/**
	 * 
	 * @param log
	 * @param attributes
	 * @return A sorted list of attributes and classifiers; first classifiers,
	 *         than attributes, and the default classifier.
	 */
	public static Pair<AttributeClassifier[], AttributeClassifier> getAttributeClassifiers(XLog log,
			String[] attributes, boolean filterLifeCycleClassifiers) {

		AttributeClassifier firstClassifier = null;
		Set<AttributeClassifier> result = new TreeSet<>();

		//add classifiers
		{
			for (XEventClassifier classifier : log.getClassifiers()) {
				boolean include = true;
				if (filterLifeCycleClassifiers) {
					for (String attribute : classifier.getDefiningAttributeKeys()) {
						if (attribute.equals(XLifecycleExtension.KEY_TRANSITION)) {
							include = false;
						}
					}
				}
				if (include) {
					AttributeClassifier add = new AttributeClassifier(classifier);
					result.add(add);
					if (firstClassifier == null) {
						firstClassifier = add;
					}
				}
			}
		}

		//add attributes
		{
			for (String attribute : attributes) {
				if (!filterLifeCycleClassifiers || !attribute.equals(XLifecycleExtension.KEY_TRANSITION)) {
					result.add(new AttributeClassifier(attribute));
				}
			}
		}

		//transform to array
		AttributeClassifier[] result2 = new AttributeClassifier[result.size()];
		Iterator<AttributeClassifier> it = result.iterator();
		for (int i = 0; i < result2.length; i++) {
			result2[i] = it.next();
		}
		
		//look for the concept:name attribute
		for (AttributeClassifier classifier : result2) {
			if (classifier.isAttribute() && classifier.getAttribute().equals(XConceptExtension.KEY_NAME)) {
				firstClassifier = classifier;
			}
		}

		if (firstClassifier == null) {
			//if we still did not find a classifier, return a dummy one
			firstClassifier = new AttributeClassifier("empty classifier");
		}

		return Pair.of(result2, firstClassifier);
	}

	/**
	 * 
	 * @param attributeClassifiers
	 * @return An XEventClassifier representing the
	 */
	public static XEventClassifier constructClassifier(AttributeClassifier... attributeClassifiers) {
		String[] attributes = new String[attributeClassifiers.length];
		int i = 0;
		for (AttributeClassifier attributeClassifier : attributeClassifiers) {
			if (attributeClassifier.isClassifier()) {
				return attributeClassifier.getClassifier();
			}
			attributes[i] = attributeClassifier.getAttribute();
			i++;
		}
		return new XEventAttributeClassifier("custom classifier", attributes);
	}
}
