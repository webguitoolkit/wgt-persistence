/*
 * File: DatabaseSetupException.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.util;

/**
 * @author Wolfram Kaiser
 *
 */
public class DatabaseSetupException extends Exception {

	private static final long serialVersionUID = -4812743911881251478L;

	/**
	 * 
	 */
	public DatabaseSetupException() {
	}

	/**
	 * @param message
	 */
	public DatabaseSetupException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DatabaseSetupException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DatabaseSetupException(String message, Throwable cause) {
		super(message, cause);
	}

}
