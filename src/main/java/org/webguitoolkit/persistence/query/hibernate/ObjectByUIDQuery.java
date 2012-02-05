/*
 * File: ObjectByUIDQuery.java
 * Copyright (c) 2008, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.IObjectQuery;
import org.webguitoolkit.persistence.query.NonUniqueResultException;

/**
 * New class to search for objects by the unique id
 * 
 * @author hermannm
 *
 * @param <T> the class that should be the result
 */
public class ObjectByUIDQuery<T extends IPersistable> extends BaseObjectQuery<T> implements IObjectQuery<T> {

	private long uid;
	private Class<? extends T> searchClass;
	
	public ObjectByUIDQuery(PersistenceManager pm, long newUId, Class<? extends T> newSearchClass) {
		super(pm);
		setUId(newUId);
		setSearchClass(newSearchClass);
	}

	@SuppressWarnings( value={"unchecked"} )
	public T performQuery() throws NonUniqueResultException {
		T foundObject = null;
		Criteria searchCriteria = createSearchCriteria();
		
		changeLoadOnDemand(searchCriteria);
		
		addDistinctClause(searchCriteria);
		
		try {
			foundObject = (T) searchCriteria.uniqueResult();
		}
		catch (HibernateException e) {
			throw new NonUniqueResultException("Could not find exactly one entry for " + getUId(), e);
		}
		
		return foundObject;
	}

	protected Criteria createSearchCriteria() {
		Criteria searchCriteria = getDBSession().createCriteria(getSearchClass());
		searchCriteria.add(Restrictions.eq(getSearchFieldName(), getUId()));
		return searchCriteria;
	}

	protected String getSearchFieldName() {
		return "objectUId";
	}
	
	protected Class<? extends T> getSearchClass() {
		return searchClass;
	}
	
	private void setSearchClass(Class<? extends T> newSearchClass) {
		searchClass = newSearchClass;
	}
	
	public long getUId() {
		return uid;
	}

	private void setUId(long newUId) {
		uid = newUId;
	}
}
