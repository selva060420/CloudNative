package Others.BlockingQueue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class BlockingQueueImplementation
{
    private Queue<String> queue;
    private Semaphore semaphore;

    public BlockingQueueImplementation(int size)
    {
        queue = new LinkedList<String>();
        semaphore = new Semaphore(size);
    }

    public void putMethod(String string)
    {
        try
        {
            System.out.println("Acquiring value in Queue: " + " ThreadName: " + Thread.currentThread().getName());
            semaphore.acquire();
            queue.offer(string);
            System.out.println("Acquired value in Queue: " + string + " ThreadName: " + Thread.currentThread().getName());
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void takeMethod()
    {
        System.out.println("Releasing value in Queue: " + " ThreadName: " + Thread.currentThread().getName());
        String string = queue.poll();
        semaphore.release();
        System.out.println("Released value in Queue: " + string + " ThreadName: " + Thread.currentThread().getName());
    }

    public static void main(String[] args)
    {
        BlockingQueueImplementation queue = new BlockingQueueImplementation(4);

    }

}
