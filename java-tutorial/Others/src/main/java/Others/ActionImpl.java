package Others;

import java.util.concurrent.CountDownLatch;

public class ActionImpl implements Runnable
{
    private CountDownLatch latch;

    public ActionImpl(CountDownLatch latch)
    {
        this.latch = latch;
    }

    public void run()
    {
        synchronized (ActionImpl.class)
        {
            System.out.println("kishore  is very bad");
            System.out.println("count is: " + latch.getCount());
            latch.countDown();
            System.out.println(" after count is: " + latch.getCount());
        }
    }

}
