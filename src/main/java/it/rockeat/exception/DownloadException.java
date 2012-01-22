package it.rockeat.exception;

public class DownloadException extends Exception {

	private static final long serialVersionUID = -6633924647276685344L;

	public DownloadException() {
		super();
	}

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(Throwable cause) {
		super(cause);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
