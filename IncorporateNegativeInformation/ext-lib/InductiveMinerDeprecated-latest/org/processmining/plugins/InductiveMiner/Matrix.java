package org.processmining.plugins.InductiveMiner;

import gnu.trove.map.hash.THashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Matrix<X extends Comparable<X>, Y extends Number> {
	private Map<Pair<Maybe<X>, Maybe<X>>, Y> matrix;
	private List<X> activities;
	private boolean includeStartEnd;

	public Matrix(X[] activities, boolean includeStartEnd) {
		matrix = new THashMap<Pair<Maybe<X>, Maybe<X>>, Y>();
		this.activities = Arrays.asList(activities);
		Collections.sort(this.activities);
		this.includeStartEnd = includeStartEnd;
	}

	public Y get(X from, X to) {
		return matrix.get(new Pair<Maybe<X>, Maybe<X>>(new Maybe<X>(from), new Maybe<X>(to)));
	}

	public void set(X from, X to, Y newValue) {
		matrix.put(new Pair<Maybe<X>, Maybe<X>>(new Maybe<X>(from), new Maybe<X>(to)), newValue);
	}

	public List<X> getActivities() {
		return activities;
	}

	public String toString() {
		return toString(false);
	}

	public String toString(boolean useHTML) {
		StringBuilder s = new StringBuilder();

		String newLine = "\n";
		String newCell = "";
		if (useHTML) {
			newLine = "\n<tr>";
			newCell = "<td>";
			s.append("<table>");
		}

		//titles
		if (useHTML) {
			s.append(newLine);
		}
		s.append(newCell);
		s.append(placeHolder());
		for (X from : activities) {
			s.append(newCell);
			s.append(shortenString(from.toString()));
		}
		s.append(newCell);
		if (includeStartEnd) {
			s.append(" -E-");
			s.append(newLine);
		}

		{
			if (includeStartEnd) {
				s.append(newCell);
				s.append(" -S- ");
				for (X to : activities) {
					s.append(newCell);
					Y x = get(null, to);
					if (x != null) {
						s.append(shortenNumber(x));
					} else {
						s.append(placeHolder());
					}
				}

				Y x = get(null, null);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}
			
			s.append(newLine);
		}

		for (X from : activities) {
			s.append(newCell);
			s.append(shortenString(from.toString()));
			for (X to : activities) {
				Y x = get(from, to);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}

			if (includeStartEnd) {
				Y x = get(from, null);
				s.append(newCell);
				if (x != null) {
					s.append(shortenNumber(x));
				} else {
					s.append(placeHolder());
				}
			}

			s.append(newLine);
		}
		if (useHTML) {
			s.append("</table>");
		}
		return s.toString();
	}

	public static String placeHolder() {
		return "  .  ";
	}

	public static String shortenString(String name) {
		String s = name.substring(0, Math.min(name.length(), 3));
		return String.format("%1$" + 4 + "s ", s);
	}

	public static String shortenNumber(Number n) {
		if (n instanceof Double) {
			return String.format("%1.2f ", n);
		} else {
			return String.format("%3d  ", n);
		}
	}
	
	protected void debug(String x) {
		System.out.println(x);
	}

}
