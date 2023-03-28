package multipleThreading.blockingQueue.producerConsumer;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable
{
    private BlockingQueue<String> queue;

    public Producer(BlockingQueue<String> queue)
    {
        this.queue = queue;
    }

    public void run()
    {
        synchronized (this)
        {
            System.out.println("Producer updation starts ThreadName: " + Thread.currentThread().getName());
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    queue.put("index: " + i + " ThreadName: " + Thread.currentThread().getName());
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            System.out.println("producer updated successfully ThreadName: " + Thread.currentThread().getName());
        }
    }

}
