package oops.solid.singleResponsibility;
//SingleResponsibiity- Each module shouldnt have more than one reason to change
//in OrderProcessing we supposed to process the orders
public class OrderProcessing {
	//main responsibility of the OrderProcessing module
	public void process() {
		//some code
	}
	//here save in db is not related to OrderProcessing module
	@Deprecated
	public void save() {
		//some code regarding save in db
	}
}
