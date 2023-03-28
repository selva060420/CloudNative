package oops.inheritance.multiple;

import oops.abstraction.interfaces.BookBuyer;
import oops.abstraction.interfaces.BookReader;
import oops.encapsulation.Book;
// staff inherits BookBuyer,BookReader
public class Staff implements BookBuyer, BookReader {
	private Book book;

	public Staff(Book book) {
		this.book = book;
	}
    public Book getBook() {
    	return book;
    }
	@Override
	public void read() {
		System.out.println("Staff reading " + book.getName());
	}

	@Override
	public void timeOfReading(int timeDuration) {
		System.out.println("Staff reading " + book.getName()+" for 5 minutes");
	}

	@Override
	public void buy() {
		System.out.println("Staff buying " + book.getName());
	}

}
