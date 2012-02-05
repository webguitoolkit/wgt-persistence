package org.webguitoolkit.persistence.test;

import java.io.FileNotFoundException;

import javax.xml.parsers.FactoryConfigurationError;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.util.HibernatePersistenceFrameworkInitializer;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;


public abstract class BaseHibernateTest extends TestCase {
	
	private PersistenceManager persistenceManager;

	/**
	 * Allow both constructors as defined by the JUnit superclass
	 * 
	 * @param name
	 */
	public BaseHibernateTest(String name) {
		super(name);
	}

	public BaseHibernateTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		initializeLog4jConfiguration();
		Configuration config = createHibernateConfiguration(true);
		
		HibernatePersistenceFrameworkInitializer hpfi = createInitializer();
		initializePersistenceFramework(hpfi, config, getHibernateConfigId() );
	}

	@Override
	protected void tearDown() throws Exception {
		getPersistenceManager().rollback();
		getPersistenceManager().close();
		PersistenceContext.setPersistenceManager(null);
		
		// forces Cobertura flush if JUnit tests run in the same VM as Maven
//		try {
//			String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
//			String methodName = "saveGlobalProjectData";
//			Class saveClass = Class.forName(className);
//			Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
//			saveMethod.invoke(null, new Object[0]);
//		}
//		catch (Throwable localThrowable) {
//		}
		
		super.tearDown();
	}

	/**
	 * @return
	 */
	protected String getHibernateConfigId() {
		return "defaultConfig";
	}
	
	protected void initializeLog4jConfiguration() throws FileNotFoundException, FactoryConfigurationError {
		if (StringUtils.isNotEmpty(getLog4jXmlFilename())) {
			DOMConfigurator.configure(HibernatePersistenceFrameworkInitializer.getAbsoluteFileName(getLog4jXmlFilename(), getClass()));
		}
		
	}

	protected String getLog4jXmlFilename() {
		return null;
	}

	/**
	 * @param config
	 */
	protected void initializePersistenceFramework(HibernatePersistenceFrameworkInitializer hpfi, Configuration config, String configId ) {
		setPersistenceManager(hpfi.initialize(config, configId, getAnnotatedClasses(), true));
	}

	/**
	 * @return
	 */
	protected HibernatePersistenceFrameworkInitializer createInitializer() {
		return new HibernatePersistenceFrameworkInitializer();
	}

	/**
	 * @return
	 */
	protected PersistenceManager createPersistenceManager(IPersistenceUtility associatedHU) {
		return new PersistenceManager(associatedHU);
	}

	/**
	 * @return
	 */
	protected Configuration createHibernateConfiguration( boolean createDrop ) {
		Configuration config = new Configuration().
			setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect").
		    setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver").
		    setProperty("hibernate.connection.url", "jdbc:derby:memory:test;create=true").
		    setProperty("hibernate.connection.username", "sa").
		    setProperty("hibernate.connection.password", "").
		    setProperty("hibernate.connection.pool_size", "1").
		    setProperty("hibernate.connection.autocommit", "true").
		    setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
		    if( createDrop )
		    	config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		    config.setProperty("hibernate.show_sql", "true");
		return config;
	}
	
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
	protected Session getSession() {
		return ((HibernateUtility)getPersistenceUtility()).getSession();
	}
	
	protected abstract Class<IPersistable>[] getAnnotatedClasses();

	/**
	 * @param newPersistenceManager the persistenceManager to set
	 */
	protected void setPersistenceManager(PersistenceManager newPersistenceManager) {
		persistenceManager = newPersistenceManager;
	}
	

	protected boolean isAttached(IPersistable entity ){
		return getSession().contains(entity);
	}
	
	protected void reinitPersistenceManager(){
		Configuration config = createHibernateConfiguration(false);
		HibernatePersistenceFrameworkInitializer hpfi = createInitializer();
		setPersistenceManager(hpfi.initialize(config, getHibernateConfigId(), getAnnotatedClasses(), false));
	}

}
