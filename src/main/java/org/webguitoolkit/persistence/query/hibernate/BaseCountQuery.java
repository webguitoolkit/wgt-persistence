/*
 * File: BaseCountQuery.java
 * Copyright (c) 2007, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.ICountQuery;


/**
 * @author Wolfram Kaiser
 *
 */
public abstract class BaseCountQuery<T extends IPersistable> extends BaseHibernateQuery<T> implements ICountQuery<T> {

	/**
	 * 
	 */
	public BaseCountQuery(PersistenceManager persistenceManager ) {
		super( persistenceManager );
	}

	/**
	 * @see org.webguitoolkit.persistence.query.ICountQuery.IBaseCountQuery#execute()
	 */
	public long execute() {
		if (isParametersComplete()) {
			Criteria criteria = createSearchCriteria();
			formProjection(criteria);
			return ((Long)criteria.list().get(0)).longValue();
		}
		else {
			return 0;
		}
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
		criteria.setProjection(Projections.rowCount());
	}

	protected Criteria createSearchCriteria() {
		return getDBSession().createCriteria(getSearchClass());
	}
}
