package Others.comparable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComparableVsComparatorTest
{
    public static void main(String[] args)
    {
        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 25));
        people.add(new Person("Bob", 30));
        people.add(new Person("Charlie", 20));
//        Person class implements Comparable<Person> i.e. a pojo class which provides a natural ordering
//         refer to Person class toCompare method for natural ordering which compares based on person name
//        Collections.sort(people);
        // But we can override the natural ordering by creating a different class which implements Comparator<Person>
        // override by passing agecomparator class that compares different person age.
        Collections.sort(people, new AgeComparator());
        System.out.println(people);
    }
}

class AgeComparator implements Comparator<Person>
{
    @Override
    public int compare(Person p1, Person p2)
    {
        return p1.getAge() - p2.getAge();
    }
}
