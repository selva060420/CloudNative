package basicConcepts.Static;

public class StaticUseCases
{
    static class staticClass
    {
        public void sampleMethod()
        {
            System.out.println("Inner class with static keyword");
        }
    }

    class nonstaticClass
    {
        public void sampleMethod()
        {
            System.out.println("Inner class without static keyword");
        }
    }

    private static int staticCounter = 6;
    private int nonstaticCounter = 6;

    public static void staticincrement()
    {
        staticCounter++;
        System.out.println("Static value: " + staticCounter);
    }

    public void nonstaticincrement()
    {
        nonstaticCounter++;
        System.out.println("nonStatic value: " + nonstaticCounter + " " + this);
    }

    static
    {
        int number = 1;
        System.out.println("number in static block: " + number);
    }

    public static void main(String[] args)
    {
        StaticUseCases.staticincrement();
        StaticUseCases.staticincrement();
        StaticUseCases.staticincrement();
        StaticUseCases obj1 = new StaticUseCases();
        StaticUseCases obj2 = new StaticUseCases();
        StaticUseCases obj3 = new StaticUseCases();
        obj1.nonstaticincrement();
        obj2.nonstaticincrement();
        obj3.nonstaticincrement();
        StaticUseCases.staticClass ob1 = new StaticUseCases.staticClass();
        ob1.sampleMethod();
        StaticUseCases.nonstaticClass ob2 = obj1.new nonstaticClass();
        ob2.sampleMethod();
    }

}
