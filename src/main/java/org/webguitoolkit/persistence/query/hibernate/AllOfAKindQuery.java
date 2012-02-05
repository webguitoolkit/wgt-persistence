/*
 * File: AllOfAKindQuery.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query.hibernate;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
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
		Criteria criteria = createCriteria();
		if (getOrderByField() != null) {
			criteria.addOrder(createOrderCondition());
		}
		return criteria.list();
	}

	/**
	 * @return
	 */
	protected Order createOrderCondition() {
		Order createdOrder = Order.asc(getOrderByField());
		if (isOrderIgnoreCase()) {
			createdOrder.ignoreCase();
		}
		return createdOrder;
	}

	/**
	 * @return
	 */
	protected Criteria createCriteria() {
		return getDBSession().createCriteria(getSearchClass());
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
