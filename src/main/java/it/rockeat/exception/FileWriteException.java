package it.rockeat.exception;

public class FileWriteException extends Exception {

	private static final long serialVersionUID = 1613430272520903664L;

	public FileWriteException() {
		super();
	}

	public FileWriteException(String message) {
		super(message);
	}

	public FileWriteException(Throwable cause) {
		super(cause);
	}

	public FileWriteException(String message, Throwable cause) {
		super(message, cause);
	}

}
