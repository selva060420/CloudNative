package multipleThreading.countDownLatch;

import java.util.concurrent.CountDownLatch;

public class LatchCount implements Runnable
{
    private CountDownLatch latch;

//	final  Object lock=new Object();
    public LatchCount(CountDownLatch latch)
    {
        this.latch = latch;
    }

    public void run()
    {
        synchronized (this)
        {
            System.out.println("previous count: " + latch.getCount() + " count method starts , threadName: "
                    + Thread.currentThread().getName());
            latch.countDown();
            System.out.println("After count: " + latch.getCount() + " count method ends, threadName: "
                    + Thread.currentThread().getName());
        }
    }
}
