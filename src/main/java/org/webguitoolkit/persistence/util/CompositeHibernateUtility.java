/**
 * 
 */
package org.webguitoolkit.persistence.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.proxy.HibernateProxyHelper;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable;


/**
 * The CompositeHibernateUtility encapsulated one or more HibernateUtility instances, each of which
 * is configured to connect to a specific database backend (through a Hibernate configuration instance).
 * For each HibernateUtility instance the CompositeHibernateUtility knows which classes the HibernateUtility
 * is responsible for. The CompositeHibernateUtility then delegates all calls for instances of the
 * persistent classes to the registered HibernateUtility. 
 * <p>
 * Usage example (from IS-Persistence-Framework2-Benutzung.doc chapter 2):
 * <code>
 * 
 * Configuration config1 = new Configuration().
 *     setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect").
 *     setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver").
 *     setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:pt1").
 *     setProperty("hibernate.connection.username", "sa").
 *     setProperty("hibernate.connection.password", "").
 *     setProperty("hibernate.connection.autocommit", "true").
 * config1.addAnnotatedClass( Message.class );
 * 
 * HibernateUtility hibernateUtil1 = new HibernateUtility(config);
 * 
 * Configuration config2 = new Configuration().
 *     setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect").
 *     setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver").
 *     setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:pt2").
 *     setProperty("hibernate.connection.username", "sa").
 *     setProperty("hibernate.connection.password", "").
 *     setProperty("hibernate.connection.autocommit", "true").
 * config2.addAnnotatedClass( Message2.class );
 * 
 * HibernateUtility hibernateUtil2 = new HibernateUtility(config2);
 * 
 * CompositeHibernateUtility combinedHU = new CompositeHibernateUtility();
 * combinedPU.addDelegatePersistenceUtility(hibernateUtil1);
 * combinedPU.addDelegatePersistenceUtility(hibernateUtil2);
 * 
 * PersistenceManager pm = createPersistenceManager(combinedHU);
 * PersistenceContext.setPersistenceManager(pm);
 * </code>
 * @author i102389
 */
public class CompositeHibernateUtility implements IPersistenceUtility {

	private List<HibernateUtility> delegatePersistenceUtilities;
	private Map<String, HibernateUtility> delegatePersistenceUtilityCache;
	
	/**
	 * 
	 */
	public CompositeHibernateUtility() {
		delegatePersistenceUtilities = new ArrayList<HibernateUtility>();
		delegatePersistenceUtilityCache = new HashMap<String, HibernateUtility>();
	}

	public CompositeHibernateUtility( HibernateUtility... hibernateUtilities ) {
		this();
		for( HibernateUtility hu : hibernateUtilities ){
			addDelegatePersistenceUtility( hu );
		}
	}

