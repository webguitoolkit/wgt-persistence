/*
 * File: BaseLastDateQuery.java
 * Copyright (c) 2007, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.ILastDateQuery;


/**
 * @author Wolfram Kaiser
 *
 */
public abstract class BaseLastDateQuery<T extends IPersistable> extends BaseHibernateQuery<T> implements ILastDateQuery<T> {

	/**
	 * @param newPersistenceManager
	 */
	public BaseLastDateQuery(PersistenceManager newPersistenceManager) {
		super(newPersistenceManager);
	}

	/**
	 * @see org.webguitoolkit.persistence.query.ICountQuery.IBaseCountQuery#execute()
	 */
	public Date execute() {
		Criteria criteria = createSearchCriteria();
		formProjection(criteria);
		
		Object foundResult = criteria.list().get(0);
		Date foundLastDate = null;
		if (foundResult != null) {
			foundLastDate = (Date)foundResult;
		}
		
		return foundLastDate;
	}

	/**
	 * @return
	 */
	protected boolean isParametersComplete() {
		return true;
	}

	/**
	 * @param criteria
	 */
	protected void formProjection(Criteria criteria) {
		criteria.setProjection(Projections.max(getDateField()));
	}

	protected Criteria createSearchCriteria() {
		return getDBSession().createCriteria(getSearchClass());
	}
	
	/**
	 * @return
	 */
	protected abstract String getDateField();
}
