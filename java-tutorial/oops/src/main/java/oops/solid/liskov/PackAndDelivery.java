package oops.solid.liskov;

import oops.solid.openClosed.PackAndOrderProcessing;

//liskov --> variation of openClosed principle
//objects can be replaced by objects of subclasses without compromising functionalites(without breaking the normal flow)

public class PackAndDelivery extends PackAndOrderProcessing{

	@Override
	public void packing()  {
		try {
			//some code
			throw new Exception("packing unsuccessfull");
		}catch(Exception e) {
			System.out.println(e);
		}
	}

}
