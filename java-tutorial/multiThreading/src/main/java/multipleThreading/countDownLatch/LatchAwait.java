package multipleThreading.countDownLatch;

import java.util.concurrent.CountDownLatch;

//dont use latch for locking which will leads to dead lock due to wait
public class LatchAwait implements Runnable
{
    private CountDownLatch latch;

//	final Object lock=new Object();//we can also use dummy lock sunc block
    public LatchAwait(CountDownLatch latch)
    {
        this.latch = latch;
    }

    public void run()
    {
        synchronized (this)
        {
            System.out.println("await method waiting , threadName: " + Thread.currentThread().getName());
            try
            {
                latch.await();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("await method executed, threadName: " + Thread.currentThread().getName());
        }
    }
}
