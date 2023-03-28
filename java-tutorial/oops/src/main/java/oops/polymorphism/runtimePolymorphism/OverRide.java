package oops.polymorphism.runtimePolymorphism;
//override method is called as runtimepolymorphism
import oops.encapsulation.Book;
import oops.inheritance.multiple.Staff;

public class OverRide extends Staff{
	public OverRide(Book book) {
		super(book);
	}

	@Override
	public void read() {
		System.out.println("I am reading book: "+this.getBook().getName());
	}
	
	public static void main(String[] args) {
		OverRide ride=new OverRide(new Book(6,"Marvel"));
		ride.read();
	}
	
}
