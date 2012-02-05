package org.webguitoolkit.persistence.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.TransactionRequiredException;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.StaleStateException;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.webguitoolkit.persistence.ConcurrentChangeException;
import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;


/**
 * The HibernateUtility is a wrapper for a Hibernate Session object. The SessionFactory is kept as static variable in
 * order to minimize effort. HU takes care of creating new session whenever needed with the correct SessionFactory. HU
 * provides functions to attach persistable objects to a Hibernate session.
 * 
 * For multi-configuration scenarios (e.g. using application model AND user model) multiple SessionFactories are
 * required. These are kept in here, too. Maybe this has to be redesigned in the future.
 * 
 * @author Martin, Peter, Wolfram
 * 
 */
public class JPAUtility implements IPersistenceUtility {

	
	private Map<Class<? extends IPersistable>,Map<String, Integer>> attributeLengthByClass = new HashMap<Class<? extends IPersistable>, Map<String,Integer>>(); // map of the string attributes length

	private static Hashtable<String, EntityManagerFactory> factories = new Hashtable<String, EntityManagerFactory>();
	private EntityManagerFactory entityManagerFactory;
	
	private EntityManager entityManager;

	private Log log = LogFactory.getLog(JPAUtility.class);

	/**
	 * 
	 * @param config
	 */
	public JPAUtility(String configId) {
		initialize(configId);
	}

	/**
	 * @param config
	 */
	protected void initialize(String configId) {
		
		if (factories.get(configId) == null) {
			factories.put(configId, Persistence.createEntityManagerFactory(configId) );
		}
		entityManagerFactory = factories.get(configId);
	}

//	/**
//	 * Create a HU an map the passed classes to its configuration.
//	 * 
//	 * @param annotatedClasses
//	 *            all annotated classes to be used wit this HU.
//	 * @param hibernateCfg
//	 *            the configuration XML, if NULL "hibernate.cfg.xml" will be taken.
//	 */
//	public <T extends IPersistable> JPAUtility(Class<T>[] annotatedClasses, String hibernateCfg) {
//		Configuration annotationConfiguration = new Configuration();
//		for (Class<T> clazz : annotatedClasses) {
//			annotationConfiguration.addAnnotatedClass(clazz);
//		}
//		if (hibernateCfg != null) {
//			initialize(annotationConfiguration.configure(hibernateCfg), hibernateCfg);
//		} else {
//			initialize(annotationConfiguration.configure(), "hibernate.cfg.xml");
//		}
//	}


	/**
	 * Add all persistable to Session
	 */
	public void attach(Set<IPersistable> registeredObjects) {
		log.trace(">>> attach()");
		EntityManager em = getEntityManager();
		List<IPersistable> registered = new ArrayList<IPersistable>(registeredObjects);
		for (IPersistable persistable : registered) {
			try {
				em.lock(persistable, LockModeType.NONE);
			} catch (javax.persistence.PersistenceException ex) {
				// object is not known in the hibernate session
				// -> throw it away
				log.error("Object: " + persistable.getClass().getSimpleName() + " " + persistable.getId(), ex);
				registeredObjects.remove(persistable);
			}
		}
		log.trace("<<< attach()");
	}

	/**
	 * Closes the Session in order to detach all persistables.
	 */
	public void detach() {
		log.trace(">>> detach()");
		close();
		log.trace("<<< detach() ");

	}

	/**
	 * close the session if open and not NULL
	 */
	public void close() {
		log.trace(">>> close()");
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
		entityManager = null;
		log.trace("<<< close() ");
	}

	/**
	 * 
	 */
	public void refresh(IPersistable po) throws PersistenceException {
		if (po.getModificationType() == ModificationType.NEW) {
			if (po.getId() > 0) {
				// handle only objects that are already stored in the
				// database and thus can be evicted/refreshed
				getEntityManager().refresh(po);
			}
		} else {
			getEntityManager().refresh(po);
		}
	}

	/**
	 * 
	 * @return a session. may be a new one.
	 */
	public EntityManager getEntityManager() {
		if (entityManager == null) {
			log.debug("Opening new EntityManager (" + Thread.currentThread() + ")");

			entityManager = entityManagerFactory.createEntityManager();
	
		} else if (!entityManager.isOpen()) {
			// log.fatal("Found a Session that is not open (" + Thread.currentThread() + ")");
			entityManager = entityManagerFactory.createEntityManager();
		}

		return entityManager;
	}

