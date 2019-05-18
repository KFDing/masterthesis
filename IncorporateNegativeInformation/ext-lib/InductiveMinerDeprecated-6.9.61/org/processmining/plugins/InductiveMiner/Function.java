package org.processmining.plugins.InductiveMiner;


public interface Function<I, O> {
	public O call(I input) throws Exception;
}