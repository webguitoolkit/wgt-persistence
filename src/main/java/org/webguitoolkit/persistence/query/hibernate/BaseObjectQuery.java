/*
 * File: BaseObjectQuery.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.IObjectQuery;
import org.webguitoolkit.persistence.query.NonUniqueResultException;


/**
 * @author Wolfram Kaiser
 */
public abstract class BaseObjectQuery<T extends IPersistable> extends BaseHibernateQuery<T> implements IObjectQuery<T> {
		
	public BaseObjectQuery( PersistenceManager persistenceManager ) {
		super( persistenceManager );
	}

	/**
	 * @see com.endress.infoserve.user.bo.queries.IBaseObjectQuery#execute()
	 */
	public T execute() throws NonUniqueResultException {
		try {
			T foundObject = performQuery();
			return foundObject;
		}
		catch (NonUniqueResultException e) {
			// do not fall into the generic exception handling routine - continue with the declare exception instead
			throw e;
		}
		catch (Exception e) {
			LogFactory.getLog(getClass()).error("could not execute query", e);
			throw new RuntimeException(e);
		}
	}

	protected abstract T performQuery() throws NonUniqueResultException;
	
	protected void addDistinctClause(Criteria searchCriteria) {
		//searchCriteria.setResultTransformer(new DistinctRootEntityResultTransformer());
		searchCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		/*
		// work-around for performming a select distinct
		String[] fields = getDbSession().getSessionFactory().getClassMetadata(getSearchClass()).getPropertyNames();
		Projection projectionFields = formProjection(fields);
		searchCriteria.setProjection(Projections.distinct(projectionFields));
		searchCriteria.setResultTransformer(new AliasToBeanResultTransformer(getSearchClass()));
		*/ 
	}
	
	/**
	 * Plug-in method which allows subclasses to enable or disable load on demand dynamically.
	 * 
	 * Example: searchCriteria.setFetchMode("person", FetchMode.EAGER)
	 */
	protected void changeLoadOnDemand(Criteria searchCriteria) {
		// do nothing
	}
	
	/**
	 * Creates a projection which includes all the given class properties in the query
	 * 
	 * @param properties
	 * @return
	 */
	protected Projection formProjection(String[] properties) {
		ProjectionList list = Projections.projectionList();
		for (int i = 0; i < properties.length; ++i) {
			list.add(Projections.property(properties[i]), properties[i]);
		}
		return list;
	}
}
