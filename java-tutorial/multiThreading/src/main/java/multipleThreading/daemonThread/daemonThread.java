package multipleThreading.daemonThread;

import multipleThreading.implementrunnable.SampleRunnable;
//Daemon thread has low priority,It wont run alone .it will be executed along with user thread
public class daemonThread {

	public static void main(String[] args) {
		SampleRunnable runnable=new SampleRunnable();
		Thread thread=new Thread(runnable, "Daemon thread");
		Thread userthread=new Thread(runnable, "user thread");
		thread.setDaemon(true);
		System.out.println("Daemon or not: "+thread.isDaemon());
		userthread.start();
		thread.start();
	}

}
