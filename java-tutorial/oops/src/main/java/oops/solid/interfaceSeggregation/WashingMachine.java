package oops.solid.interfaceSeggregation;
//here creating separate interfaces for all the actions of washing machine 
//interface, instead of declaring all actions in a single interface

//Not advisable because if a class want to implement only WashingMachine interface remaining methods 
//will not  be us
 public interface WashingMachine {
	public void wash();
	public void rinse();
	public void spin();
	public void dry();
}
 interface WashingMachineWasher{
	 public void wash();
 }
 interface WashingMachineRinser{
	 public void rinse();
 }
 interface WashingMachineSpinner{
	 public void spin();
 }
 interface WashingMachineDryer{
	 public void dry();
 }