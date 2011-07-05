package uk.ac.open.kmi.iserve.client.rest.exception;

public class UnsupportedMimeTypeException extends Exception {

	private static final long serialVersionUID = 2185001728735431851L;

	public UnsupportedMimeTypeException() {
	}

	public UnsupportedMimeTypeException(String message) {
		super(message);
	}

	public UnsupportedMimeTypeException(Throwable cause) {
		super(cause);
	}

	public UnsupportedMimeTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
