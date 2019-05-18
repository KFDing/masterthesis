package org.processmining.plugins.InductiveMiner;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;

/**
 * Multi-selection combobox to select a classifier. It shows the classifiers
 * from the event log, as well as an option to construct one from data
 * attributes from the event log.
 * 
 * @author sleemans
 *
 */
public class ClassifierChooser extends JPanel {

	private static final long serialVersionUID = 3348039386637737989L;
	private final MultiComboBox<AttributeClassifier> combobox;

	/**
	 * Notice: this constructor walks through the event log to gather
	 * attributes.
	 * 
	 * @param log
	 */
	public ClassifierChooser(XLog log) {
		this(log, getEventAttributes(log));
	}

	/**
	 * This constructor does not walk through the event log, but takes the list
	 * of event attributes provided.
	 * 
	 * @param log
	 * @param eventAttributes
	 */
	private ClassifierChooser(XLog log, String[] eventAttributes) {
		this(log, eventAttributes, false);
	}

	/**
	 * This construct does not walk through the event log, but takes the list of
	 * event attributes provided. Life cycle transition classifiers and
	 * attributes are filtered if requested.
	 * 
	 * @param log
	 * @param eventAttributes
	 * @param filterLifeCycleTransition
	 */
	public ClassifierChooser(XLog log, String[] eventAttributes, boolean filterLifeCycleTransition) {
		setLayout(new BorderLayout());
		setOpaque(false);
		this.combobox = new MultiComboBox<>(AttributeClassifier.class, new AttributeClassifier[0]);
		add(combobox, BorderLayout.CENTER);

		if (log != null && eventAttributes != null) {
			Pair<AttributeClassifier[], AttributeClassifier> p = AttributeClassifiers.getAttributeClassifiers(log,
					eventAttributes, filterLifeCycleTransition);
			replaceClassifiers(p.getA(), p.getB());
		}
	}

	public void addActionListener(ActionListener actionListener) {
		combobox.addActionListener(actionListener);
	}

	public XEventClassifier getSelectedClassifier() {
		return AttributeClassifiers.constructClassifier(combobox.getSelectedObjects());
	}

	/**
	 * Replace the classifiers in the combobox and select (only) one.
	 * 
	 * @param attributeClassifiers
	 * @param selectedClassifier
	 */
	public void replaceClassifiers(AttributeClassifier[] attributeClassifiers, AttributeClassifier selectedClassifier) {
		combobox.removeAllItems();
		for (AttributeClassifier classifier : attributeClassifiers) {
			combobox.addItem(classifier, classifier.isClassifier());
		}
		combobox.setSelectedItem(selectedClassifier);
	}

	public MultiComboBox<AttributeClassifier> getMultiComboBox() {
		return combobox;
	}

	/**
	 * 
	 * @param log
	 * @return A list of all event attributes (keys). Linear in the size of the
	 *         event log.
	 */
	public static String[] getEventAttributes(XLog log) {
		THashSet<String> attributes = new THashSet<>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				attributes.addAll(event.getAttributes().keySet());
			}
		}
		return attributes.toArray(new String[attributes.size()]);
	}
}
