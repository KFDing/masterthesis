package org.processmining.plugins.InductiveMiner.jobList;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class JobListConcurrent implements JobList {
	
	private ConcurrentLinkedQueue<Future<?>> jobs;
	private ExecutorService pool;
	
	public JobListConcurrent(ExecutorService pool) {
		jobs = new ConcurrentLinkedQueue<Future<?>>();
		this.pool = pool;
	}

	public void addJob(Runnable job) {
		Future<?> x = pool.submit(job);
		jobs.add(x);
	}

	public void join() throws ExecutionException {
		while (!jobs.isEmpty()) {
			try {
				jobs.poll().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
