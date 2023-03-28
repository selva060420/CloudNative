package multipleThreading.Synchronized.block.lock.object;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import oops.encapsulation.Book;

class Reader implements Runnable {
	private Book book;

	public Reader(Book book) {
		this.book = book;
	}

	@Override
	public void run() {
		synchronized (book) {
			System.out.println("Reading book Content, threadName " + Thread.currentThread().getName());
			System.out.println("List Content: " + book.getContent().toString());
		}
	}
}

class Writer implements Runnable {
	private Book book;
	private static int count = 1;

	public Writer(Book book) {
		this.book = book;
	}

	@Override
	public void run() {
		synchronized (book) {
			System.out.println("Adding Book content, threadName: " + Thread.currentThread().getName());
			for (int i = 1; i < 11; i++) {
				book.getContent().add("PageNo: " + (count++) + ", threadName: " + Thread.currentThread().getName());
			}
			System.out.println(" Content added, threadName: " + Thread.currentThread().getName());
		}
	}

}

public class ObjectLevelLocker {
	public static void instantiate() {
		Book book = new Book();
		book.setContent(new ArrayList<String>());
		Writer writerRunnable = new Writer(book);
		Reader readerRunnable = new Reader(book);
		ExecutorService service = Executors.newFixedThreadPool(12);
		for (int i = 0; i < 4; i++) {
			service.submit(readerRunnable);
			service.submit(readerRunnable);
			service.submit(writerRunnable);
		}
		service.shutdown();

//		for(int i=1;i<=4;i++) {
//			int j=4+i;
//			new Thread(writerRunnable,"writer thread "+i).start();
//			new Thread(readerRunnable,"reader thread "+i).start();
//			new Thread(readerRunnable,"reader thread "+j).start();
//		}
	}

	public static void main(String[] args) {
		ObjectLevelLocker.instantiate();
	}

}
