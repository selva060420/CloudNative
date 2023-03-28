package oops.encapsulation;

import java.util.List;

// Encapsulation is a helpful for data-binding, datas in encapsulation will be accessed by getters and setter and constructor.
public class Book {
	private String name;
	private int id;
	private List<String> content;
	
	public Book(int id,String name){
		this.id=id;
		this.name=name;
	}
	
	public Book() {
	}
	
	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	
	public void setId(int id) {
		this.id=id;
	}
	
	
	@Override
	public String toString() {
		return "Book name: "+name +" Book id: "+id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public void typeOfBook() {
		System.out.println("type of book want to overrided");
	}

	
}
