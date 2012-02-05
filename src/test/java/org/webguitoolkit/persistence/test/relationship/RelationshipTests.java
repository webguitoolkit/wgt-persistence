package org.webguitoolkit.persistence.test.relationship;

import org.hibernate.collection.PersistentSet;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.RelatedObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


/**
 * Tests regarding the relationship management. Important here is that even in the transient state of the instances the relations
 * are managed automatically on both sides even if the maintenance happens only on one side.
 * 
 * @author peter
 * 
 */
public class RelationshipTests extends TestCaseForPFW {

	public void testOne2Many() {
		TestObject berlin = new TestObject("Berlin");
		RelatedObject silo1 = new RelatedObject("silo1");
		silo1.setMany2one(berlin);

		assertTrue(silo1.getMany2one() == berlin);
		assertTrue(berlin.getOne2many().contains(silo1));

		TestObject new_york = new TestObject("New York");
		silo1.setMany2one(new_york);
		assertTrue(silo1.getMany2one() == new_york);
		assertTrue(new_york.getOne2many().contains(silo1));
		assertTrue(!berlin.getOne2many().contains(silo1));

		silo1.setMany2one(null);
		assertTrue(silo1.getMany2one() == null);
		assertTrue(!new_york.getOne2many().contains(silo1));
	}

	public void testMany2One() throws PersistenceException {
		TestObject berlin = new TestObject("Berlin");
		TestObject freiburg = new TestObject("Freiburg");
		RelatedObject silo1 = new RelatedObject("silo1");
		RelatedObject silo2 = new RelatedObject("silo2");
		berlin.addOne2many(silo1);
		berlin.addOne2many(silo2);

		assertTrue(silo1.getMany2one() == berlin);
		assertTrue(silo2.getMany2one() == berlin);
		assertTrue(berlin.getOne2many().contains(silo1));
		assertTrue(berlin.getOne2many().contains(silo2));

		getPersistenceManager().commit();
		berlin.removeOne2many(silo1);
		assertNotSame("is marked changed",  ModificationType.CHANGED, berlin.getModificationType());
		assertEquals("is not marked changed",  ModificationType.CHANGED, silo1.getModificationType());
		assertTrue(silo1.getMany2one() == null);
		assertTrue(!berlin.getOne2many().contains(silo1));
		assertTrue(((PersistentSet)berlin.getOne2many()).isDirty());

		getPersistenceManager().commit();

		assertFalse(((PersistentSet)berlin.getOne2many()).isDirty());
		berlin.addOne2many(silo1);
		assertNotSame("is marked changed",  ModificationType.CHANGED, berlin.getModificationType());
		assertEquals("is not marked changed",  ModificationType.CHANGED, silo1.getModificationType());
		assertTrue(silo1.getMany2one() == berlin);
		assertTrue(berlin.getOne2many().contains(silo1));

		getPersistenceManager().commit();

		freiburg.addOne2many(silo1);
		assertTrue(silo1.getMany2one() == freiburg);
		assertTrue(freiburg.getOne2many().contains(silo1));
		assertFalse(berlin.getOne2many().contains(silo1));

		getPersistenceManager().commit();
	}

	public void testMany2Many() throws PersistenceException {
		TestObject silo1 = new TestObject("silo1");
		TestObject silo2 = new TestObject("silo2");
		RelatedObject silogruppe1 = new RelatedObject("silogruppe1");
		RelatedObject silogruppe2 = new RelatedObject("silogruppe2");

		silogruppe1.addMany2many(silo1);
		assertTrue(silo1.getMany2many().contains(silogruppe1));
		assertTrue(silogruppe1.getMany2many().contains(silo1));

		getPersistenceManager().commit();

		silogruppe1.addMany2many(silo2);
//		assertFalse(silo2.isMarkedChanged());
//		assertTrue(silogruppe1.isMarkedChanged());

		assertTrue(silo2.getMany2many().contains(silogruppe1));
		assertTrue(silogruppe1.getMany2many().contains(silo2));

		getPersistenceManager().commit();

		silogruppe2.addMany2many(silo1);
		assertTrue(silo1.getMany2many().contains(silogruppe2));
		assertTrue(silogruppe2.getMany2many().contains(silo1));

		getPersistenceManager().commit();

		silogruppe1.removeMany2many(silo1);
		assertTrue(!silo1.getMany2many().contains(silogruppe1));
		assertTrue(!silogruppe1.getMany2many().contains(silo1));

		getPersistenceManager().commit();

		silo2.removeMany2many(silogruppe2);
		assertTrue(!silo2.getMany2many().contains(silogruppe2));
		assertTrue(!silogruppe2.getMany2many().contains(silo2));

		getPersistenceManager().commit();

	}

