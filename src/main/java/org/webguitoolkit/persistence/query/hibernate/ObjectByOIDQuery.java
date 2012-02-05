/*
 * File: ObjectByNameQuery.java
 * Copyright (c) 2008, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.NonUniqueResultException;


/**
 * A special query which search a unqiue object for a given oid or loing value The identifier id can be customized by
 * subclasses. The identifier value should be passed as a parameter.
 * 
 * @author Wolfram Kaiser & Lars Brößler
 */
public abstract class ObjectByOIDQuery<T extends IPersistable> extends BaseObjectQuery<T> {

	
	private Long objectId;
	
	public ObjectByOIDQuery(PersistenceManager persistenceManager,  Long newobjectId) {
		super( persistenceManager );
		setObjectId(newobjectId);
	}
	
	public ObjectByOIDQuery(PersistenceManager persistenceManager,  long newobjectId) {
		super( persistenceManager );
		setObjectId(new Long(newobjectId));
	}
	
	public ObjectByOIDQuery(PersistenceManager persistenceManager,  String newobjectId) {
		super( persistenceManager );
		setObjectId(new Long(newobjectId));
	}
	
	public ObjectByOIDQuery(PersistenceManager persistenceManager) {
		super( persistenceManager );
	
	}	
	
 
	/**
	 * @see com.endress.infoserve.user.bo.datastore.BaseObjectQuery#performQuery()
	 */
	@SuppressWarnings("unchecked")
	public T performQuery() throws NonUniqueResultException {
		T foundObject = null;
		Criteria searchCriteria = createSearchCriteria();
		
		changeLoadOnDemand();
		
		addDistinctClause(searchCriteria);
		
		List<T> foundObjects = searchCriteria.list();

		if (foundObjects.size() > 1) {
			throw new NonUniqueResultException("Could not find exactly one entry for " + getObjectId());
		}

		Iterator<T> foundObjectsIterator = foundObjects.iterator();
		if (foundObjectsIterator.hasNext()) {
			foundObject = foundObjectsIterator.next();
		}
		
		return foundObject;
	}

	protected Criteria createSearchCriteria() {
		Criteria searchCriteria = getDBSession().createCriteria(getSearchClass());
		searchCriteria.add(Restrictions.eq(getSearchFieldName(), getObjectId()));
		return searchCriteria;
	}

	/**
	 * Plug-in method which allows subclasses to enable or disable load on demand dynamically.
	 */
	protected void changeLoadOnDemand() {
		// do nothing
	}

	protected abstract String getSearchFieldName();
	
	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
}
