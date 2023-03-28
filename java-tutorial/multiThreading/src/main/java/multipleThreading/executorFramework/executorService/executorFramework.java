package multipleThreading.executorFramework.executorService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import multipleThreading.implementrunnable.SampleRunnable;

public class executorFramework {
	private Runnable runnable;

	public executorFramework(Runnable runnable) {
		this.runnable = runnable;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	private static void executorMethod() throws InterruptedException, ExecutionException {
		executorFramework obj = new executorFramework(new SampleRunnable());
		ExecutorService ex=Executors.newSingleThreadExecutor();
		Future future=ex.submit(obj.getRunnable());
		System.out.println("future: "+future.get());//output null indicates the thread succefully completed
		ex.shutdown();
//		ExecutorService ex=Executors.newFixedThreadPool(3);
//		ex.submit(obj.getRunnable());
//		ex.submit(obj.getRunnable());
//		ex.submit(obj.getRunnable());
//		ex.shutdown();

//		ExecutorService ex = Executors.newCachedThreadPool();
//		for (int i = 0; i < 100; i++) {
//			ex.submit(obj.getRunnable());
//		}
//		ex.shutdown();

		// another way of passing runnable object
//		SampleRunnable runnable =new SampleRunnable();
//		ExecutorService ex1=Executors.newSingleThreadExecutor();
//		ex1.submit(runnable);
//		ex1.shutdown();
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		executorMethod();
//		scheduledExecutorMethod();
	}

	private static void scheduledExecutorMethod() {
		//calling non scheduled exectors methods
//		executorFramework.executorMethod();
		
		//scheduled exectors methods
//		executorFramework obj=new executorFramework(new SampleRunnable());
//		ScheduledExecutorService ex=Executors.newSingleThreadScheduledExecutor();
//		ex.schedule(obj.getRunnable(), 5, TimeUnit.SECONDS);
//		ex.shutdown();
		
		//In cronjob dont shutdown the thread
//		executorFramework obj=new executorFramework(new SampleRunnable());
//		ScheduledExecutorService ex=Executors.newSingleThreadScheduledExecutor();
//		ex.scheduleAtFixedRate(obj.getRunnable(), 5, 10, TimeUnit.SECONDS);
		
//		In cronjob dont shutdown the thread
		executorFramework obj=new executorFramework(new SampleRunnable());
		ScheduledExecutorService ex=Executors.newSingleThreadScheduledExecutor();
		ex.scheduleWithFixedDelay(obj.getRunnable(), 5, 10, TimeUnit.SECONDS);
	}

	

}
