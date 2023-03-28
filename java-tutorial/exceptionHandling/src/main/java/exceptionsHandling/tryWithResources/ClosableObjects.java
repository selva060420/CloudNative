package exceptionsHandling.tryWithResources;

import java.util.Scanner;

public class ClosableObjects {
	public static void tryWithResources() {
		try(Scanner scanner=new Scanner(System.in)){
			System.out.println("Enter the value for tryWithResources: ");
			int input=scanner.nextInt();
			System.out.println("Input value: "+input);
		}catch (Exception e) {
			System.err.println("Exception occured: "+e);
		}
	}
	public static void tryWithoutResources(){
		Scanner scanner=null;
		try{
			scanner=new Scanner(System.in);
			System.out.println("Enter the value for tryWithoutResources: ");
			int input=scanner.nextInt();
			System.out.println("Input value: "+input);
		}catch (Exception e) {
			System.err.println("Exception occured: "+e);
		}finally {
			scanner.close();
		}
	}
	public static void main(String[] args) {
		ClosableObjects.tryWithoutResources();
		ClosableObjects.tryWithResources();
	}
}
