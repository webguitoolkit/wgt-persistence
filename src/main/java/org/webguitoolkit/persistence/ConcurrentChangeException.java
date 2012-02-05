package org.webguitoolkit.persistence;

/**
 * This Exception will be thrown if the PersistenceManager detects during commit, 
 * rollback, refresh that an object has been modified while the application is working on it.
 * 
 * @author peter.zaretzke@infoserve.endress.com
 */


public class ConcurrentChangeException extends PersistenceException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Exception exception;

	public ConcurrentChangeException(String msg, Exception root) {
		super(msg);
		exception = root;
	}

	public Exception getException() {
		return exception;
	}

}
