package org.processmining.plugins.InductiveMiner.jobList;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolMiner {
	private ExecutorService pool;
	private int numberOfThreads;
	private ConcurrentLinkedQueue<Future<?>> jobs;
	
	//constructor, makes an estimate of the number of threads.
	public ThreadPoolMiner() {
		numberOfThreads = Runtime.getRuntime().availableProcessors();
		init();
	}
	
	//constructor, takes a number of threads. Provide 1 to execute synchronously.
	public ThreadPoolMiner(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
		init();
	}
	
	public static ThreadPoolMiner useFactor(double factor) {
		return new ThreadPoolMiner((int) (Runtime.getRuntime().availableProcessors() * factor));
	}
	
	//add a job to be executed. Will block if executed synchronously
	public synchronized void addJob(Runnable job) {
		if (numberOfThreads > 1) {
			Future<?> x = pool.submit(job);
			jobs.add(x);
		} else {
			job.run();
		}
	}
	
	//wait till all jobs have finished execution. While waiting, new jobs can still be added and will be executed.
	//Hence, will block until the thread pool is idle
	public void join() throws ExecutionException {
		if (numberOfThreads > 1) {
			ExecutionException error = null;
			
			//wait for all jobs to finish
			while (!jobs.isEmpty()) {
				Future<?> job = jobs.poll();
				try {
					job.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					//e.printStackTrace();
					error = e;
				}
			}
			
			//all jobs are done
			
			pool.shutdown();
			
			 if (error != null) {
				throw new ExecutionException(error);
			}
		}
	}
	
	private void init() {
		if (numberOfThreads > 1) {
			pool = Executors.newFixedThreadPool(numberOfThreads);
			jobs = new ConcurrentLinkedQueue<Future<?>>();
		}
	}
	
	public int getNumerOfThreads() {
		return numberOfThreads;
	}
}
