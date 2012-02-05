/*
 * File: DatabaseProvider.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.util;

import org.webguitoolkit.persistence.PersistenceManager;

/**
 * A DatabaseProvider bundles a ModelFactory and a DB session. Classes which depend on these information may inherit from
 * this class.
 * 
 * @author Wolfram Kaiser
 */
public abstract class DatabaseProvider {

	
	private PersistenceManager persistenceManager;

	/**
	 * Required for PersistenceFramework2 compatibility - shouldn't break PersistenceFramework1 API usage
	 * 
	 * @param persistenceManager
	 */
	public DatabaseProvider( PersistenceManager persistenceManager ) {
		this.persistenceManager = persistenceManager;
	}
	
	public PersistenceManager getPersistenceManager(){
		if ( persistenceManager == null ) {
			throw new RuntimeException("PersistenceManager not initialized! May be you are useing the depricated Constructor!");
		}
		return persistenceManager;
	}
}
