package Others.BlockingQueue;

public class DeadLock
{
    Object lock1 = new Object();
    Object lock2 = new Object();

    public void run()
    {
        synchronized (lock1)
        {
            //
            synchronized (lock2)
            {

            }
        }
    }

    public static void main(String[] args)
    {

    }

}
