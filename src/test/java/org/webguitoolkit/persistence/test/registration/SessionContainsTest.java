package org.webguitoolkit.persistence.test.registration;

import org.hibernate.Session;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


/**
 * Tests around session registration of Persistable.
 * 
 * @author peter
 * 
 */
public class SessionContainsTest extends TestCaseForPFW {
	/**
	 * Check if Session.contains uses equals() or == identity. None of these is used but the object id.
	 * 
	 * @throws Exception
	 */
	public void testRegistration() throws Exception {
		TestObject haus1 = new TestObject("Haus");
		getPersistenceManager().commit();
		getPersistenceManager().close();
		HibernateUtility hu = (HibernateUtility) getPersistenceManager().getPersistenceUtility();
		Session session1 = hu.getSession();
		assertFalse("session should not contain haus1", session1.contains(haus1));
		TestObject haus2 = new TestObject("Haus");
		getPersistenceManager().commit();
		assertTrue("session should contain haus2", session1.contains(haus2));
		assertFalse("session should not contain haus1", session1.contains(haus1));
		getPersistenceManager().close();
		Session session2 = hu.getSession();
		haus1.setText("Haus1");
		assertTrue(session1 != session2);
		assertFalse(haus1.equals(haus2));
		assertTrue("session should contain haus1", session2.contains(haus1));
	}

}
