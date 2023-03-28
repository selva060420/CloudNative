package Others;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class SampleThread
{
    public static void main(String args[]) throws InterruptedException, ExecutionException, TimeoutException
    {

        System.out.println("Main thread execution starts");
        CountDownLatch latch = new CountDownLatch(3);
        ActionImpl one = new ActionImpl(latch);
        ActionImpl two = new ActionImpl(latch);
        ActionImpl three = new ActionImpl(latch);
//		FutureTask<String> task = new FutureTask(one);
        ExecutorService service = Executors.newFixedThreadPool(3);
        service.submit(one);
        service.submit(two);
        service.submit(three);
//		System.out.println("future get: "+task.get());
        latch.await();
        System.out.println("Main thread execution ends");
        System.out.println("Main thread execution ends 1 ");
        service.shutdown();
        System.out.println("Main thread execution ends 2 ");
        System.out.println("Main thread execution ends 3  ");
    }
}
