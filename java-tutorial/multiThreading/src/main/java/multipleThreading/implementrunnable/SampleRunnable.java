package multipleThreading.implementrunnable;

import java.util.concurrent.CountDownLatch;

import Others.Person;

public class SampleRunnable implements Runnable
{
    private static final int n = 5;
    private CountDownLatch latch;
    private Person person;

    public SampleRunnable()
    {

    }

    public SampleRunnable(Person person)
    {
        this.person = person;
    }

    public SampleRunnable(CountDownLatch latch)
    {
        this.latch = latch;
    }

    public Person getPerson()
    {
        return person;
    }

    @Override
    public void run()
    {
        try
        {
            for (int i = 1; i <= 5; i++)
            {
                System.out.println("5th table: " + (n * i) + " current thread is: " + Thread.currentThread().getName());
                Thread.sleep(500);
            }
        }
        catch (InterruptedException e)
        {
            System.err.println("Exception occured: " + e);
        }
    }

    public static void main(String[] args)
    {
        SampleRunnable runnable = new SampleRunnable();
        Thread thread = new Thread(runnable, "selva");
        thread.start();
        Thread thread1 = new Thread(runnable, "kumar");
        thread1.start();
        Thread thread2 = new Thread(runnable, "kishore");
        thread2.start();

    }
}
