/*
 * File: AllOfAKindQuery.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.jpa;

import java.util.Collection;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;

/**
 * @author i102389
 *
 * @param <T> The elements in the collection may be classes implementing an interface which is derived from IPersistable.
 */
public class AllOfAKindQuery<T extends IPersistable> extends BaseCollectionQuery<T> {

	private Class<? extends T> searchClass;
	private String orderByField;
	private boolean isOrderIgnoreCase;

	public AllOfAKindQuery(PersistenceManager persistenceManager, Class<? extends T> searchClass) {
		super(persistenceManager);
		this.searchClass = searchClass;
	}

	@SuppressWarnings(value = { "unchecked" })
	public Collection<T> execute() {
		return performQuery();
	}

	protected Class<? extends T> getSearchClass() {
		return searchClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<T> performQuery() {
		CriteriaQuery<? extends T> criteria = createDefaultCriteria();
		Root<? extends T> p = criteria.from(getSearchClass());
		if (getOrderByField() != null) {
			criteria.orderBy(createOrderCondition( p ));
		}
		return (Collection<T>)getEntityManager().createQuery(criteria).getResultList();
	}

	/**
	 * @return
	 */
	protected Order createOrderCondition( Root<? extends T> root ) {
		Order createdOrder = getEntityManager().getCriteriaBuilder().asc( root.get( getOrderByField() ) );
		if (isOrderIgnoreCase()) {
//			TODO: createdOrder.ignoreCase();
		}
		return createdOrder;
	}


	protected String getOrderByField() {
		return orderByField;
	}

	protected void setOrderByField(String newOrderByField) {
		orderByField = newOrderByField;
	}

	protected void setOrderByField(String newOrderByField, boolean newIsOrderIgnoreCase) {
		orderByField = newOrderByField;
		isOrderIgnoreCase = newIsOrderIgnoreCase;
	}
	
	/**
	 * @return the isOrderIgnoreCase
	 */
	protected boolean isOrderIgnoreCase() {
		return isOrderIgnoreCase;
	}
}
