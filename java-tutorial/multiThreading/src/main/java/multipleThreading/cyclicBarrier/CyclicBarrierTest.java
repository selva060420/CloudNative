package multipleThreading.cyclicBarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CyclicBarrierTest
{

    public static void main(String[] args) throws InterruptedException
    {

        CyclicBarrier barrier = new CyclicBarrier(3, new BarrierAction()); // Set the number of threads to 3
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++)
        {
            service.submit(new MyRunnable(barrier));
        }
        service.shutdown();
    }
}

class MyRunnable implements Runnable
{

    private final CyclicBarrier barrier;

    public MyRunnable(CyclicBarrier barrier)
    {
        this.barrier = barrier;
    }

    @Override
    public void run()
    {
        System.out.println("before await: " + Thread.currentThread().getName());
        try
        {
            barrier.await();
        }
        catch (InterruptedException | BrokenBarrierException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Wait for all other threads to reach the barrier
        System.out.println("after await: " + Thread.currentThread().getName());
    }
}

class BarrierAction implements Runnable
{
    @Override
    public void run()
    {
        // Wait for all other threads to reach the barrier
        System.out.println("All threads have reached the barrier: " + Thread.currentThread().getName());
    }
}
