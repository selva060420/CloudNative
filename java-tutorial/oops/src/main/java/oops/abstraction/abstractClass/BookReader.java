package oops.abstraction.abstractClass;
//Abstraction will allow zero to hundred abstraction
public abstract class BookReader {
	//Zero abstraction
	public abstract void read();
	//hundred abstraction
	public void timeOfReading(int timeDuration) {
		System.out.println("Student reading for: "+ timeDuration);
	}
}
