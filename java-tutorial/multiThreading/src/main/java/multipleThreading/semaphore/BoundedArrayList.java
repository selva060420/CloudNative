package multipleThreading.semaphore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import multipleThreading.implementrunnable.SampleRunnable;

public class BoundedArrayList {
	private List<String> list;
	private Semaphore semaphore;
	public BoundedArrayList(int size) {
		list=new ArrayList<String>();
		semaphore=new Semaphore(size);
	}
	public boolean add(String value) {
		boolean  isAdded=false;
		try {
			System.out.println("Going to Acquire permit"+" value: "+value);
			semaphore.acquire();
			System.out.println("Permit Acquired"+" value: "+value);
			isAdded=list.add(value);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return isAdded;
	}
	public boolean remove(String value) {
		boolean isRemoved=list.remove(value);
		System.out.println("Going to release permit"+" value: "+value);
		if(isRemoved) {
			semaphore.release();
			System.out.println("Permit released"+" value: "+value);
		}
		return isRemoved;
	}
	public List<String> getList(){
		return list;
	}
	public static void main(String[] args) {
		BoundedArrayList list =new BoundedArrayList(3);
		System.out.println("List: "+list.getList());
		SampleRunnable adderRunnable=new SampleRunnable() {

			@Override
			public void run() {
				System.out.println("Adding values in thread: "+Thread.currentThread().getName());
				list.add("selva1");
				list.add("selva2");
				list.add("selva3");
				list.add("selva4");
				System.out.println("List: "+list.getList());
			}
			
		};
		SampleRunnable removerRunnable=new SampleRunnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("removing values in thread: "+Thread.currentThread().getName());
				list.remove("selva1");
				list.remove("selva2");
				System.out.println("List: "+list.getList());
			}
			
		};
		ExecutorService service=Executors.newCachedThreadPool();
		service.submit(adderRunnable);
		service.submit(removerRunnable);
		service.shutdown();
	}
}
