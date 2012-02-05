/**
 * 
 */
package org.webguitoolkit.persistence.util;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.strategy.ValidityAuditStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;


/**
 * A Helper class which runs through all steps required for initialization of the PersistenceFramework 2.
 * <p/>
 * The Helper class has two purposes:
 * <p/>
 * <ul>
 * <li>
 * Simplify the initialization process by using <code>
 *   HibernatePersistenceFrameworkInitializer hpfi = new HibernatePersistenceFrameworkInitializer().initialize(hibernateConfig, getAnnotatedClasses());
 * </code> instead of <code>
 *   HibernateUtility hu = new HibernateUtility(getAnnotatedClasses(), hibernateConfig);
 *   PersistenceManager pm = new PersistenceManager(hu);
 *   PersistenceContext.setPersistenceManager(pm);
 * </code></li>
 * <li>
 * Enable the usage of several database backends (-> different HibernateUtility configurations): <code>
		Configuration config2 = ...
		//config2.addAnnotatedClass( PersistentObject2.class );
		HibernateUtility hu2 = new HibernateUtility(config2, "hibernateUtility2" );
		hpfi.addHibernateUtility(nu2);
 *   </code></li>
 * </ul>
 * 
 * @author i102389
 */
public class HibernatePersistenceFrameworkInitializer {

	public static final String HIBERNATE_NAMESCHEME_DELIMITER = "-";
	public static final String JNDI_HIERARCHY_SEPARATOR = "/";

	/**
	 * 
	 */
	public HibernatePersistenceFrameworkInitializer() {
	}

	/**
	 * @param hibernateCfgXml
	 */
	@SuppressWarnings("unchecked")
	public PersistenceManager initialize(String hibernateCfgXml) {
		return initialize(hibernateCfgXml, new Class[0], false);
	}

	public PersistenceManager initialize(String hibernateCfgXml, Class<? extends IPersistable>[] annotatedClasses) {
		return initialize(hibernateCfgXml, annotatedClasses, false);
	}

	@SuppressWarnings("unchecked")
	public PersistenceManager initialize(String hibernateCfgXml, boolean isCreateDB) {
		return initialize(hibernateCfgXml, new Class[0], isCreateDB);
	}

	/**
	 * @param hibernateCfgXml
	 */
	public PersistenceManager initialize(String hibernateCfgXml, Class<? extends IPersistable>[] annotatedClasses, boolean isCreateDB) {
		return initialize(createHibernateConfiguration(hibernateCfgXml), hibernateCfgXml, annotatedClasses, isCreateDB);
	}

	public PersistenceManager initialize(Configuration config, String configId, Class<? extends IPersistable>[] annotatedClasses,
			boolean isCreateDB) {
		determineHibernateDialectFromJNDI(config);

		registerPersistableClasses(config, annotatedClasses);

		// additional configuration step to get Revision tables created
		// TODO: refactor to remove library dependency on Hibernate Envers 
		// ValidityAuditStrategy offer better performance for querying revision results (see http://www.warski.org/blog/?p=261) 
		config.setProperty("org.hibernate.envers.audit_strategy", ValidityAuditStrategy.class.getName());
		config.buildMappings(); // necessary despite getFor() calling buildMappings() as well - but too late?
		AuditConfiguration.getFor(config);

		if (isCreateDB) {
			createDbStructures(config);
		}

		IPersistenceUtility hu = createHibernateUtility(config, configId);
		PersistenceManager pm = createPersistenceManager(hu);
		PersistenceContext.setPersistenceManager(pm);

		return pm;
	}

	/**
	 * @param config
	 */
	protected void determineHibernateDialectFromJNDI(Configuration config) {
		try {
			InitialContext jndiContext = new InitialContext();
			String configDatasource = (String)config.getProperty(Environment.DATASOURCE);
			if (StringUtils.isNotEmpty(configDatasource)
					&& (configDatasource.lastIndexOf(JNDI_HIERARCHY_SEPARATOR) < configDatasource.length())) {
				String datasourceName = configDatasource.substring(configDatasource.lastIndexOf(JNDI_HIERARCHY_SEPARATOR) + 1);

				String datasourceHibernateDialect = (String)jndiContext.lookup("java:comp/env/hibernateDialect"
						+ HIBERNATE_NAMESCHEME_DELIMITER + datasourceName);
				if (StringUtils.isNotEmpty(datasourceHibernateDialect)) {
					Properties additionalProperties = new Properties();
					additionalProperties.put(Environment.DIALECT, datasourceHibernateDialect);
					config.addProperties(additionalProperties);
				}
			}
		}
		catch (NamingException e) {
			LogFactory.getLog(getClass()).debug("Error while searching Hibernate dialect property in JNDI", e);
		}
	}

