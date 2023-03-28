package Others.comparable;

public class Person implements Comparable<Person>
{
    private int age;
    private String name;
    private String gender;

    public Person(int age, String name, String gender)
    {
        super();
        this.age = age;
        this.name = name;
        this.gender = gender;
    }

    public Person(String name, int age)
    {
        super();
        this.age = age;
        this.name = name;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public String getName()
    {
        return name;
    }

    public String getGender()
    {
        return gender;
    }

    {
        this.gender = gender;
    }

//    @Override
//    protected void finalize() throws Throwable
//    {
//        System.out.println("Executing instructions before dying");
//    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int compareTo(Person other)
    {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString()
    {
        return "Person [age=" + age + ", name=" + name + ", gender=" + gender + "]";
    }

}
