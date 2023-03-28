package oops.inheritance.single;

import oops.encapsulation.Book;
// ComicBook inherits Book
public class ComicBook extends Book{
	private String type;
	
	public ComicBook(String type) {
		super();
		this.type=type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ComicBook [type=" + type + "]";
	}

	@Override
	public void typeOfBook() {
		// TODO Auto-generated method stub
		System.out.println("Type of book: "+type);
	}
}
