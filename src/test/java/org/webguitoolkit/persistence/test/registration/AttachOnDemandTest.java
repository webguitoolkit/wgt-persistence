package org.webguitoolkit.persistence.test.registration;

import java.util.Collection;

import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


public class AttachOnDemandTest extends TestCaseForPFW {
	public void testAttachOnDemand() throws Exception {
//		for( int i = 0; i<10000;i++){
//			new TestObject("Object "+i);
//		}
		TestObject object1 = new TestObject("Object");
		getPersistenceManager().commit();
		getPersistenceManager().close();

//		getPersistenceManager().attach();
		AllOfAKindQuery<TestObject> query = new AllOfAKindQuery<TestObject>(getPersistenceManager(), TestObject.class);
		Collection<TestObject> result = query.execute();
		TestObject object2 = result.iterator().next();
		assertTrue(object1.equals(object2));
		assertTrue(object1 == object2);
		System.out.println(result);
		getPersistenceManager().getPersistenceUtility().attach(object1);

		HibernateUtility hu = (HibernateUtility)getPersistenceUtility();
		System.out.println(hu.getSession().contains(object1));
		System.out.println(hu.getSession().contains(object2));
	}
}
