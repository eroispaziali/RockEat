package it.rockeat.exception;

public class FileSaveException extends Exception {

	private static final long serialVersionUID = 1613430272520903664L;

	public FileSaveException() {
		super();
	}

	public FileSaveException(String message) {
		super(message);
	}

	public FileSaveException(Throwable cause) {
		super(cause);
	}

	public FileSaveException(String message, Throwable cause) {
		super(message, cause);
	}

}
