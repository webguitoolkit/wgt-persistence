/*
 * (c) 2005, Endress&Hauser InfoServe GmbH & Co KG
 * 
 */
package org.webguitoolkit.persistence.query.jpa;

import java.util.Collection;

import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.ICollectionQuery;


/**
 * Common super class for all queries which should return more than one result.
 * 
 * @author thomas.weinschenk@infoserve.endress.com
 * @authero Wolfram Kaiser
 */
public abstract class BaseCollectionQuery<T extends IPersistable> extends BaseJPAQuery<T> implements ICollectionQuery<T> {
	
	
	/**
	 * @param persistenceManager
	 */
	public BaseCollectionQuery( PersistenceManager persistenceManager ) {
		super( persistenceManager );
	}

	/**
	 * Template method which defines the basic behaviour for executing a query. The actual query must be implemented
	 * in a plug-in method.
	 * 
	 * @see com.endress.infoserve.icp.queries.ICommonQuery#execute()
	 */
	public Collection<T> execute() {
		try {
			Collection<T> foundObjects = performQuery();
			return foundObjects;
		}
		catch (Exception e) {
			LogFactory.getLog(getClass()).error("could not execute query", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Plug-in method for implementing the query.
	 */
	protected abstract Collection<T> performQuery();
	
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

}
