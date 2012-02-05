/*
 * File: DefaultLastDateQuery.java
 * Copyright (c) 2007, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;


/**
 * If only a simple max(date) query is required then this default implementation can be customized to
 * search for a certain date field in a table/given class.
 * 
 * @author Wolfram Kaiser
 */
public class DefaultLastDateQuery<T extends IPersistable> extends BaseLastDateQuery<T> {

	private Class<? extends T> searchClass;
	private String dateField;
	
	/**
	 * @param newPersistenceManager
	 */
	public DefaultLastDateQuery(PersistenceManager newPersistenceManager, Class<? extends T> newSearchClass, String newDateField) {
		super(newPersistenceManager);
		setSearchClass(newSearchClass);
		setDateField(newDateField);
	}

	/**
	 * @see com.endress.infoserve.persistence.queries.BaseLastDateQuery#getDateField()
	 */
	protected String getDateField() {
		return dateField;
	}

	/**
	 * @see com.endress.infoserve.persistence.queries.BaseLastDateQuery#getSearchClass()
	 */
	protected Class<? extends T> getSearchClass() {
		return searchClass;
	}

	/**
	 * @param newDateField the dateField to set
	 */
	protected void setDateField(String newDateField) {
		dateField = newDateField;
	}

	/**
	 * @param newSearchClass the searchClass to set
	 */
	protected void setSearchClass(Class<? extends T> newSearchClass) {
		searchClass = newSearchClass;
	}

}
