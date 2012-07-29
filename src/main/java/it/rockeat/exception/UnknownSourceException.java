package it.rockeat.exception;

public class UnknownSourceException extends Exception {

	private static final long serialVersionUID = -133619635089781073L;

	public UnknownSourceException() {
		super();
	}

	public UnknownSourceException(String message) {
		super(message);
	}

	public UnknownSourceException(Throwable cause) {
		super(cause);
	}

	public UnknownSourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
