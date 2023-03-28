package multipleThreading.blockingQueue.producerConsumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer implements Runnable
{
    private BlockingQueue<String> queue;

    public Consumer(BlockingQueue<String> queue)
    {
        this.queue = queue;
    }

    public void run()
    {
        synchronized (this)
        {
            System.out.println("Polling Started ,ThreadName: " + Thread.currentThread().getName());
            try
            {
                while (true)
                {
                    System.out.println("Queue output: [ " + queue.take() + " ]");
                }
//		System.out.println("Queue is emptied "+"ThreadName: "+Thread.currentThread().getName());
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(5);
        Consumer consumerRunnable = new Consumer(queue);
        Producer producerRunnable = new Producer(queue);
        ExecutorService service = Executors.newCachedThreadPool();
        for (int i = 0; i < 4; i++)
        {
            service.submit(producerRunnable);
        }
        for (int i = 0; i < 2; i++)
        {
            service.submit(consumerRunnable);
        }
        service.shutdown();
    }

}
