package org.webguitoolkit.persistence.query.jpa;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.BaseQuery;
import org.webguitoolkit.persistence.util.IPersistenceUtility;
import org.webguitoolkit.persistence.util.JPAUtility;


public abstract class BaseJPAQuery<T extends IPersistable> extends BaseQuery {

	public BaseJPAQuery(PersistenceManager pm) {
		super(pm);
	}

	protected EntityManager getEntityManager() {
		return getEntityManagerForClass( getSearchClass() );
	}
	
	/**
	 * @see com.endress.infoserve.user.bo.model.IDatabaseProvider#getDbSessionForClass()
	 */
	protected EntityManager getEntityManagerForClass(Class<? extends IPersistable> resultEntityClass) {
		EntityManager associatedSession;
		
		IPersistenceUtility pu = getPersistenceManager().getPersistenceUtility().getDelegatePersistenceUtilityForClass(resultEntityClass.getCanonicalName());
		if ((pu != null) && (pu instanceof JPAUtility)) {
			return ((JPAUtility)pu).getEntityManager();
		}
		else {
			throw new RuntimeException("Cannot call getDbSessionForClass() for Non-Hibernate DB backend/PersistenceUtility: " + resultEntityClass);
		}
	}
	
	protected abstract Class<? extends T> getSearchClass();
	
	protected CriteriaQuery<? extends T> createDefaultCriteria() {
		return getEntityManager().getCriteriaBuilder().createQuery(getSearchClass());
	}
	
	/**
	 * Utility method which allows subclasses to enable or disable the query cache.

	 */
	protected void forceDbRead(CriteriaQuery searchCriteria) {
		throw new UnsupportedOperationException("Not implemented for JPA Query");
//		searchCriteria.setCacheMode(CacheMode.REFRESH);
	}
}
