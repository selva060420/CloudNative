package multipleThreading.exchanger;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import oops.encapsulation.Book;

public class ExchangerDemo {

	public static void main(String[] args) {
	    Exchanger exchanger =new Exchanger();
		ExchangerThread selvaRunnable=new ExchangerThread(exchanger) {
		 	@Override
			public void run() {
				Book selvaBook=new Book(1,"selvabook");
				System.out.println("before exchanging in selva thread: "+selvaBook);
				try {
					selvaBook=(Book) exchanger.exchange(selvaBook);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("After exchanging in selva thread: "+selvaBook);
			}
		};
		ExchangerThread kishoreRunnable=new ExchangerThread(exchanger) {
		 	@Override
			public void run() {
		 		Book kishoreBook=new Book(2,"kishorebook");
				System.out.println("before exchanging in kishore thread: "+kishoreBook);
				try {
					kishoreBook=(Book) exchanger.exchange(kishoreBook);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("After exchanging in kishore thread: "+kishoreBook);
			}
		};
		ExecutorService service=Executors.newCachedThreadPool();
		service.submit(selvaRunnable);
		service.submit(kishoreRunnable);
		service.shutdown();
	}

}

class ExchangerThread implements Runnable{
	private Exchanger exchanger;
	public ExchangerThread(Exchanger exchanger) {
		this.exchanger=exchanger;
	}
 	@Override
	public void run() {
		//no code
	}
}


