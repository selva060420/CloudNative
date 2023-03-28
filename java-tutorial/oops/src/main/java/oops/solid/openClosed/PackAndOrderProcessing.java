package oops.solid.openClosed;
//Openandclosed -- It means that software entities(class,object,method) will be open for extension and closed for Modification
//here for additional method packing we are creating a new class PackAndOrderProcessing for extension ,without modifying OrderProcessing
import oops.solid.singleResponsibility.OrderProcessing;

public class PackAndOrderProcessing extends OrderProcessing{
	public void  packing() {
		System.out.println("Packed");
	}

	@Override
	public void process() {
		this.packing();
		super.process();
	}
	
}
