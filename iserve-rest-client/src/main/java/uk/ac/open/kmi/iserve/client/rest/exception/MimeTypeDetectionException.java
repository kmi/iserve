package uk.ac.open.kmi.iserve.client.rest.exception;

public class MimeTypeDetectionException extends Exception {

	private static final long serialVersionUID = 8258314980859154921L;

	public MimeTypeDetectionException() {
	}

	public MimeTypeDetectionException(String message) {
		super(message);
	}

	public MimeTypeDetectionException(Throwable cause) {
		super(cause);
	}

	public MimeTypeDetectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
