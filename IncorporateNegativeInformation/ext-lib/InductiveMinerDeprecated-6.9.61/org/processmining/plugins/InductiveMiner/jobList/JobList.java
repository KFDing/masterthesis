package org.processmining.plugins.InductiveMiner.jobList;

import java.util.concurrent.ExecutionException;

public interface JobList {
	
	public void addJob(Runnable job);
	
	public void join() throws ExecutionException;
	
}
