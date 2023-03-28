package multipleThreading.joinWaitNotifyNotifyAll;

import Others.Person;

class Speaker implements Runnable
{
    private Person person;

    public Speaker(Person person)
    {
        this.person = person;
    }

    @Override
    public void run()
    {
        synchronized (person)
        {
            person.setName("Dummy");
            System.out.println("name setted for speaker, threadName: " + Thread.currentThread().getName());
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            person.notifyAll();
            System.out.println("Speaker notifies the listener" + " threadName: " + Thread.currentThread().getName());
        }
    }

}

class Listener implements Runnable
{
    private Person person;

    public Listener(Person person)
    {
        this.person = person;
    }

    @Override
    public void run()
    {
        synchronized (person)
        {
            System.out.println("waiting for speaker to finish, threadName: " + Thread.currentThread().getName());
            try
            {
                person.wait();
            }
            catch (InterruptedException e)
            {
                System.err.println("Exception occurs: " + e);
            }
            System.out.println("Listener got the name: " + person.getName() + " threadName: " + Thread.currentThread().getName());
        }
    }
}

public class WaitNotifyNotifyALL
{

    public static void main(String[] args)
    {
        Person person = new Person();
        Person.Address address = person.new Address();
        final Object lock = new Object();
        person.setName("selva");
        person.setAge(22);
        address.setCity("theni");
        address.setCountry("india");
        address.setDoorNo(221);
        address.setStreetName("mainroad");
        person.setAddress(address);
        Speaker speaker1 = new Speaker(person);
        Speaker speaker2 = new Speaker(person);
        Listener listener1 = new Listener(person);
        Listener listener2 = new Listener(person);
        Listener listener3 = new Listener(person);
        Listener listener4 = new Listener(person);
        Thread speakerThread1 = new Thread(speaker1, "Speaker1 thread");
        Thread speakerThread2 = new Thread(speaker2, "Speaker2 thread");
        Thread listener1Thread = new Thread(listener1, "listener1 Thread");
        Thread listener2Thread = new Thread(listener2, "listener2 Thread");
        Thread listener3Thread = new Thread(listener3, "listener3 Thread");
        Thread listener4Thread = new Thread(listener4, "listener4 Thread");
        listener1Thread.start();
        listener2Thread.start();
        listener3Thread.start();
        listener4Thread.start();
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        speakerThread1.start();
        speakerThread2.start();

    }
}
