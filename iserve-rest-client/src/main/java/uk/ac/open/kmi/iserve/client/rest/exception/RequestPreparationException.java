package uk.ac.open.kmi.iserve.client.rest.exception;

public class RequestPreparationException extends Exception {

	private static final long serialVersionUID = -5405926088439927809L;

	public RequestPreparationException() {
	}

	public RequestPreparationException(String message) {
		super(message);
	}

	public RequestPreparationException(Throwable cause) {
		super(cause);
	}

	public RequestPreparationException(String message, Throwable cause) {
		super(message, cause);
	}

}