	/**
	 * This test is focused on the "worst case". Two pairs objects are related bidirectional. Now we change the relationship
	 * "over cross". What shall happen is the new relationship is maintained. the old partners are "alone".
	 * 
	 * @throws PersistenceException
	 */
	public void testOne2One() throws PersistenceException {

		TestObject peter = new TestObject("Peter");
		TestObject paul = new TestObject("Paul");
		RelatedObject mary = new RelatedObject("Mary");
		RelatedObject jane = new RelatedObject("Jane");

		assertEquals("is not marked new",  ModificationType.NEW, peter.getModificationType());
		assertEquals("is not marked new",  ModificationType.NEW, paul.getModificationType());
		assertEquals("is not marked new",  ModificationType.NEW, mary.getModificationType());
		assertEquals("is not marked new",  ModificationType.NEW, jane.getModificationType());

		getPersistenceManager().commit();

		peter.setOne2one(mary);
		jane.setOne2one(paul);

		assertTrue(peter.getOne2one() == mary);
		assertTrue(mary.getOne2one() == peter);

		assertTrue(paul.getOne2one() == jane);
		assertTrue(jane.getOne2one() == paul);

		getPersistenceManager().commit();

		assertNotSame("is marked changed",  ModificationType.CHANGED, peter.getModificationType());
		assertNotSame("is marked changed",  ModificationType.CHANGED, paul.getModificationType());
		assertNotSame("is marked changed",  ModificationType.CHANGED, mary.getModificationType());
		assertNotSame("is marked changed",  ModificationType.CHANGED, jane.getModificationType());

		paul.setOne2one(mary);

		assertTrue(paul.getOne2one() == mary);
		assertTrue(mary.getOne2one() == paul);

		assertEquals("is not marked changed",  ModificationType.CHANGED, paul.getModificationType());
		// assertTrue(mary.isMarkedChanged());

		// assertTrue(jane.isMarkedChanged());
		assertTrue(jane.getOne2one() == null);

		assertEquals("is not marked changed",  ModificationType.CHANGED, peter.getModificationType());
		assertTrue(peter.getOne2one() == null);

		getPersistenceManager().commit();

		mary.setOne2one(null);
		// assertTrue(mary.isMarkedChanged());
		assertEquals("is not marked changed",  ModificationType.CHANGED, paul.getModificationType());
		assertTrue(paul.getOne2one() == null);
		assertTrue(mary.getOne2one() == null);
	}

	public void testOne2ManyWithProxy() throws Exception {
		TestObject berlin = new TestObject("Berlin");
		TestObject new_york = new TestObject("New York");

		RelatedObject silo1 = new RelatedObject("silo1");
		silo1.setMany2one(berlin);

		assertTrue(silo1.getMany2one() == berlin);
		assertTrue(berlin.getOne2many().contains(silo1));
		assertFalse(new_york.getOne2many().contains(silo1));

		try {
			getPersistenceManager().commit();
		}
		catch (PersistenceException e) {
			throw e;
		}
		getPersistenceUtility().close();

		HibernateUtility hu = (HibernateUtility)getPersistenceUtility();
		assertFalse(hu.getSession().contains(silo1));

//		TestObject b = (TestObject)hu.getSession().load(TestObject.class, berlin.getId());
		RelatedObject silo = (RelatedObject)hu.getSession().load(RelatedObject.class, silo1.getId());
		// hu.attach(silo);

		berlin = silo.getMany2one();

		hu.attach(new_york);

		new_york.addOne2many(silo);

		assertTrue(new_york.getOne2many().contains(silo));
		assertFalse(berlin.getOne2many().contains(silo));
	}
	/**
	 * Relation management is set to manual -> no changes in the related object
	 * @throws PersistenceException
	 */
	public void testManualRelation() throws PersistenceException {
		getPersistenceManager().setManualRelationManagement();
		TestObject poOne = new TestObject("parent");
		RelatedObject poMany = new RelatedObject("child");
		poMany.setMany2one(poOne);
		
		assertSame("Child object should have reference to parent object", poOne, poMany.getMany2one());
		assertFalse("Parent object should not have reference to child object", poOne.getOne2many().contains(poMany));
	}

}
