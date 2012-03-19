package it.rockeat.exception;

public class LookupException extends Exception {

	private static final long serialVersionUID = -1760171023653737125L;

	public LookupException() {
		super();
	}

	public LookupException(String message) {
		super(message);
	}

	public LookupException(Throwable cause) {
		super(cause);
	}

	public LookupException(String message, Throwable cause) {
		super(message, cause);
	}

}
