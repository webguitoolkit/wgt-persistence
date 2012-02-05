package org.webguitoolkit.persistence.util;

import java.util.Set;

import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable;


public interface IPersistenceUtility {

	/**
	 * @deprecated
	 * @param registeredObjects
	 */
	void attach(Set<IPersistable> registeredObjects);

	/**
	 * attach IPersistable to the underlying framework
	 */
	void attach(IPersistable po );

	void detach();

	void save(IPersistable po) throws PersistenceException;

	void update(IPersistable po) throws PersistenceException;

	void delete(IPersistable po) throws PersistenceException;

	void commitTransaction() throws PersistenceException;
	
	void rollbackTransaction() throws PersistenceException;

	void close();

	void refresh(IPersistable po) throws PersistenceException;

	void beginTransaction();

	/**
	 * This method is included according to the composite design pattern. Implementing classes
	 * which delegate to several instances of IPersistenaceUtility can choose the correct instance.
	 * 
	 * @param searchClassName
	 * @return
	 */
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(String searchClassName);
	
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(Class<? extends IPersistable> searchClassName);

	public int getPropertyLength( IPersistable entity, String propertyName );
}
