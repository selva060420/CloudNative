package exceptionsHandling.CustomException;

public class MinimumBalanceException extends Exception {
	public MinimumBalanceException(String message, Throwable cause) {
		super(message, cause);
	}

	public MinimumBalanceException(String message) {
		super(message);
	}

	public MinimumBalanceException(Throwable cause) {
		super(cause);
	}
}
