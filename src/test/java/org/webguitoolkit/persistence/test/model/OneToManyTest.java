/**
 * 
 */
package org.webguitoolkit.persistence.test.model;

import java.util.Collection;

import org.hibernate.PropertyValueException;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.BaseHibernateTest;
import org.webguitoolkit.persistence.test.testobjects.RelatedObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


/**
 * @author i102389
 *
 */
public class OneToManyTest extends BaseHibernateTest{

	/**
	 * @param name
	 */
	public OneToManyTest(String name) {
		super(name);
	}

	/**
	 * 
	 */
	public OneToManyTest() {
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#getAnnotatedClasses()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Class<IPersistable>[] getAnnotatedClasses() {
		return new Class[] {TestObject.class, RelatedObject.class};
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#getLog4jXmlFilename()
	 */
	@Override
	protected String getLog4jXmlFilename() {
		return TestCaseForPFW.LOG4J_PFW_XML_FILE;
	}
	
//	@Override
//	protected String getHibernateConfigId() {
//		return getClass().toString() + "-hibernateconfigid";
//	}
	
	public void test01InsertCorrectOrder() throws PersistenceException {
		TestObject poOne = new TestObject("parent");
		RelatedObject poMany = new RelatedObject("child");
		poMany.setMany2one(poOne);
		
		assertSame("Child object should have reference to parent object", poOne, poMany.getMany2one());
		assertTrue("Parent object should have reference to child object", poOne.getOne2many().contains(poMany));
		getPersistenceManager().commit();
		getPersistenceManager().close();
		
		Collection<RelatedObject> res = new AllOfAKindQuery<RelatedObject>(getPersistenceManager(), RelatedObject.class).execute();
		TestObject res1 = res.iterator().next().getMany2one();
		poOne.equals(res1);
		res1.equals(poOne);
	}
	
	public void test02InsertWrongOrder() {
		RelatedObject poMany = new RelatedObject("child");
		TestObject poOne = new TestObject("parent");
		
		// because the child has been created first the parent must be set afterwards
		poMany.setMany2one(poOne);
		
		assertSame("Child object should have reference to parent object", poOne, poMany.getMany2one());
		assertTrue("Parent object should have reference to child object", poOne.getOne2many().contains(poMany));
		
		try {
			getPersistenceManager().commit();
//			fail();
		}
		catch (PersistenceException pexc) {
			assertTrue("Wrong expected exception", pexc.getCause() instanceof PropertyValueException);
		}
	}
	

	public void test03DeleteEmpty() throws PersistenceException {
		TestObject poOne = new TestObject("parent");
		getPersistenceManager().commit();
		
		poOne.markDeleted();
		getPersistenceManager().commit();
	}
	
	public void test04DeleteEmptyAfterReload() throws PersistenceException {
		TestObject poOne = new TestObject("parent");
		getPersistenceManager().commit();
		getPersistenceManager().close();

		reinitPersistenceManager();
				
		// load objects anew from database
		QueryFactory queryFactory = new QueryFactory();
		Collection<IPersistable> result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
		TestObject removePO = (TestObject) result.iterator().next();
		
		// delete it
		removePO.markDeleted();
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poOne.getModificationType());
		assertEquals("Object should be marked as deleted",  ModificationType.DELETED, removePO.getModificationType());
		getPersistenceManager().commit();
		
		getPersistenceManager().attach();
		// check state and object access
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, removePO.getModificationType());
		assertEquals("Accessing attributes should be possible", "parent", removePO.getText());
		//assertEquals("Accessing collection should be possible", 0, removePO.getObjectsMany().size());
	}
	
	public void test05DeleteCascade() throws PersistenceException {
		TestObject poOne = new TestObject("parent");
		RelatedObject poMany1 = new RelatedObject("child1");
		RelatedObject poMany2 = new RelatedObject("child1");
		poMany1.setMany2one(poOne);
		poMany2.setMany2one(poOne);
		
		getPersistenceManager().commit();
		
		poOne.markDeleted();
		assertEquals("Object should not be marked as deleted",  ModificationType.DELETED, poOne.getModificationType());
		assertEquals("Object should not be marked as deleted",  ModificationType.DELETED, poMany1.getModificationType());
		assertEquals("Object should not be marked as deleted",  ModificationType.DELETED, poMany2.getModificationType());
		getPersistenceManager().commit();

		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poOne.getModificationType());
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poMany1.getModificationType());
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poMany2.getModificationType());
	}
	
	public void test06DeleteCascadeAfterReload() throws PersistenceException {
		TestObject poOne = new TestObject("parent");
		RelatedObject poMany1 = new RelatedObject("child1");
		RelatedObject poMany2 = new RelatedObject("child1");
		poMany1.setMany2one(poOne);
		poMany2.setMany2one(poOne);
		
		getPersistenceManager().commit();
		getPersistenceManager().close();
		
		reinitPersistenceManager();
		
		QueryFactory queryFactory = new QueryFactory();
		Collection<IPersistable> result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
		TestObject removePO = (TestObject) result.iterator().next();
		
		removePO.markDeleted();
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poOne.getModificationType());
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poMany1.getModificationType());
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, poMany2.getModificationType());
		assertEquals("Object should be marked as deleted",  ModificationType.DELETED, removePO.getModificationType());
		RelatedObject rel = removePO.getOne2many().iterator().next();
		assertEquals("Object should be marked as deleted",  ModificationType.DELETED, rel.getModificationType());
		getPersistenceManager().commit();
		
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, removePO.getModificationType());
		assertNotSame("Object should not be marked as deleted",  ModificationType.DELETED, rel.getModificationType());
	}
}
