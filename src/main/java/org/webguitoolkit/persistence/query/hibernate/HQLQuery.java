package org.webguitoolkit.persistence.query.hibernate;

import java.util.Collection;

import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.ICollectionQuery;


public class HQLQuery<T extends IPersistable> extends BaseHibernateQuery<T> implements ICollectionQuery<T> {
	
	private String hql;
	private Class<? extends T> searchClass;
	
	public HQLQuery(PersistenceManager pm, String hql, Class<? extends T> newSearchClass) {
		super( pm );
		this.hql = hql;
		searchClass = newSearchClass;
	}

	@SuppressWarnings( value={"unchecked"} )
	public Collection<T> execute() {
		return getDBSession().createQuery(hql).list();
	}

	protected Class<? extends T> getSearchClass() {
		return searchClass;
	}
}
