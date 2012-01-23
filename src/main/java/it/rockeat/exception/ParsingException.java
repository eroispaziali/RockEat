package it.rockeat.exception;

public class ParsingException extends Exception {

	private static final long serialVersionUID = -1760171023653737125L;

	public ParsingException() {
		super();
	}

	public ParsingException(String message) {
		super(message);
	}

	public ParsingException(Throwable cause) {
		super(cause);
	}

	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
