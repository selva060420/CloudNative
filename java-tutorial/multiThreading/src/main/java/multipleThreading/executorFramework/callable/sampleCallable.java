package multipleThreading.executorFramework.callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class sampleCallable implements Callable<String> {

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		return "callable out";
	}
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		sampleCallable obj=new sampleCallable();
		ExecutorService ex=Executors.newSingleThreadExecutor();
		Future<String> future= ex.submit(obj);
		System.out.println("future: "+future.get());//outputs the return statement of callable method
		ex.shutdown();
	}

}
