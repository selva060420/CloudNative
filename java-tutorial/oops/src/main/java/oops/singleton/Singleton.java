package oops.singleton;
//It is a class that has only one instance and global point of access to it.

class EarlySingleton
{

    // Early instantiation
    private static EarlySingleton instance = new EarlySingleton();

    // private constructor
    private EarlySingleton()
    {
    }

    // public method to get the instance
    public static EarlySingleton getInstance()
    {
        return instance;
    }

    public void method()
    {
        System.out.println("I am Early Singleton");
    }

}

class LazySingleton
{

    // Lazy instantiation
    private static LazySingleton instance;

    // private constructor
    private LazySingleton()
    {
    }

// public method to get the instance, creates the instance if it hasn't been created yet
    public static LazySingleton getInstance()
    {
        if (instance == null)
        {
            instance = new LazySingleton();
        }
        return instance;
    }

    public void method()
    {
        System.out.println("I am Lazy Singleton");
    }
}

public class Singleton
{
    public static void main(String[] args)
    {
        EarlySingleton early = EarlySingleton.getInstance();
        LazySingleton lazy = LazySingleton.getInstance();
        early.method();
        lazy.method();
    }
}
