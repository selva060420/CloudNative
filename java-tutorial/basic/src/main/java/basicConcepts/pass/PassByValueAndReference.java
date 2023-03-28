package basicConcepts.pass;

import Others.Person;

public class PassByValueAndReference
{
    private Person person;

    public PassByValueAndReference()
    {
        person = new Person();
    }

    public void passByReference(Person person)
    {
        person.setAge(22);
        System.out.println("Persons age in passByReference method: " + person.getAge());
    }

    public void passByValue(int value)
    {
        value = 10;

        System.out.println("passByValue method Output: " + value);
    }

    public static void main(String[] args)
    {
        PassByValueAndReference obj = new PassByValueAndReference();
        int mainMethodValue = 5;
        obj.passByValue(mainMethodValue);
        System.out.println("Main method Output: " + mainMethodValue);

        // pass by refernce will be carried out by setter
        Person person = new Person();
        person.setAge(10);
        obj.passByReference(person);

        System.out.println("Persons age in main method: " + person.getAge());

    }

}
