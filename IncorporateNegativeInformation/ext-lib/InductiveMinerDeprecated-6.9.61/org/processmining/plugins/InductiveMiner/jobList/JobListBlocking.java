package org.processmining.plugins.InductiveMiner.jobList;

import java.util.concurrent.ExecutionException;

public class JobListBlocking implements JobList {

	public void addJob(Runnable job) {
		job.run();
	}

	public void join() throws ExecutionException {
		
	}
	
}