	/**
	 * Plugin-method
	 * 
	 * @param config
	 */
	protected void registerPersistableClasses(Configuration config, Class<? extends IPersistable>[] annotatedClasses) {
		for (Class<? extends IPersistable> theClass : annotatedClasses) {
			config.addAnnotatedClass(theClass);
		}
	}

	/**
	 * @param config
	 */
	protected void createDbStructures(Configuration config) {
		SchemaExport ddl = new SchemaExport(config);
		ddl.create(true, true);
	}

	/**
	 * @param hu
	 * @return
	 */
	protected PersistenceManager createPersistenceManager(IPersistenceUtility hu) {
		return new PersistenceManager(hu);
	}

	/**
	 * @param config
	 * @return
	 */
	protected HibernateUtility createHibernateUtility(Configuration config, String configId) {
		return new HibernateUtility(config, configId);
	}

	public void addHibernateUtility(HibernateUtility newHibernateUtility) {
		IPersistenceUtility currentPU = PersistenceContext.getPersistenceManager().getPersistenceUtility();

		if (currentPU instanceof CompositeHibernateUtility) {
			// avoid adding the same HibernateUtility twice - this is done in the addDelegatePersistenceUtility method
			((CompositeHibernateUtility)currentPU).addDelegatePersistenceUtility(newHibernateUtility);
		}
		else if (currentPU instanceof HibernateUtility) {
			// avoid adding the same HibernateUtility twice
			if (newHibernateUtility != currentPU) {
				// if there has been only one HibernateUtility so far then replace it with a CompositeHibernateUtility and
				// register the old HibernateUtility with the CompositeHibernateUtility
				CompositeHibernateUtility newCompositeHU = new CompositeHibernateUtility();
				// add existing HibernateUtility
				newCompositeHU.addDelegatePersistenceUtility((HibernateUtility)currentPU);
				// add new HibernateUtility
				newCompositeHU.addDelegatePersistenceUtility(newHibernateUtility);
				PersistenceContext.getPersistenceManager().setPersistenceUtility(newCompositeHU);
			}
		}
		else {
			throw new RuntimeException("Cannot add another HibernateUtility to an non-HibernateUtility");
		}
	}

	public void removeHibernateUtility(HibernateUtility oldHibernateUtility) {
		IPersistenceUtility currentPU = PersistenceContext.getPersistenceManager().getPersistenceUtility();

		if (currentPU instanceof CompositeHibernateUtility) {
			// remove only if there is still at least one other PU left
			((CompositeHibernateUtility)currentPU).removeDelegatePersistenceUtility(oldHibernateUtility);
			// TODO: "Unset" the CompositeHibernateUtility if there is exactly one PU left (to avoid unnecessary delegated method
			// calls
		}
		else if (currentPU == oldHibernateUtility) {
			// cannot remove the only remaining HibernateUtility
		}
		else {
			// cannot remove the HibernateUtility because it is not the one currently set
		}
	}

	protected Configuration createHibernateConfiguration(String hibernateConfigFile) {
		Configuration fileConfiguration = new Configuration();
		fileConfiguration.configure(hibernateConfigFile);
		return fileConfiguration;
	}

	/**
	 * Obtains the absolute path to a relative file name. The relative filename is searched in the class path in relation to where
	 * the class file of this class is located. If the relative filename starts with with a directory root ('/') then the
	 * directory root is the root of the classpath.
	 */
	public static String getAbsoluteFileName(String strRelativeFileName, Class<?> callerClass) throws FileNotFoundException {
		URL absoluteFileUrl = callerClass.getResource(strRelativeFileName);
		if (absoluteFileUrl == null) {
			throw new FileNotFoundException("File not found (relative to class file): " + strRelativeFileName + " [class="
					+ callerClass.getName() + "]");
		}
		return absoluteFileUrl.getFile().replaceAll("%20", " ");
	}

	public static URL getAbsoluteFileNameAsURL(String strRelativeFileName, Class<?> callerClass) throws FileNotFoundException {
		URL absoluteFileUrl = callerClass.getResource(strRelativeFileName);
		if (absoluteFileUrl == null) {
			throw new FileNotFoundException("File not found (relative to class file): " + strRelativeFileName + " [class="
					+ callerClass.getName() + "]");
		}
		return absoluteFileUrl;
	}
}