	/**
	 * 
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#beginTransaction()
	 */
	public void beginTransaction() {
		getEntityManager().getTransaction().begin();
	}

	/**
	 * @see com.endress.infoserve.persistence.IHibernateUtility#commitTransaction()
	 */
	public void commitTransaction() throws PersistenceException {

		try {
			EntityTransaction transaction = getEntityManager().getTransaction();
			
			if (transaction != null && transaction.isActive()) {
				log.debug("Committing database (" + Thread.currentThread() + ")");
				transaction.commit();
			} else {
				log.debug("Attempt to commit without valid transaction (" + Thread.currentThread() + ")");
			}
		}
		catch (StaleStateException e) {
			throw new ConcurrentChangeException("Error during commit", e);
		}
		catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see com.endress.infoserve.persistence.IHibernateUtility#rollbackTransaction()
	 */
	public void rollbackTransaction() throws PersistenceException {

		EntityTransaction transaction = getEntityManager().getTransaction();

		if (transaction != null && transaction.isActive()) {
			log.debug("Rollback transaction (" + Thread.currentThread() + ")");
			transaction.rollback();
		}
		
		// a rollback on the transaction is not enough because the session still has the objects in its action queue
		// -> clear action queues in session
		getEntityManager().clear();
	}

	public void delete(IPersistable po) throws PersistenceException {
		try {
			getEntityManager().remove(po);
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e);
		}
	}

	public void save(IPersistable po) throws PersistenceException {
		try {
			getEntityManager().persist(po);
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e);
		}
	}

	public void update(IPersistable po) throws PersistenceException {
		try {
			getEntityManager().persist(po);
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e);
		}
	}

	public class MyPostLoadEventListener extends DefaultPostLoadEventListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void onPostLoad(PostLoadEvent event) {
			super.onPostLoad(event);
			Object dbObject = event.getEntity();
			if (dbObject instanceof Persistable) {
				// add/attach the object only if it is an IPersistable - some objects may be read from DB
				// but are not full IPersistable because the have only read-only data (e.g. views)
				Persistable me = (Persistable) dbObject;
				PersistenceManager pm = PersistenceContext.getPersistenceManager();
				if (pm == null) {
					throw new RuntimeException("PersistenceManager must be set in context");
				}
				pm.addObjectInUse(me);
			}
		}
	}

	// /**
	// * tried to attach used object when new loaded but it does not work that way
	// * @author i102415
	// */
	// public class MyPreLoadEventListener extends DefaultPreLoadEventListener {
	// private static final long serialVersionUID = 1L;
	//
	// public void onPreLoad(PreLoadEvent event) {
	// IPersistable me = (IPersistable) event.getEntity();
	//
	// PersistenceManager pm = PersistenceContext.getPersistenceManager();
	// if (pm == null) {
	// throw new RuntimeException(
	// "PersistenceManager must be set in context");
	// }
	// for (IPersistable entity : pm.getUsedObjectSet()) {
	// if (entity.getClass().equals(me.getClass())
	// && entity.getId() == me.getId()) {
	// System.out.println("Object exists1");
	// getSession().evict(me);
	// attach(entity);
	// break;
	// }
	// }
	//
	// super.onPreLoad(event);
	// }
	// }

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}
	
	/**
	 * @param searchClassName
	 * @return
	 */
	public EntityType<?> searchForEntityType(Class theClass) {
		return entityManagerFactory.getMetamodel().entity(theClass);
	}

	/**
	 * This is HibernateUtility does not know of any other HibernateUtitilities (e.g. for delegation) so the instance
	 * itself is returned if a class mapping for this class can be found
	 */
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(String searchClassName) {
		IPersistenceUtility foundPU = null;
		try {
			if (searchForEntityType( Class.forName(searchClassName) ) != null) {
				foundPU = this;
			}
		}
		catch (ClassNotFoundException e) {
			return null;
		}
		return foundPU;
	}

	/**
	 * This is HibernateUtility does not know of any other HibernateUtitilities (e.g. for delegation) so the instance
	 * itself is returned if a class mapping for this class can be found
	 */
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(Class searchClass) {
		IPersistenceUtility foundPU = null;
		if (searchForEntityType( searchClass ) != null) {
			foundPU = this;
		}
		return foundPU;
	}

	public void attach(IPersistable po) {
		EntityManager entityManager = getEntityManager();

		if (po.getModificationType()!= ModificationType.NEW && !entityManager.contains(po) ) {
			if( !entityManager.getTransaction().isActive() )
				entityManager.getTransaction().begin();
			try {
				entityManager.lock( po, LockModeType.NONE);
			} catch (TransactionRequiredException ex) {
				// object is not in the data base -> it is new
			} catch (IllegalArgumentException ex) {
				// nothing to do
//				try {
//					// remove other object and lock current
//					Object other = session.get(ex.getEntityName(), ex.getIdentifier());
//					session.evict(other);
//					session.buildLockRequest(LockOptions.NONE).lock(po);
//				} catch (Throwable e) {
//					printSessionStatistic(System.out);
//					e.printStackTrace();
//					LogFactory.getLog(getClass()).fatal("Error during resolving NonUniqueObjectException on attach", e);
//				}
			}
		}
	}
	
	
