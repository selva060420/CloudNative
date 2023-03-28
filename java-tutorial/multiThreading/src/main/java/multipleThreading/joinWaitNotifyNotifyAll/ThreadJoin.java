package multipleThreading.joinWaitNotifyNotifyAll;

import multipleThreading.implementrunnable.SampleRunnable;

public class ThreadJoin {

	public static void main(String[] args) {
		int counter=0;
		SampleRunnable runnable=new SampleRunnable();
		SampleRunnable runnable1=new SampleRunnable() {

			@Override
			public void run() {
				for(int i=0;i<100;i++) {
					System.out.println("running thread 2, count: "+i);
				}
			}
			
		};
		Thread thread1=new Thread(runnable, "join thread1");
		Thread thread2=new Thread(runnable1, "join thread2");
		thread1.start();
		try {
			thread1.join(); //block below lines till thread1 completes
		}catch(InterruptedException e) {
			System.err.println("Exception occured: "+e);
		}
		
		thread2.start();

		try {
			thread2.join(); //block below lines till thread2 completes
		}catch(InterruptedException e) {
			System.err.println("Exception occured: "+e);
		}
		System.out.println("counter: "+counter++);
		System.out.println("counter: "+counter++);
		System.out.println("counter: "+counter++);
		System.out.println("counter: "+counter++);
		
	}

}
