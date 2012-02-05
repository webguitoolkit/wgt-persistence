package org.webguitoolkit.persistence;


public class PersistenceException extends Exception {

	private static final long serialVersionUID = 220935215560448914L;

	public PersistenceException(String msg) {
		super( msg );
	}

	public PersistenceException(Exception e) {
		super( e );
	}

}