//	private void loadAttributeLength( Class entity ) {
//		Map<String, Integer> attributeLengths = this.attributeLengthByClass.get(entity);
//		if (config != null && attributeLengths == null) {
//			// String className =
//			// HibernateProxyHelper.getClassWithoutInitializingProxy
//			// (this).getName();
//			String className = entity.getName();
//			if (className.indexOf("$$") > 0) {
//				className = className.substring(0, className.indexOf("$$"));
//			}
//			attributeLengths = new HashMap<String, Integer>();
//			this.attributeLengthByClass.put(entity, attributeLengths);
//			loadProperties(config, className, attributeLengths);
//		} else {
//			attributeLengths = new HashMap<String, Integer>();
//		}
//	}
//
//	/*
//	 * recursive loading of the property length
//	 */
//	@SuppressWarnings("unchecked")
//	private void loadProperties(Configuration hibernateConfig, String className, Map<String, Integer> attributeLengths ) {
//		try {
//			if (Persistable.class.getName().equals(className)) {
//				return;
//			} else {
//				// collect the parent properties first
//				String superClass = Class.forName(className).getSuperclass()
//						.getName();
//				loadProperties(hibernateConfig, superClass, attributeLengths);
//			}
//			// get mapping class
//			PersistentClass persClass = hibernateConfig
//					.getClassMapping(className);
//			if (persClass != null) {
//				Iterator<Property> propIter = persClass.getPropertyIterator();
//				// iteration over the properties and collect the strings
//				while (propIter.hasNext()) {
//					Property prop = propIter.next();
//					if (prop.getValue() != null
//							&& prop.getValue() instanceof SimpleValue) {
//						SimpleValue value = (SimpleValue) prop.getValue();
//						if (!"java.lang.String".equals(value.getTypeName())
//								&& !"text"
//										.equalsIgnoreCase(value.getTypeName())) {
//							continue;
//						}
//						Iterator<org.hibernate.mapping.Column> iter = value
//								.getColumnIterator();
//						if (iter.hasNext()) {
//							org.hibernate.mapping.Column col = iter.next();
//							attributeLengths.put(prop.getName(), col
//									.getLength());
//						}
//					} else if (prop.getValue() != null
//							&& prop.getValue().getClass().equals(Map.class)) {
//						org.hibernate.mapping.Map value = (org.hibernate.mapping.Map) prop
//								.getValue();
//						if (value.getIndex().getColumnIterator().hasNext()) {
//							org.hibernate.mapping.Column indexCol = (org.hibernate.mapping.Column) value
//									.getIndex().getColumnIterator().next();
//							attributeLengths.put(prop.getName() + ".index",
//									indexCol.getLength());
//						}
//						if (value.getElement().getColumnIterator().hasNext()) {
//							org.hibernate.mapping.Column elementCol = (org.hibernate.mapping.Column) value
//									.getElement().getColumnIterator().next();
//							attributeLengths.put(prop.getName() + ".element",
//									elementCol.getLength());
//						}
//					}
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			// should not happen
//			LogFactory.getLog(this.getClass()).error("Error Loading class", e);
//		}
//	}
	
	/**
	 * @return the length of the String properties from the hibernate
	 *         configuration
	 */
	public int getPropertyLength( IPersistable po, String propertyName ) {
//		Map<String, Integer> attributeLengths = this.attributeLengthByClass.get(po.getClass());
//		if (attributeLengths == null) {
//			// just call once
//			loadAttributeLength(po.getClass());
//			attributeLengths = this.attributeLengthByClass.get(po.getClass());
//		}
//		if (attributeLengths == null) {
//			// should not happen
//			return -1;
//		}
//		Integer length = attributeLengths.get(propertyName);
//		if (length == null) {
			return -1;
//		}
//		return length;
	}



}
