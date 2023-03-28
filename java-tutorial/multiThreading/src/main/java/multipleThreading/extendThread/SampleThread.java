package multipleThreading.extendThread;

public class SampleThread extends Thread {
	private static final int n=5;
	@Override
	public void run() {
		for(int i=1;i<=5;i++) {
			System.out.println("5th table: "+(5*i)+" daemon or not: "+this.isDaemon());
		}
	}
	public static void main(String[] args) {
		SampleThread thread1=new SampleThread();
		thread1.setDaemon(true);
		thread1.start();
		SampleThread thread2=new SampleThread();
		thread2.start();
		SampleThread thread3=new SampleThread();
		thread3.start();
	}

}
