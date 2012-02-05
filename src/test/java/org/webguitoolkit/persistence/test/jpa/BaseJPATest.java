package org.webguitoolkit.persistence.test.jpa;

import java.io.FileNotFoundException;

import javax.persistence.EntityManager;
import javax.xml.parsers.FactoryConfigurationError;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.util.IPersistenceUtility;
import org.webguitoolkit.persistence.util.JPAUtility;


public abstract class BaseJPATest extends TestCase {
	
	private PersistenceManager persistenceManager;
	private JPAUtility jpaUtility;

	/**
	 * Allow both constructors as defined by the JUnit superclass
	 * 
	 * @param name
	 */
	public BaseJPATest(String name) {
		super(name);
	}

	public BaseJPATest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		initializeLog4jConfiguration();
		
		jpaUtility = new JPAUtility("junit-test");
		persistenceManager = new PersistenceManager( jpaUtility );
		PersistenceContext.setPersistenceManager(persistenceManager);
	}

	@Override
	protected void tearDown() throws Exception {
		getPersistenceManager().rollback();
		getPersistenceManager().close();
		PersistenceContext.setPersistenceManager(null);		
		super.tearDown();
	}

	/**
	 * @return
	 */
	protected JPAUtility getJPAUtility() {
		return jpaUtility;
	}
	
	protected void initializeLog4jConfiguration() throws FileNotFoundException, FactoryConfigurationError {
		if (StringUtils.isNotEmpty(getLog4jXmlFilename())) {
			DOMConfigurator.configure(getClass().getResource(getLog4jXmlFilename()).getFile().replaceAll("%20", " "));
		}
	}

	protected String getLog4jXmlFilename() {
		return null;
	}

	/**
	 * @return
	 */
	protected PersistenceManager createPersistenceManager(IPersistenceUtility associatedHU) {
		return new PersistenceManager(associatedHU);
	}

//	/**
//	 * @return
//	 */
//	protected Configuration createHibernateConfiguration() {
//		Configuration config = new Configuration().
//			setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect").
//		    setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver").
//		    setProperty("hibernate.connection.url", "jdbc:derby:memory:test;create=true").
//		    setProperty("hibernate.connection.username", "sa").
//		    setProperty("hibernate.connection.password", "").
//		    setProperty("hibernate.connection.pool_size", "1").
//		    setProperty("hibernate.connection.autocommit", "true").
//		    setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider").
//		    setProperty("hibernate.hbm2ddl.auto", "create-drop").
//		    setProperty("hibernate.show_sql", "true");
//		return config;
//	}
	
	/**
	 * Convenience method
	 * 
	 * @return
	 */
	protected PersistenceManager getPersistenceManager(){
		return persistenceManager;
	}

	/**
	 * Convenience method
	 * 
	 * @return
	 */
	protected IPersistenceUtility getPersistenceUtility(){
		return getPersistenceManager().getPersistenceUtility();
	}

	/**
	 * Convenience method
	 * 
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return jpaUtility.getEntityManager();
	}
	
//	protected abstract Class<IPersistable>[] getAnnotatedClasses();

	/**
	 * @param newPersistenceManager the persistenceManager to set
	 */
	protected void setPersistenceManager(PersistenceManager newPersistenceManager) {
		persistenceManager = newPersistenceManager;
	}
	

	protected boolean isAttached(IPersistable entity ){
		return getEntityManager().contains(entity);
	}

}
