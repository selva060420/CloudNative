package multipleThreading.Synchronized.deadLock;

public class deadLock {

	public static void main(String[] args) {
		Object lock1=new Object();
		Object lock2=new Object();
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized(lock1) {
					System.out.println(Thread.currentThread().getName()+" acquired lock1");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					synchronized (lock2) {
						System.out.println(Thread.currentThread().getName()+" acquired lock2");
					}
				}
				
			}
			
		}, "First thread").start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized(lock2) {
					System.out.println(Thread.currentThread().getName()+" acquired lock2");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					synchronized (lock1) {
						System.out.println(Thread.currentThread().getName()+" acquired lock1");
					}
				}
				
			}
			
		}, "second thread").start();
	}

}

