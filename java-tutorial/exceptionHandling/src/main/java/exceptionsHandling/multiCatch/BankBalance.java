package exceptionsHandling.multiCatch;

import exceptionsHandling.CustomException.MinimumBalanceException;

public class BankBalance {
	private Integer balance;
	private static final int MINBALANCE = 1000;

	public BankBalance(Integer balance) {
		this.balance = balance;
	}

	public void process() {
		try {
			if (balance == null) {
				throw new NullPointerException("Balance is null");
			} else if (balance >= MINBALANCE) {
				System.out.println("Minimum balance maintained");
			} else {
				throw new MinimumBalanceException("minimum balance not maintained");
			}
		} catch (MinimumBalanceException | NullPointerException e) {
			System.err.println("Exception occured: " + e);
		}
	}

	public static void main(String[] args) {
		BankBalance obj = new BankBalance(null);
		obj.process();
	}

}
