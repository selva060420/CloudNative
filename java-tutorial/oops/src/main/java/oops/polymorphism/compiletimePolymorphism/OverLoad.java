package oops.polymorphism.compiletimePolymorphism;
//method Overloading is a compile time polymorphism
//here name methods are comes under compile time polymorphism
public class OverLoad {
	private String name;
	private int id;
	private int age;

	public OverLoad(String name) {
		this(name, 10);
	}

	public OverLoad(String name, int id) {
		this(name, id, 20);
	}

	public OverLoad(String name, int id, int age) {
		this.name = name;
		this.id = id;
		this.age = age;
	}
    
	@Override
	public String toString() {
		return "OverLoad [name=" + name + ", id=" + id + ", age=" + age + "]";
	}

	public void name(String name) {
		System.out.println("My name is : " + name);
	}

	public void name(String name, int id) {
		System.out.println("My name is :" + name + " and id: " + id);
	}

	public void name(String name, int id, int age) {
		System.out.println("My name is :" + " and id: " + id + " ,age: " + age);
	}
	public static void main(String[] args) {
		OverLoad one=new OverLoad("Selva");
		OverLoad two=new OverLoad("Selva", 6);
		OverLoad three=new OverLoad("Selva", 6, 22);
		System.out.println(one);
		System.out.println(two);
		System.out.println(three);
	}
}
