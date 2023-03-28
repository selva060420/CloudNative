package Others.BlockingQueue;

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
        System.out.println("Producing queue values ,ThreadName: " + Thread.currentThread().getName());
        try
        {
            for (int i = 0; i < 2; i++)
            {
                queue.put("index: " + i);
            }
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Producing queue added ,ThreadName: " + Thread.currentThread().getName());
    }
}
