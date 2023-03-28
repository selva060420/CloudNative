package oops.anonymous;

//Annonymous class is inner class ,which doesnt have name
//here Objects have created for a specific tasks.
public class AnnonymousClass {
	public  void annonymousMethod() {
		System.out.println("This is annonymous class");
	}
	public static void main(String args[]) {
		AnnonymousClass obj= new AnnonymousClass() {
			@Override
			public   void annonymousMethod() {
				System.out.println("This is main annonymous class");
			}
		};
		obj.annonymousMethod();
	}
}