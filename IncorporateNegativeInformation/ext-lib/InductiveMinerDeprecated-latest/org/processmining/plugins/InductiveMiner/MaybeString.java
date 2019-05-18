package org.processmining.plugins.InductiveMiner;

public class MaybeString extends Maybe<String> {
	
	public MaybeString(String x) {
		super(x);
	}
	
	public static MaybeString load(String x) {
		if (x.length() == 0) {
			return new MaybeString(null);
		}
		return new MaybeString(x.substring(1));
	}
	
	@Override
	public String get() {
		return x;
	}
	
	@Override
	public String toString() {
		if (x == null) {
			return "";
		} else {
			return "!" + x.toString();
		}
	}
}
