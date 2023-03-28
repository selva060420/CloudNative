package multipleThreading.Synchronized.method;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedMethod implements Runnable {
	public void nonSyncMethod() {
		System.out.println("Calling 5th table by nonsyncmethod, threadName: " + Thread.currentThread().getName());
		for (int i = 1; i <= 10; i++) {
			System.out.println("5th table: " + i + " * 5= " + 5 * i);
		}
	}

	public synchronized void syncMethod() {
		System.out.println("Calling 5th table by syncmethod, threadName: " + Thread.currentThread().getName());
		for (int i = 1; i <= 10; i++) {
			System.out.println("5th table: " + i + " * 5= " + 5 * i);
		}
	}

	@Override
	public void run() {
//		this.nonSyncMethod();
		this.syncMethod();
	}

	public static void main(String[] args) {
		SynchronizedMethod runnable = new SynchronizedMethod();
		ExecutorService service = Executors.newCachedThreadPool();
		for (int i = 0; i < 50; i++) {
			service.submit(runnable);
		}
		service.shutdown();
	}
}
