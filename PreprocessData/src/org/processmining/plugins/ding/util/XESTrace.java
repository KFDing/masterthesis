package org.processmining.plugins.ding.util;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.log.utils.XUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class XESTrace extends AbstractCollection<Event> implements Trace<Event> {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy");
	private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");
	
	private static final int MAX_TRACE_SIZE = 1000000;

	private final Map<XEvent, Color> colorMap;
	private final XEventClassifier classifier;
	
	private final XTrace xesTrace;
	private final String extraInfo;

	public XESTrace(XTrace trace, XEventClassifier classifier) {
		this(trace, classifier, Collections.<XEvent, Color>emptyMap(), ProMTraceView.EMPTY_LABEL);
	}

	public XESTrace(XTrace xesTrace, XEventClassifier classifier, Map<XEvent, Color> colorMap) {
		this(xesTrace, classifier, colorMap, ProMTraceView.EMPTY_LABEL);		
	}

	public XESTrace(XTrace xesTrace, XEventClassifier classifier, Map<XEvent, Color> colorMap, String extraInfo) {
		this.xesTrace = xesTrace;
		this.classifier = classifier;
		this.colorMap = colorMap;
		if (xesTrace.size() > MAX_TRACE_SIZE) {
			this.extraInfo = extraInfo.concat(" limited to the first "+MAX_TRACE_SIZE+" events");
		} else {
			this.extraInfo = extraInfo;
		}
	}
	
	public final int size() {
		return Math.min(xesTrace.size(), MAX_TRACE_SIZE);
	}

	public Iterator<Event> iterator() {
		if (xesTrace.size() > MAX_TRACE_SIZE) {
			// Limit the number of shown events
			List<XEvent> subList = xesTrace.subList(0, MAX_TRACE_SIZE);
			return Iterators.transform(subList.iterator(), new Function<XEvent, Event>() {

				public Event apply(XEvent e) {
					return createXESEvent(e, colorMap.get(e));
				}
			});
		} else {
			return Iterators.transform(xesTrace.iterator(), new Function<XEvent, Event>() {

				public Event apply(XEvent e) {
					return createXESEvent(e, colorMap.get(e));
				}
			});
		}
	}

	public final Event createXESEvent(XEvent e, Color color) {
		String label = classifier.getClassIdentity(e);
		String bottomLabel = null;
		String bottomLabel2 = null;
		String topLabel = null;
		
		Date time = XTimeExtension.instance().extractTimestamp(e);
		String resource = XOrganizationalExtension.instance().extractResource(e);
		String transition = XLifecycleExtension.instance().extractTransition(e);
		if (time != null || resource != null) {
			StringBuilder topLabelBuilder = new StringBuilder();
			if (time != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(time);
				if (isTimeUnset(cal)) {
					topLabelBuilder.append(DATE_FORMAT.format(time));
				} else {
					topLabelBuilder.append(DATETIME_FORMAT.format(time));
				}
			}

			if (resource != null) {
				topLabelBuilder.append(" (");
				topLabelBuilder.append(resource);
				topLabelBuilder.append(")");
			} 
			
			if (transition != null) {
				topLabelBuilder.append(" [");
				topLabelBuilder.append(transition);
				topLabelBuilder.append("]");
			}

			topLabel = (topLabelBuilder.length() > 0) ? topLabelBuilder.toString() : ProMTraceView.EMPTY_LABEL;			
		}

		Iterable<XAttribute> attributeToDisplay = Iterables.filter(e.getAttributes().values(),
				new Predicate<XAttribute>() {

					public boolean apply(XAttribute a) {
						return !(a.getExtension() instanceof XConceptExtension
								|| a.getExtension() instanceof XTimeExtension
								|| a.getExtension() instanceof XOrganizationalExtension || a.getExtension() instanceof XLifecycleExtension);
					}
				});
		List<XAttribute> selectedAttributes = Ordering.from(new Comparator<XAttribute>() {

			public int compare(XAttribute o1, XAttribute o2) {
				String s1 = o1.getKey() + o1.toString();
				String s2 = o2.getKey() + o2.toString();
				return Ints.compare(s1.length(), s2.length());
			}
			
		}).leastOf(attributeToDisplay, 2);
		Iterator<XAttribute> iter = selectedAttributes.iterator();
		if (iter.hasNext()) {
			XAttribute a = iter.next();
			bottomLabel = a.getKey() + "=" + a.toString();
		}
		if (iter.hasNext()) {
			XAttribute a = iter.next();
			bottomLabel2 = a.getKey() + "=" + a.toString();
		}

		return new XESEvent(e.getID(), color == null ? Color.LIGHT_GRAY : color, label, topLabel, bottomLabel, bottomLabel2);
	}

	private static final boolean isTimeUnset(Calendar cal) {
		return cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0;
	}

	public final String getName() {
		String traceName = XUtils.getConceptName(xesTrace);
		if (traceName != null) {
			return traceName;
		} else {
			return ProMTraceView.EMPTY_LABEL;
		}
	}

	public final Color getNameColor() {
		return null;
	}

	public final String getInfo() {
		return extraInfo;
	}

	public final Color getInfoColor() {
		return null;
	}

}