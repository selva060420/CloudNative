package oops.inheritance.multilevel;

import oops.encapsulation.Book;
import oops.inheritance.hybrid.Student;
// CollegeStudent inherits  Student inherits Person
public class CollegeStudent extends Student  {
	
	private Book book;
	public CollegeStudent(String placeOfStudy, String instutionName, Book book) {
		super(placeOfStudy, instutionName, book);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void read() {
		System.out.println(super.getName()+" is reading "+book.getName()+" College");
		
	}

	@Override
	public void timeOfReading(int timeDuration) {
		System.out.println(super.getName()+"is reading "+book.getName()+" for 5 minutes"+" College");
		
	}

	@Override
	public void buy() {
		System.out.println(super.getName()+"is not Buying: "+ book.getName()+" College");
		
	}

}
