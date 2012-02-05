package org.webguitoolkit.persistence.query;

public class NonUniqueResultException extends Exception{
	private static final long serialVersionUID = 1L;

	public NonUniqueResultException() {
		super();
	}

	public NonUniqueResultException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonUniqueResultException(String message) {
		super(message);
	}

	public NonUniqueResultException(Throwable cause) {
		super(cause);
	}
}