	@SuppressWarnings("unchecked")
	public void addDelegatePersistenceUtility(HibernateUtility newPersistenceUtility) {
		if (!containsDelegatePersistenceUtility(newPersistenceUtility)) {
			delegatePersistenceUtilities.add(newPersistenceUtility);
			Iterator<PersistentClass> classIterator = newPersistenceUtility.getConfig().getClassMappings();
			while (classIterator.hasNext()) {
				PersistentClass currentPersistentClass = (PersistentClass) classIterator.next();
				delegatePersistenceUtilityCache.put(currentPersistentClass.getClassName(), newPersistenceUtility);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void removeDelegatePersistenceUtility(HibernateUtility oldPersistenceUtility) {
		if (containsDelegatePersistenceUtility(oldPersistenceUtility)) {
			delegatePersistenceUtilities.remove(oldPersistenceUtility);
			Iterator<PersistentClass> classIterator = oldPersistenceUtility.getConfig().getClassMappings();
			while (classIterator.hasNext()) {
				PersistentClass currentPersistentClass = (PersistentClass) classIterator.next();
				delegatePersistenceUtilityCache.remove(currentPersistentClass.getClassName());
			}
		}
	}
	
	public Iterator<HibernateUtility> getDelegatePersistenceUtilities() {
		return delegatePersistenceUtilities.iterator();
	}
	
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(String searchClassName) {
		HibernateUtility foundHibernateUtility = delegatePersistenceUtilityCache.get(searchClassName);
		if (foundHibernateUtility == null) {
			for (HibernateUtility currentHU : delegatePersistenceUtilities) {
				if (currentHU.searchForClass(searchClassName) != null) {
					foundHibernateUtility = currentHU;
					delegatePersistenceUtilityCache.put(searchClassName, foundHibernateUtility);
					break;
				}
			}
		}
		return foundHibernateUtility;
	}
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(Class<? extends IPersistable> searchClass) {
		HibernateUtility foundHibernateUtility = delegatePersistenceUtilityCache.get(searchClass.getName());
		if (foundHibernateUtility == null) {
			for (HibernateUtility currentHU : delegatePersistenceUtilities) {
				if (currentHU.searchForClass(searchClass) != null) {
					foundHibernateUtility = currentHU;
					delegatePersistenceUtilityCache.put(searchClass.getName(), foundHibernateUtility);
					break;
				}
			}
		}
		return foundHibernateUtility;
	}

	public boolean containsDelegatePersistenceUtility(HibernateUtility checkPersistenceUtility) {
		boolean foundHibernateUtility = false;
		for (HibernateUtility currentHU : delegatePersistenceUtilities) {
			if (currentHU == checkPersistenceUtility) {
				foundHibernateUtility = true;
				break;
			}
		}
		return foundHibernateUtility;
	}
	
	public void clearDelegatePersistenceUtilityCache() {
		delegatePersistenceUtilityCache.clear();
	}
	
	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#attach(java.util.Set)
	 */
	public void attach(Set<IPersistable> registeredObjects) {
		// initialize a lookup mechanism for searching a HibernateUtility
		Map<HibernateUtility, Set<IPersistable>> huMap = new HashMap<HibernateUtility, Set<IPersistable>>();
		for (HibernateUtility currentHU : delegatePersistenceUtilities) {
			huMap.put(currentHU, new HashSet<IPersistable>());
		}		
		
		// iterate over all persistent objects and put them in the list according to the HibernateUtility in which their class is registered
		for (IPersistable currentPO : registeredObjects) {
			IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(currentPO.getClass().getCanonicalName());
			Set<IPersistable> targetSet = huMap.get(foundHU);
			targetSet.add(currentPO);
		}
		
		// attach the sorted lists for each HibernateUtility
		for (HibernateUtility currentHU : delegatePersistenceUtilities) {
			Set<IPersistable> targetSet = huMap.get(currentHU);
			currentHU.attach(targetSet);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#beginTransaction()
	 */
	public void beginTransaction() {
		for (HibernateUtility nextHU : delegatePersistenceUtilities) {
			nextHU.beginTransaction();
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#close()
	 */
	public void close() {
		for (HibernateUtility nextHU : delegatePersistenceUtilities) {
			nextHU.close();
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#commitTransaction()
	 */
	public void commitTransaction() throws PersistenceException {
		for (HibernateUtility nextHU : delegatePersistenceUtilities) {
			nextHU.commitTransaction();
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#delete(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void delete(IPersistable po) throws PersistenceException {
		IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(po.getClass().getCanonicalName());
		if (foundHU != null) {
			foundHU.delete(po);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#detach()
	 */
	public void detach() {
		for (IPersistenceUtility nextHU : delegatePersistenceUtilities) {
			nextHU.detach();
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#refresh(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void refresh(IPersistable po) throws PersistenceException {
		IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(po.getClass().getCanonicalName());
		if (foundHU != null) {
			foundHU.refresh(po);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#rollbackTransaction()
	 */
	public void rollbackTransaction() throws PersistenceException {
		for (HibernateUtility nextHU : delegatePersistenceUtilities) {
			nextHU.rollbackTransaction();
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#save(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void save(IPersistable po) throws PersistenceException {
		IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(po.getClass().getCanonicalName());
		if (foundHU != null) {
			foundHU.save(po);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#update(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void update(IPersistable po) throws PersistenceException {
		IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(po.getClass().getCanonicalName());
		if (foundHU != null) {
			foundHU.update(po);
		}
	}

	/**
	 * @return the delegatePersistenceUtility
	 */
	protected List<HibernateUtility> getDelegatePersistenceUtility() {
		return delegatePersistenceUtilities;
	}

	/**
	 * @param newDelegatePersistenceUtility the delegatePersistenceUtility to set
	 */
	protected void setDelegatePersistenceUtility(List<HibernateUtility> newDelegatePersistenceUtility) {
		delegatePersistenceUtilities = newDelegatePersistenceUtility;
	}

	/**
	 * @return the delegatePersistenceUtilityCache
	 */
	protected Map<String, HibernateUtility> getDelegatePersistenceUtilityCache() {
		return delegatePersistenceUtilityCache;
	}

	/**
	 * @param newDelegatePersistenceUtilityCache the delegatePersistenceUtilityCache to set
	 */
	protected void setDelegatePersistenceUtilityCache(Map<String, HibernateUtility> newDelegatePersistenceUtilityCache) {
		delegatePersistenceUtilityCache = newDelegatePersistenceUtilityCache;
	}

	public void attach(IPersistable po) {
		IPersistenceUtility pu = getDelegatePersistenceUtilityForClass( HibernateProxyHelper.getClassWithoutInitializingProxy( po ).getCanonicalName());;
		if (pu != null) {
			pu.attach( po );
		}
		else {
			throw new RuntimeException("Could not attach object of class '" + po.getClass().getCanonicalName() + "' - no associated IPersistenceUtility found!");
		}
		
	}

	public int getPropertyLength(IPersistable po, String propertyName) {
		IPersistenceUtility foundHU = getDelegatePersistenceUtilityForClass(po.getClass().getCanonicalName());
		if (foundHU != null) {
			return foundHU.getPropertyLength( po, propertyName );
		}
		return -1;
	}
}
