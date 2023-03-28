package oops.inheritance.hybrid;

import Others.Person;
import oops.abstraction.interfaces.BookBuyer;
import oops.abstraction.interfaces.BookReader;
import oops.encapsulation.Book;

//student inherits BookBuyer,BookReader(multiple)
//student inherits Person(single)
public class Student extends Person implements BookBuyer, BookReader
{
    private String placeOfStudy;
    private String instutionName;
    private Book book;

    public Student(String placeOfStudy, String instutionName, Book book)
    {
        super();
        this.placeOfStudy = placeOfStudy;
        this.instutionName = instutionName;
        this.book = book;
    }

    @Override
    public void read()
    {
        System.out.println(super.getName() + " is reading " + book.getName());

    }

    @Override
    public void timeOfReading(int timeDuration)
    {
        System.out.println(super.getName() + "is reading " + book.getName() + " for 5 minutes");

    }

    @Override
    public void buy()
    {
        System.out.println(super.getName() + "is not Buying: " + book.getName());

    }

}
