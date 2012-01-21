package it.rockeat.exception;

public class ConnectionException extends Exception {

	private static final long serialVersionUID = 1613430272520903664L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
