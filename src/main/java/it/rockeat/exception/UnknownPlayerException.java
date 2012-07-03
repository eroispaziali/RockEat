package it.rockeat.exception;

public class UnknownPlayerException extends Exception {

	private static final long serialVersionUID = 7498871704061184682L;

	public UnknownPlayerException() {
		super();
	}

	public UnknownPlayerException(String message) {
		super(message);
	}

	public UnknownPlayerException(Throwable cause) {
		super(cause);
	}

	public UnknownPlayerException(String message, Throwable cause) {
		super(message, cause);
	}

}
