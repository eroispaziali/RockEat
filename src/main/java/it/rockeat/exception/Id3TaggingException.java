package it.rockeat.exception;

public class Id3TaggingException extends Exception {

	private static final long serialVersionUID = 1843400911325998635L;

	public Id3TaggingException() {
		super();
	}

	public Id3TaggingException(String message) {
		super(message);
	}

	public Id3TaggingException(Throwable cause) {
		super(cause);
	}

	public Id3TaggingException(String message, Throwable cause) {
		super(message, cause);
	}

}
