package org.webguitoolkit.persistence.query.hibernate;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.BaseQuery;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;


public abstract class BaseHibernateQuery<T extends IPersistable> extends BaseQuery {

	public BaseHibernateQuery(PersistenceManager pm) {
		super(pm);
	}

	protected Session getDBSession() {
		return getDbSessionForClass( getSearchClass() );
	}
	
	/**
	 * @see com.endress.infoserve.user.bo.model.IDatabaseProvider#getDbSessionForClass()
	 */
	protected Session getDbSessionForClass(Class<? extends T> resultEntityClass) {
		Session associatedSession;
		
		IPersistenceUtility pu = getPersistenceManager().getPersistenceUtility().getDelegatePersistenceUtilityForClass(resultEntityClass.getCanonicalName());
		if ((pu != null) && (pu instanceof HibernateUtility)) {
			associatedSession = ((HibernateUtility)pu).getSession();
		}
		else {
			throw new RuntimeException("Cannot call getDbSessionForClass() for Non-Hibernate DB backend/PersistenceUtility: " + resultEntityClass);
		}
		
		return associatedSession;
	}
	
	protected abstract Class<? extends T> getSearchClass();
	
	protected Criteria createDefaultCriteria() {
		return getDBSession().createCriteria(getSearchClass());
	}
	
	/**
	 * Utility method which allows subclasses to enable or disable the query cache.

	 */
	protected void forceDbRead(Criteria searchCriteria) {
		searchCriteria.setCacheMode(CacheMode.REFRESH);
	}
}
