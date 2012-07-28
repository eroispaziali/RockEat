package it.rockeat.exception;

public class BackendException extends Exception {

	private static final long serialVersionUID = 2129019827648156635L;

	public BackendException() {
		super();
	}

	public BackendException(String message) {
		super(message);
	}

	public BackendException(Throwable cause) {
		super(cause);
	}

	public BackendException(String message, Throwable cause) {
		super(message, cause);
	}

}
