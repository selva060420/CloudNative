package Others.BlockingQueue;

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

    @Override
    public void run()
    {
        System.out.println("Consuming queue values ,ThreadName: " + Thread.currentThread().getName());
        try
        {
            queue.take();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Consumed queue added ,ThreadName: " + Thread.currentThread().getName());
    }

    public static void main(String[] args)
    {
        BlockingQueue<String> queue = new ArrayBlockingQueue(3);
        Producer producerRunnable = new Producer(queue);
        Consumer consumerRunnable = new Consumer(queue);
        ExecutorService service = Executors.newCachedThreadPool();
//		service.submit(producerRunnable);
//		service.submit(producerRunnable);
        service.submit(consumerRunnable);
//		service.submit(consumerRunnable);
        service.shutdown();
    }
}
