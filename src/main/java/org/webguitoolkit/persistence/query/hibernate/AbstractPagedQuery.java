package org.webguitoolkit.persistence.query.hibernate;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.IPagedQuery;


/**
 * For processing a lot of objects,<br>
 * e.g. iterating over all objects on a database<br> 
 * you can use the AbstractPagedQuery query.<br>
 * 
 * The default page size is 100 but you can change it by calling the constructor<br>
 * with the PersistenceManager and the pageSize<br>
 * 
<pre>
		AbstractPagedQuery<MyObject> query = new MyPagedQuery<MyObject>( getPersistenceManager(), 10 );
		while( query.hasNext() ){
			Collection<MyObject> part = query.next();
			// ... process your data here
		}
</pre>
 * 
 * @author Martin Hermann
 *
 * @param <T> the interface of the search class
 */
public abstract class AbstractPagedQuery<T extends IPersistable> extends BaseHibernateQuery<T> implements IPagedQuery<T>{

	private long resultCount = -1l;
	private int pageSize = 100;
	private int currentCount = 0;
	
	public AbstractPagedQuery( PersistenceManager pm ) {
		super(pm);
	}
	public AbstractPagedQuery(PersistenceManager pm, int pageSize ) {
		super(pm);
		this.pageSize = pageSize;
	}

	protected abstract Criteria getSearchCriteria();
	

	/**
	 * @return true if there are more results
	 */
	public boolean hasNext(){
		if( resultCount < 0 ){
			executeCountQuery();
		}
		return resultCount > currentCount;
	}

	private void executeCountQuery() {
		Criteria criteria = getSearchCriteria();
		criteria.setProjection(Projections.rowCount());
		resultCount = ((Long)criteria.list().get(0)).longValue();
	}

	@SuppressWarnings( value="unchecked" )
	protected Collection<T> executePagedQuery() {
		Criteria criteria = getSearchCriteria();
		criteria.setFirstResult(currentCount);
		criteria.setMaxResults( pageSize );
		Collection<T> result = criteria.list();
		currentCount += pageSize;
		return result;
	}

	/**
	 * @return the next portion of the result and an empty collection if no results are available anymore
	 */
	public Collection<T> next() {
		// Hibernate usually returns an empty collection even if nothing is found - we return an empty collection as well if there are no more results left
		Collection<T> result = new ArrayList<T>();
		if( hasNext() ){
			result = executePagedQuery();
		}
		return result;
	}
}
