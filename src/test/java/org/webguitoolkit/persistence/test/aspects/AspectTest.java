package org.webguitoolkit.persistence.test.aspects;

import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


/**
 * Testing the aspects related to the persistable classes
 * 
 * @author peter
 * 
 */
public class AspectTest extends TestCaseForPFW {

	/**
	 * SImple test whether the object is marked new after creation and not new after committing.
	 * 
	 * @throws PersistenceException
	 */
	public void testCreation() throws PersistenceException {
		TestObject silo = new TestObject("Silo1");
		assertEquals("must be new",  ModificationType.NEW, silo.getModificationType());
		getPersistenceManager().commit();
		assertNotSame("must be not new", ModificationType.NEW, silo.getModificationType());
	}

	/**
	 * Test if markedChanged() is called when a attribute is changed and that the modificationList is created.
	 * 
	 * @throws PersistenceException
	 */
	public void testModification() throws PersistenceException {
		TestObject silo = new TestObject("Silo1");
		assertEquals("must be new", ModificationType.NEW, silo.getModificationType());
		getPersistenceManager().commit();
		assertNotSame("must be not new", ModificationType.NEW, silo.getModificationType());
//		assertTrue("must have no change list", silo.listModifications().size() == 0);
		silo.setText("new name");
		assertEquals("must be changed", ModificationType.CHANGED, silo.getModificationType());
//		assertTrue("must have change list", silo.listModifications().size() == 1);
//		String log = silo.listModifications().get(0);
//		System.out.println("Log = " + log);
//		assertTrue("change item must have the 'name'", log.contains("name"));
		getPersistenceManager().commit("peter");
	}

	public void testSessionAttachment() {
		// TODO-PZ testSessionAttachment
	}
	
	public void testNoMarkedChangedOnTransientFields() throws PersistenceException {
		TestObject silo = new TestObject("Silo1");
		getPersistenceManager().commit();
		assertNotSame("must be not new", ModificationType.NEW, silo.getModificationType());
		assertNotSame("must be not marked-changed", ModificationType.CHANGED, silo.getModificationType());
		silo.setTransientValue("changed value");
		assertNotSame("must be not marked-changed", ModificationType.CHANGED, silo.getModificationType());
	}
}
