package org.webguitoolkit.persistence.util;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.TransientObjectException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.CollectionKey;
import org.hibernate.engine.EntityKey;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.stat.SessionStatistics;
import org.webguitoolkit.persistence.ConcurrentChangeException;
import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;

/**
 * The HibernateUtility is a wrapper for a Hibernate Session object. The SessionFactory is kept as static variable in order to
 * minimize effort. HU takes care of creating new session whenever needed with the correct SessionFactory. HU provides functions
 * to attach persistable objects to a Hibernate session.
 * 
 * For multi-configuration scenarios (e.g. using application model AND user model) multiple SessionFactories are required. These
 * are kept in here, too. Maybe this has to be redesigned in the future.
 * 
 * @author Martin, Peter, Wolfram
 * 
 */
public class HibernateUtility implements IPersistenceUtility {

	private Map<Class<? extends IPersistable>, Map<String, Integer>> attributeLengthByClass = new HashMap<Class<? extends IPersistable>, Map<String, Integer>>(); // map
																																									// of
																																									// the
																																									// string
																																									// attributes
																																									// length

	private static final String DEFAULT_ID = "_defautlId";

	private Configuration config;

	// PZ: keep all SessionFactories in here. Used in multi-db scenarios with PFW (questions -> martin)
	// Avoid unnecessary SessionFactory initialization when creating a Session. This is done only once now
	// because SessionFactory initialization parses the whole configuration/DB structure every time otherwise.
	private static Hashtable<String, SessionFactory> factories = new Hashtable<String, SessionFactory>();
	private static Hashtable<String, Configuration> configs = new Hashtable<String, Configuration>();
	private SessionFactory sessionFactory;
	private Session session;

	private Log log = LogFactory.getLog(HibernateUtility.class);

	// flag for PersistenceInterceptor that indicates if we have to load the object from the database or from the objects in use
	private boolean isRefresh = false;

	// tells if this util will be used by hibernate-search
	// if yes, we cannot reach the PersistanceManager, since HS runs in own threads
	private boolean usedForSearchIndex = false;

	/**
	 * 
	 * @param config
	 */
	public HibernateUtility(Configuration config, String configId) {
		initialize(config, configId);
	}

	/**
	 * @param config
	 */
	protected void initialize(Configuration config, String configId) {
		if (configId == null) {
			configId = DEFAULT_ID;
		}

		if (factories.get(configId) == null) {
			setListeners(config);
			factories.put(configId, config.buildSessionFactory());
			configs.put(configId, config);
		}
		this.config = configs.get(configId);
		sessionFactory = factories.get(configId);
	}

	/**
	 * Create a HU an map the passed classes to its configuration.
	 * 
	 * @param annotatedClasses all annotated classes to be used wit this HU.
	 * @param hibernateCfg the configuration XML, if NULL "hibernate.cfg.xml" will be taken.
	 */
	public <T extends IPersistable> HibernateUtility(Class<T>[] annotatedClasses, String hibernateCfg) {
		Configuration annotationConfiguration = new Configuration();
		for (Class<T> clazz : annotatedClasses) {
			annotationConfiguration.addAnnotatedClass(clazz);
		}
		if (hibernateCfg != null) {
			initialize(annotationConfiguration.configure(hibernateCfg), hibernateCfg);
		}
		else {
			initialize(annotationConfiguration.configure(), "hibernate.cfg.xml");
		}
	}

	/**
	 * Standard HU based on config XML with class mapping.
	 * 
	 * @param configFileName
	 */
	public HibernateUtility(String configFileName) {
		this(new Configuration().configure(configFileName), configFileName);
	}

