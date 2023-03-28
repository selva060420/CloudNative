package collections.list.Synchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import multipleThreading.implementrunnable.SampleRunnable;

public class ListUseCases {

	public static void main(String[] args) {
		List<String> syncList1 = Collections.synchronizedList(new ArrayList<String>());
		CopyOnWriteArrayList<String> syncList2 = new CopyOnWriteArrayList<String>();
		List<String> nonSyncList=new ArrayList<String>();
		// similar methods will be as like asynchronous list
		SampleRunnable writerRunnable = new SampleRunnable() {
			@Override
			public void run() {
				for(int i=0;i<20;i++) {
					nonSyncList.add("index: "+i+" writer runnable, threadName: "+Thread.currentThread().getName());
					System.out.println("nonSyncList :"+nonSyncList);
					System.out.println("index: "+i+"Value added in nonSyncList , threadName: "+Thread.currentThread().getName());
				}
			}

		};

		SampleRunnable removerRunnable = new SampleRunnable() {
			@Override
			public void run() {
				for(int i=0;i<10;i++) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					nonSyncList.remove(i);
					System.out.println("nonSyncList :"+nonSyncList);
					System.out.println("index: "+i+"Value removed in nonSyncList , threadName: "+Thread.currentThread().getName());
				}
			}

		};
		
		ExecutorService service=Executors.newCachedThreadPool();
		service.submit(removerRunnable);
		service.submit(removerRunnable);
		service.submit(writerRunnable);
		service.submit(writerRunnable);
		service.submit(writerRunnable);
		service.submit(writerRunnable);
		service.shutdown();
	}

}
