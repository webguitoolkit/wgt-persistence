package org.webguitoolkit.persistence.test.model;

import java.sql.Time;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.cfg.Configuration;
import org.hibernate.hql.ast.QuerySyntaxException;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.model.IQueryFactory;
import org.webguitoolkit.persistence.test.TestPersistenceUtilityWrapper;
import org.webguitoolkit.persistence.test.testobjects.ITextable;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject2;
import org.webguitoolkit.persistence.util.CompositeHibernateUtility;
import org.webguitoolkit.persistence.util.HibernatePersistenceFrameworkInitializer;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;


public class MultiplePMTest extends TestCaseForPFW {
	
	public MultiplePMTest() {	
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#initializePersistenceFramework(org.hibernate.cfg.Configuration)
	 */
	@Override
	protected void initializePersistenceFramework(HibernatePersistenceFrameworkInitializer hpfi, Configuration config, String configId) {
		super.initializePersistenceFramework(hpfi, config, configId );
		
		// set-up own HibernateUtility first so it can be used during the factory methods when 
		// the PersistenceManager is created
		Configuration m2Config = createHibernateConfiguration(true);
		m2Config.setProperty("hibernate.connection.url", "jdbc:derby:memory:test2;create=true");
		
		m2Config.addAnnotatedClass( TestObject2.class );

		SchemaExport ddl = new SchemaExport(m2Config);
		ddl.create(true, true);
		
		HibernateUtility m2HU = new TestPersistenceUtilityWrapper(new HibernateUtility(m2Config, configId+"2" ));
		hpfi.addHibernateUtility(m2HU);

	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#createInitializer()
	 */
	@Override
	protected HibernatePersistenceFrameworkInitializer createInitializer() {
		// create a special (anonymous) class which customizes the Initializer further
		return new HibernatePersistenceFrameworkInitializer() {
			@Override
			protected HibernateUtility createHibernateUtility(Configuration config, String configId ) {
				return new TestPersistenceUtilityWrapper(super.createHibernateUtility(config, configId ));
			}
		};
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#getLog4jXmlFilename()
	 */
	@Override
	protected String getLog4jXmlFilename() {
		return TestCaseForPFW.LOG4J_PFW_XML_FILE;
	}
	
	/**
	 * Test the creation of new objects, commit and close. 
	 * Special care is taken that the object is created with the correct PersistenceManager.
	 * 
	 * @throws Exception
	 */
	public void testInsert() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceManager().getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		TestObject message = new TestObject("JPA Message1 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		checkAttachedObject(m1HU, message);
		checkNotAttachedObject(m2HU, message);
		getPersistenceManager().close();
		
		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		TestObject2 message2 = new TestObject2("JPA TestObject2 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		checkAttachedObject(m2HU, message2);
		checkNotAttachedObject(m1HU, message2);
		getPersistenceManager().close();
				
		// test parallel insert
		activateHibernateUtility(m1HU);
		activateHibernateUtility(m2HU);
		message = new TestObject("JPA Message3 at " + new Time(System.currentTimeMillis()));
		message2 = new TestObject2("JPA Message4 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		checkAttachedObject(m1HU, message);
		checkAttachedObject(m2HU, message2);
		getPersistenceManager().close();
	}

	/**
	 * Test if new objects are marked as new (creation aspect) , that they are
	 * registered. Test if a query re-registers the object (registration aspect).
	 * 
	 * @throws Exception
	 */
	public void testRegistration() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getCanonicalName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getCanonicalName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		TestObject message = new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		checkRegisteredObject(m1HU, m2HU, message);
				
		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		TestObject2 message2 = new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		checkRegisteredObject(m2HU, m1HU, message2);
	}

	/**
	 * Test detach and attach. 
	 * Special care is taken that the object is attached to the correct PersistenceManager.
	 * 
	 * @throws Exception
	 */
	public void testAttach() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getCanonicalName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getCanonicalName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		TestObject message = new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		attachObject(m1HU, message);
		checkNotAttachedObject(m2HU, message);
				
		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		TestObject2 message2 = new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		attachObject(m2HU, message2);
		checkNotAttachedObject(m1HU, message2);
	}

	public void testUpdate() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getCanonicalName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getCanonicalName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		TestObject message = new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		checkUpdatedObject(m1HU, message);
		
		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		TestObject2 message2 = new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		checkUpdatedObject(m2HU, message2);
	
		// test parallel update
		getPersistenceManager().close();
		activateHibernateUtility(m1HU);
		activateHibernateUtility(m2HU);
		QueryFactory queryFactory = new QueryFactory(m1HU);
		QueryFactory queryFactory2 = new QueryFactory(m2HU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(TestObject.class).execute();
		Collection<IPersistable> result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		ITextable changeMessage = (ITextable) result.iterator().next();
		ITextable changeMessage2 = (ITextable) result2.iterator().next();
		String newtext = "Simultanous Change: " + message.getText();
		changeMessage.setText(newtext);
		String newtext2 = "Simultanous Change: " + message2.getText();
		changeMessage2.setText(newtext2);
		assertEquals("is not marked changed",  ModificationType.CHANGED, changeMessage.getModificationType());
		assertEquals("is not marked changed",  ModificationType.CHANGED, changeMessage2.getModificationType());
		getPersistenceManager().commit();
		getPersistenceManager().close();
		assertNotSame("is marked changed",  ModificationType.CHANGED, message.getModificationType());
		assertNotSame("is marked changed",  ModificationType.CHANGED, message2.getModificationType());
		result = queryFactory.newHQLQuery(TestObject.class).execute();
		result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		assertEquals("wrong result size ", 1, result.size());
		assertEquals("wrong result size ", 1, result2.size());
		changeMessage = (ITextable) result.iterator().next();
		changeMessage2 = (ITextable) result2.iterator().next();
		assertEquals("text new? ", newtext, changeMessage.getText());
		assertEquals("text new? ", newtext2, changeMessage2.getText());
		assertNotSame("is marked changed",  ModificationType.CHANGED, changeMessage.getModificationType());
		assertNotSame("is marked changed",  ModificationType.CHANGED, changeMessage2.getModificationType());
	}

	public void testDelete() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getCanonicalName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getCanonicalName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		ITextable message = new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		checkDeletedObject(m1HU, message);
		
		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		ITextable message2 = new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		checkDeletedObject(m2HU, message2);
		
		// test parallel update
		getPersistenceManager().close();
		activateHibernateUtility(m1HU);
		activateHibernateUtility(m2HU);
		message = new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		message2 = new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		getPersistenceManager().close();
		QueryFactory queryFactory = new QueryFactory(m1HU);
		QueryFactory queryFactory2 = new QueryFactory(m2HU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(TestObject.class).execute();
		Collection<IPersistable> result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		IPersistable foundMessage = (IPersistable) result.iterator().next();
		IPersistable foundMessage2 = (IPersistable) result2.iterator().next();
		foundMessage.markDeleted();
		foundMessage2.markDeleted();
		assertEquals("is not marked deleted",  ModificationType.DELETED, foundMessage.getModificationType());
		assertEquals("is not marked deleted",  ModificationType.DELETED, foundMessage2.getModificationType());
		getPersistenceManager().commit();
		result = queryFactory.newHQLQuery(TestObject.class).execute();
		result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		assertEquals("result size ", 0, result.size());
		assertEquals("result size ", 0, result2.size());
	}

	@SuppressWarnings("unchecked")
	public void testQuery() throws Exception {
		CompositeHibernateUtility combinedPU = (CompositeHibernateUtility) getPersistenceUtility();
		IPersistenceUtility m1HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject.class.getCanonicalName());
		IPersistenceUtility m2HU = combinedPU.getDelegatePersistenceUtilityForClass(TestObject2.class.getCanonicalName());
		
		activateHibernateUtility(m1HU);
		deactivateHibernateUtility(m2HU);
		new TestObject("JPA Message at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();

		QueryFactory queryFactory = new QueryFactory(m1HU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(TestObject.class).execute();
		assertEquals("result size ", 1, result.size());

		activateHibernateUtility(m2HU);
		deactivateHibernateUtility(m1HU);
		new TestObject2("JPA Message2 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		QueryFactory queryFactory2 = new QueryFactory(m2HU);
		Collection<IPersistable> result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		assertEquals("wrong result size ", 1, result2.size());
		
		// test parallel query
		getPersistenceManager().close();
		activateHibernateUtility(m1HU);
		activateHibernateUtility(m2HU);
		result = queryFactory.newHQLQuery(TestObject.class).execute();
		result2 = queryFactory2.newHQLQuery(TestObject2.class).execute();
		ITextable changeMessage = (ITextable) result.iterator().next();
		ITextable changeMessage2 = (ITextable) result2.iterator().next();
		assertFalse("Messages should be different from each other", changeMessage.equals(changeMessage2));
		
		// test with wrong HibernateUtility
		try {
			result = queryFactory.newHQLQuery(TestObject2.class).execute();
		}
		catch (QuerySyntaxException e) {
			assertTrue("Class should not be found in HibernateUtility", e.getMessage().indexOf("not mapped") > 0);
		}
		try {
			result2 = queryFactory2.newHQLQuery(TestObject.class).execute();
		}
		catch (QuerySyntaxException e) {
			assertTrue("Class should not be found in HibernateUtility", e.getMessage().indexOf("not mapped") > 0);
		}
		
		Criteria selectCriteria = ((HibernateUtility)m1HU).getSession().createCriteria(TestObject.class);
		List<? extends IPersistable> resultList = selectCriteria.list();
		assertEquals("wrong result size", 1, resultList.size());
	}
	
	/**
	 * 
	 */
	protected void activateHibernateUtility(IPersistenceUtility modifyHU) {
		((TestPersistenceUtilityWrapper)modifyHU).setActivated(true);
	}
	
	/**
	 * 
	 */
	protected void deactivateHibernateUtility(IPersistenceUtility modifyHU) {
		((TestPersistenceUtilityWrapper)modifyHU).setActivated(false);
	}
		
	/**
	 * @throws PersistenceException
	 */
	protected void checkDeletedObject(IPersistenceUtility checkHU, ITextable checkObject) throws PersistenceException {
		Class<? extends ITextable> resultObjectClass = checkObject.getClass();
		getPersistenceManager().commit();
		getPersistenceManager().close();

		QueryFactory queryFactory = new QueryFactory(checkHU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(resultObjectClass).execute();
		assertEquals("result size ", 1, result.size());
		IPersistable foundMessage = (IPersistable) result.iterator().next();
		foundMessage.markDeleted();
		assertEquals("is not marked deleted",  ModificationType.DELETED, foundMessage.getModificationType());
		getPersistenceManager().commit();
		result = queryFactory.newHQLQuery(resultObjectClass).execute();
		assertEquals("result size ", 0, result.size());
	}
	
	/**
	 * @param message
	 * @throws PersistenceException
	 */
	protected void checkUpdatedObject(IPersistenceUtility checkHU, ITextable checkObject) throws PersistenceException {
		Class<? extends ITextable> resultObjectClass = checkObject.getClass();
		
		getPersistenceManager().commit();
		getPersistenceManager().close();

		QueryFactory queryFactory = new QueryFactory(checkHU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(resultObjectClass).execute();
		assertEquals("result size ", 1, result.size());
		ITextable changeMessage = (ITextable) result.iterator().next();
		String newtext = "Changed : " + changeMessage.getText();
		changeMessage.setText(newtext);
		assertEquals("is not marked changed",  ModificationType.CHANGED, changeMessage.getModificationType());
		getPersistenceManager().commit();
		getPersistenceManager().close();
		
		result = queryFactory.newHQLQuery(resultObjectClass).execute();
		assertEquals("result size ", 1, result.size());
		changeMessage = (ITextable) result.iterator().next();
		assertEquals("text new? ", newtext, changeMessage.getText());
		assertNotSame("is marked changed",  ModificationType.CHANGED, changeMessage.getModificationType());
	}
	
	/**
	 * @param checkObject
	 * @throws PersistenceException
	 */
	@SuppressWarnings("unchecked")
	protected void checkRegisteredObject(IPersistenceUtility checkHU, IPersistenceUtility wrongHU, IPersistable checkObject) throws PersistenceException {
		Class<? extends IPersistable> resultObjectClass = checkObject.getClass();
		assertEquals("is not marked new",  ModificationType.NEW, checkObject.getModificationType());
		checkNotAttachedObject(checkHU, checkObject);
		checkNotAttachedObject(wrongHU, checkObject);
		getPersistenceManager().commit();
		checkAttachedObject(checkHU, checkObject);
		checkNotAttachedObject(wrongHU, checkObject);
		assertNotSame("is marked new",  ModificationType.NEW, checkObject.getModificationType());

		getPersistenceManager().close();

		QueryFactory queryFactory = new QueryFactory(checkHU);
		Collection<IPersistable> result = queryFactory.newHQLQuery(resultObjectClass).execute();
		assertEquals("result size ", 1, result.size());
		
		try {
			Collection<IPersistable> emptyCollection = ((HibernateUtility)wrongHU).getSession().createQuery("from " + resultObjectClass.getSimpleName()).list();
			assertNotNull(emptyCollection);
			//assertEquals("result size from wrong PM", 0, emptyCollection.size());
			fail("Class should not be mapped in wrong PersistenceManager");
		}
		catch (QuerySyntaxException exc) {
			// expected exception
		}
	}
	
	/**
	 * @param checkObject
	 * @throws PersistenceException
	 */
	protected void attachObject(IPersistenceUtility checkHU, IPersistable checkObject) throws PersistenceException {
		assertEquals("is not marked new",  ModificationType.NEW, checkObject.getModificationType());
		getPersistenceManager().commit();
		assertNotSame("is marked new",  ModificationType.NEW, checkObject.getModificationType());
		getPersistenceManager().detach();
		checkNotAttachedObject(checkHU, checkObject);

		getPersistenceManager().close();

		checkNotAttachedObject(checkHU, checkObject);
	}

	/**
	 * @param checkObject
	 * @param checkHU
	 */
	protected void checkAttachedObject(IPersistenceUtility checkHU, IPersistable checkObject) {
		HibernateUtility sessionHU = getHibernateUtilityForClass(checkHU, checkObject);		
		assertTrue(((HibernateUtility)sessionHU).getSession().contains(checkObject));
	}
	
	protected void checkNotAttachedObject(IPersistenceUtility checkHU, IPersistable checkObject) {
		HibernateUtility sessionHU = getHibernateUtilityForClass(checkHU, checkObject);
		assertFalse(((HibernateUtility)sessionHU).getSession().contains(checkObject));
	}
	
	/**
	 * @param checkHU
	 * @param checkObject
	 * @return
	 */
	protected HibernateUtility getHibernateUtilityForClass(IPersistenceUtility checkHU, IPersistable checkObject) {
		IPersistenceUtility sessionHU = checkHU;
		// unwrap (Composite)HibernateUtility if necessary
		if (sessionHU instanceof TestPersistenceUtilityWrapper) {
			sessionHU = ((TestPersistenceUtilityWrapper)sessionHU).getWrappedPersistenceUtility();
		}
		if (sessionHU instanceof CompositeHibernateUtility) {
			sessionHU = ((CompositeHibernateUtility)sessionHU).getDelegatePersistenceUtilityForClass(checkObject.getClass().getCanonicalName());
		}
		return (HibernateUtility)sessionHU;
	}
	
	class QueryFactory implements IQueryFactory {
		private IPersistenceUtility queryHU;
		
		public QueryFactory(IPersistenceUtility newQueryHU) {
			queryHU = newQueryHU;
		}
		public <T extends IPersistable> HQLQuery<T> newHQLQuery(Class<T> newResultObjectClass) {
			return new HQLQuery<T>(newResultObjectClass, queryHU);
		}
	}

	class HQLQuery<T extends IPersistable> {

		private Class<T> resultObjectClass;
		private IPersistenceUtility queryHU;
		
		public HQLQuery(Class<T> newResultObjectClass, IPersistenceUtility newQueryHU) {
			resultObjectClass = newResultObjectClass;
			queryHU = newQueryHU;
		}

		@SuppressWarnings("unchecked")
		public Collection<IPersistable> execute() {
			return ((HibernateUtility)queryHU).getSession().createQuery("from " + resultObjectClass.getSimpleName()).list();
		}
	}
}
