/*
 * File: ObjectByNameQuery.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.NonUniqueResultException;


/**
 * A special query which search a unqiue object for a given identifier. The identifier name can be customized by
 * subclasses. The identifier value should be passed as a parameter.
 * 
 * @author Wolfram Kaiser
 */
public abstract class ObjectByNameQuery<T extends IPersistable> extends BaseObjectQuery<T> {

	private String objectName;
		
	public ObjectByNameQuery(PersistenceManager persistenceManager, String newObjectName) {
		super( persistenceManager );
		setObjectName(newObjectName);
	}

	/**
	 * @see com.endress.infoserve.user.bo.datastore.BaseObjectQuery#performQuery()
	 */
	@SuppressWarnings("unchecked")
	public T performQuery() throws NonUniqueResultException {
		T foundObject = null;
		Criteria searchCriteria = createSearchCriteria();
		
		changeLoadOnDemand(searchCriteria);
		
		addDistinctClause(searchCriteria);
		
		try {
			foundObject = (T) searchCriteria.uniqueResult();
		}
		catch (HibernateException e) {
			// TODO: a HibernateException might have other causes than more than one result
			throw new NonUniqueResultException("Could not find exactly one entry for " + getObjectName(), e);
		}
		
		return foundObject;
	}

	protected Criteria createSearchCriteria() {
		Criteria searchCriteria = getDBSession().createCriteria(getSearchClass());
		searchCriteria.add(Restrictions.eq(getSearchFieldName(), getObjectName()));
		return searchCriteria;
	}

	protected abstract String getSearchFieldName();
	
	/**
	 * @return the objectName
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * @param newObjectName the objectName to set
	 */
	public void setObjectName(String newObjectName) {
		objectName = newObjectName;
	}
}
