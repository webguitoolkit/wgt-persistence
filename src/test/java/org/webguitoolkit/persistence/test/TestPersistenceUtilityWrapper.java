/**
 * 
 */
package org.webguitoolkit.persistence.test;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;


/**
 * This wrapper for HibernateUtility allows to enable/disable a wrapped HibernateUtility
 * to check whether the correct HibernateUtility is called from the PersistenceManager.
 * Functionality which is independent whether actual modification in the PersistenceManager
 * for the wrapped HibernateUtility are forwarded. Functionality which would modify the
 * wrapped HibernateUtility throws an exception if the activation flag is not set.
 * 
 * @author Wolfram Kaiser
 */
public class TestPersistenceUtilityWrapper extends HibernateUtility {

	private boolean isActivated;

	private IPersistenceUtility wrappedPersistenceUtility;
	
	/**
	 * 
	 */
	public TestPersistenceUtilityWrapper(IPersistenceUtility newWrappedPersistenceUtility) {
		super((Configuration)null,"wrapperId" );
		setWrappedPersistenceUtility(newWrappedPersistenceUtility);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.HibernateUtility#initialize(org.hibernate.cfg.Configuration)
	 */
	@Override
	protected void initialize(Configuration config, String configId ) {
		// do nothing - we delegate only...
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#attach(java.util.Set)
	 */
	public void attach(Set<IPersistable> registeredObjects) {
		if (!isActivated && (registeredObjects.size() != 0)) {
			throw new RuntimeException("Attempt to use deactivated/wrong PersistenceUtility");
		}
		getWrappedPersistenceUtility().attach(registeredObjects);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#beginTransaction()
	 */
	public void beginTransaction() {
		getWrappedPersistenceUtility().beginTransaction();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#close()
	 */
	public void close() {
		getWrappedPersistenceUtility().close();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#commitTransaction()
	 */
	public void commitTransaction() throws PersistenceException {
		getWrappedPersistenceUtility().commitTransaction();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#delete(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void delete(IPersistable po) throws PersistenceException {
		assertActivatedPersistenceUtility();
		getWrappedPersistenceUtility().delete(po);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#detach()
	 */
	public void detach() {
		getWrappedPersistenceUtility().detach();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#refresh(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void refresh(IPersistable po) throws PersistenceException {
		assertActivatedPersistenceUtility();
		getWrappedPersistenceUtility().refresh(po);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#rollbackTransaction()
	 */
	public void rollbackTransaction() throws PersistenceException {
		getWrappedPersistenceUtility().rollbackTransaction();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#save(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void save(IPersistable po) throws PersistenceException {
		assertActivatedPersistenceUtility();
		getWrappedPersistenceUtility().save(po);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#update(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void update(IPersistable po) throws PersistenceException {
		assertActivatedPersistenceUtility();
		getWrappedPersistenceUtility().update(po);
	}

	/**
	 * @see org.webguitoolkit.persistence.util.HibernateUtility#getConfig()
	 */
	@Override
	public Configuration getConfig() {
		return ((HibernateUtility)getWrappedPersistenceUtility()).getConfig();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.HibernateUtility#getSession()
	 */
	@Override
	public Session getSession() {
		return ((HibernateUtility)getWrappedPersistenceUtility()).getSession();
	}

	/**
	 * @see org.webguitoolkit.persistence.util.HibernateUtility#searchForClass(java.lang.String)
	 */
	@Override
	public PersistentClass searchForClass(String searchClassName) {
		return ((HibernateUtility)getWrappedPersistenceUtility()).searchForClass(searchClassName);
	}

	/**
	 * @return the isActivated
	 */
	public boolean isActivated() {
		return isActivated;
	}

	/**
	 * @param newIsActivated the isActivated to set
	 */
	public void setActivated(boolean newIsActivated) {
		isActivated = newIsActivated;
	}

	/**
	 * @return the wrappedPersistenceUtility
	 */
	public IPersistenceUtility getWrappedPersistenceUtility() {
		return wrappedPersistenceUtility;
	}

	/**
	 * @param newWrappedPersistenceUtility the wrappedPersistenceUtility to set
	 */
	public void setWrappedPersistenceUtility(IPersistenceUtility newWrappedPersistenceUtility) {
		wrappedPersistenceUtility = newWrappedPersistenceUtility;
	}

	/**
	 * 
	 */
	public void assertActivatedPersistenceUtility() {
		if (!isActivated) {
			throw new RuntimeException("Attempt to use deactivated/wrong PersistenceUtility");
		}
	}
}
