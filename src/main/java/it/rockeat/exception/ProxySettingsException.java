package it.rockeat.exception;

public class ProxySettingsException extends Exception {

	private static final long serialVersionUID = 367975598351484465L;

	public ProxySettingsException() {
		super();
	}

	public ProxySettingsException(String message) {
		super(message);
	}

	public ProxySettingsException(Throwable cause) {
		super(cause);
	}

	public ProxySettingsException(String message, Throwable cause) {
		super(message, cause);
	}

}