	/**
	 * Add all persistable to Session
	 */
	public void attach(Set<IPersistable> registeredObjects) {
		log.trace(">>> attach()");
		Session session = getSession();
		List<IPersistable> registered = new ArrayList<IPersistable>(registeredObjects);
		for (IPersistable persistable : registered) {
			try {
				session.buildLockRequest(LockOptions.NONE).lock(persistable);
			}
			catch (TransientObjectException ex) {
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
		if (session != null && session.isOpen()) {
			session.close();
		}
		session = null;
		log.trace("<<< close() ");
	}

	/**
	 * 
	 */
	public void refresh(IPersistable po) throws PersistenceException {
		try {
			isRefresh = true;
			if (po.getModificationType() == ModificationType.NEW) {
				if (po.getId() > 0) {
					// handle only objects that are already stored in the
					// database and thus can be evicted/refreshed
					getSession().refresh(po);
				}
			}
			else {
				getSession().refresh(po);
			}
		}
		finally {
			isRefresh = false;
		}
	}

	/**
	 * 
	 * @return a session. may be a new one.
	 */
	public Session getSession() {

		if (session == null) {
			log.debug("Opening new Session (" + Thread.currentThread() + ")");

			session = sessionFactory.openSession();

		}
		else if (!session.isOpen()) {
			// log.fatal("Found a Session that is not open (" + Thread.currentThread() + ")");
			session = sessionFactory.openSession();
		}

		return session;
	}

	/**
	 * 
	 * @see org.webguitoolkit.persistence.util.IPersistenceUtility#beginTransaction()
	 */
	public void beginTransaction() {
		getSession().beginTransaction();
	}

	/**
	 * @see com.endress.infoserve.persistence.IHibernateUtility#commitTransaction()
	 */
	public void commitTransaction() throws PersistenceException {

		try {
			Transaction transaction = getSession().getTransaction();

			if (transaction != null && !transaction.wasCommitted() && !transaction.wasRolledBack()) {
				log.debug("Committing database (" + Thread.currentThread() + ")");
				transaction.commit();
			}
			else {
				log.debug("Attempt to commit without valid transaction (" + Thread.currentThread() + ")");
			}
		}
		catch (StaleStateException e) {
			throw new ConcurrentChangeException("Error during commit", e);
		}
		catch (HibernateException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see com.endress.infoserve.persistence.IHibernateUtility#rollbackTransaction()
	 */
	public void rollbackTransaction() throws PersistenceException {

		Transaction transaction = getSession().getTransaction();

		if (transaction != null && !transaction.wasCommitted() && !transaction.wasRolledBack() && transaction.isActive()) {
			log.debug("Rollback transaction (" + Thread.currentThread() + ")");
			transaction.rollback();
		}

		// a rollback on the transaction is not enough because the session still has the objects in its action queue
		// -> clear action queues in session
		getSession().clear();
	}

	public void delete(IPersistable po) throws PersistenceException {
		try {
			getSession().delete(po);
		}
		catch (HibernateException e) {
			throw new PersistenceException(e);
		}
	}

	public void save(IPersistable po) throws PersistenceException {
		try {
			getSession().save(po);
		}
		catch (HibernateException e) {
			throw new PersistenceException(e);
		}
	}

	public void update(IPersistable po) throws PersistenceException {
		try {
			getSession().save(po);
		}
		catch (HibernateException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Sets the listeners for the hibernate configuration
	 */
	private void setListeners(Configuration config) {
		config.setListener("post-load", new MyPostLoadEventListener());
		config.setInterceptor(new PersistenceInterceptor());
		// config.setListener("pre-load", new MyPreLoadEventListener());
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
				Persistable me = (Persistable)dbObject;
				PersistenceManager pm = PersistenceContext.getPersistenceManager();
				if (pm == null) {
					if (isUsedForSearchIndex()) {
						return;
					}
					else {
						throw new RuntimeException("PersistenceManager must be set in context");
					}
				}
				pm.addObjectInUse(me);
			}
		}
	}

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * @return the config
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param searchClassName
	 * @return
	 */
	public PersistentClass searchForClass(String searchClassName) {
		return getConfig().getClassMapping(searchClassName);
	}

	/**
	 * @param searchClassName
	 * @return
	 */
	public PersistentClass searchForClass(Class<? extends IPersistable> searchClassName) {
		return getConfig().getClassMapping(searchClassName.getName());
	}

	/**
	 * This HibernateUtility does not know of any other HibernateUtitilities (e.g. for delegation) so the instance itself is
	 * returned if a class mapping for this class can be found
	 */
	public IPersistenceUtility getDelegatePersistenceUtilityForClass(String searchClassName) {
		IPersistenceUtility foundPU = null;
		if (searchForClass(searchClassName) != null) {
			foundPU = this;
		}
		return foundPU;
	}

	public IPersistenceUtility getDelegatePersistenceUtilityForClass(Class<? extends IPersistable> searchClass) {
		IPersistenceUtility foundPU = this;
		if (searchForClass(searchClass) != null) {
			foundPU = this;
		}
		return foundPU;
	}

	@SuppressWarnings("unchecked")
	public void printSessionStatistic(PrintStream out) {
		if (out == null)
			out = System.out;

		SessionStatistics statistics = getSession().getStatistics();
		int entityCount = statistics.getEntityCount();
		Collection<EntityKey> entityKeys = statistics.getEntityKeys();
		int collCount = statistics.getCollectionCount();
		Collection<CollectionKey> collKeys = statistics.getCollectionKeys();

		out.println("----------------------------------------------------------------");
		out.println("|           Session Statistic                                  |");
		out.println("----------------------------------------------------------------");
		out.println("|");
		out.println("|  Session: " + getSession().toString());
		out.println("|");
		out.println("|  Entity Count:     " + entityCount);
		out.println("|  Entity Keys:      " + entityKeys);
		out.println("|");
		out.println("|  Collection Count: " + collCount);
		out.println("|  Collection Keys:  " + collKeys);
		out.println("----------------------------------------------------------------");
	}

	public void attach(IPersistable po) {
		Session session = getSession();

		if (!session.contains(po)) {
			try {
				session.buildLockRequest(LockOptions.NONE).lock(po);
			}
			catch (TransientObjectException ex) {
				// object is not in the data base -> it is new
			}
			catch (NonUniqueObjectException ex) {
				try {
					// remove other object and lock current
					Object other = session.get(ex.getEntityName(), ex.getIdentifier());
					session.evict(other);
					session.buildLockRequest(LockOptions.NONE).lock(po);
				}
				catch (Throwable e) {
					printSessionStatistic(System.out);
					e.printStackTrace();
					LogFactory.getLog(getClass()).fatal("Error during resolving NonUniqueObjectException on attach", e);
				}
			}
			catch (HibernateException ex) {
				log.error(ex.getMessage() + ": Object: " + po.getClass().getSimpleName() + " " + po.getId());
				throw ex;
			}

		}
	}

	private void loadAttributeLength(Class entity) {
		Map<String, Integer> attributeLengths = this.attributeLengthByClass.get(entity);
		if (config != null && attributeLengths == null) {
			// String className =
			// HibernateProxyHelper.getClassWithoutInitializingProxy
			// (this).getName();
			String className = entity.getName();
			if (className.indexOf("$$") > 0) {
				className = className.substring(0, className.indexOf("$$"));
			}
			attributeLengths = new HashMap<String, Integer>();
			this.attributeLengthByClass.put(entity, attributeLengths);
			loadProperties(config, className, attributeLengths);
		}
		else {
			attributeLengths = new HashMap<String, Integer>();
		}
	}

	/*
	 * recursive loading of the property length
	 */
	@SuppressWarnings("unchecked")
	private void loadProperties(Configuration hibernateConfig, String className, Map<String, Integer> attributeLengths) {
		try {
			if (Persistable.class.getName().equals(className)) {
				return;
			}
			else {
				// collect the parent properties first
				String superClass = Class.forName(className).getSuperclass().getName();
				loadProperties(hibernateConfig, superClass, attributeLengths);
			}
			// get mapping class
			PersistentClass persClass = hibernateConfig.getClassMapping(className);
			if (persClass != null) {
				Iterator<Property> propIter = persClass.getPropertyIterator();
				// iteration over the properties and collect the strings
				while (propIter.hasNext()) {
					Property prop = propIter.next();
					if (prop.getValue() != null && prop.getValue() instanceof SimpleValue) {
						SimpleValue value = (SimpleValue)prop.getValue();
						if (!"java.lang.String".equals(value.getTypeName()) && !"text".equalsIgnoreCase(value.getTypeName())) {
							continue;
						}
						Iterator<org.hibernate.mapping.Column> iter = value.getColumnIterator();
						if (iter.hasNext()) {
							org.hibernate.mapping.Column col = iter.next();
							attributeLengths.put(prop.getName(), col.getLength());
						}
					}
					else if (prop.getValue() != null && prop.getValue().getClass().equals(Map.class)) {
						org.hibernate.mapping.Map value = (org.hibernate.mapping.Map)prop.getValue();
						if (value.getIndex().getColumnIterator().hasNext()) {
							org.hibernate.mapping.Column indexCol = (org.hibernate.mapping.Column)value.getIndex().getColumnIterator().next();
							attributeLengths.put(prop.getName() + ".index", indexCol.getLength());
						}
						if (value.getElement().getColumnIterator().hasNext()) {
							org.hibernate.mapping.Column elementCol = (org.hibernate.mapping.Column)value.getElement().getColumnIterator().next();
							attributeLengths.put(prop.getName() + ".element", elementCol.getLength());
						}
					}
				}
			}
		}
		catch (ClassNotFoundException e) {
			// should not happen
			LogFactory.getLog(this.getClass()).error("Error Loading class", e);
		}
	}

	/**
	 * @return the length of the String properties from the hibernate configuration
	 */
	public int getPropertyLength(IPersistable po, String propertyName) {
		Map<String, Integer> attributeLengths = this.attributeLengthByClass.get(po.getClass());
		if (attributeLengths == null) {
			// just call once
			loadAttributeLength(po.getClass());
			attributeLengths = this.attributeLengthByClass.get(po.getClass());
		}
		if (attributeLengths == null) {
			// should not happen
			return -1;
		}
		Integer length = attributeLengths.get(propertyName);
		if (length == null) {
			return -1;
		}
		return length;
	}

	public class PersistenceInterceptor extends EmptyInterceptor {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getEntity(String entityName, Serializable id) {
			// get the HibernateUtility from the current thread isRefresh on this one does not work correctly because it is the HU
			// where the configuration was loaded
			PersistenceManager pm = PersistenceContext.getPersistenceManager();
			if (pm == null) {
				// seems to run in different thread
				if (isUsedForSearchIndex()) {
					return null;
				}
				else {
					throw new RuntimeException("PersistenceManager must be set in context");
				}
			}
			HibernateUtility hu = (HibernateUtility)pm.getPersistenceUtility().getDelegatePersistenceUtilityForClass(entityName);

			// if refresh, don't use cache!
			if (hu != null && hu.isRefresh())
				return null;

			pm = PersistenceContext.getPersistenceManager();
			// if there was no attach() before, try to lazy attach objects
			if (!pm.isAttached() && id instanceof Long) {
				Set<Persistable> objectsInUse = pm.getObjectsInUse();
				for (IPersistable persistable : objectsInUse) {
					if (persistable.getId() == (Long)id) {
						Class<?> theClass = HibernateProxyHelper.getClassWithoutInitializingProxy(persistable);
						if (theClass.getName().equals(entityName)) {
							return persistable;
						}
					}
				}
			}
			return super.getEntity(entityName, id);
		}
	}

	protected boolean isRefresh() {
		return isRefresh;
	}

	public void setUsedForSearchIndex(boolean usedForSearchIndex) {
		this.usedForSearchIndex = usedForSearchIndex;
	}

	public boolean isUsedForSearchIndex() {
		return usedForSearchIndex;
	}

}
