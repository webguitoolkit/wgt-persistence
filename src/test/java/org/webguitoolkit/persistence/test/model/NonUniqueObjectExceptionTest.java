package org.webguitoolkit.persistence.test.model;

import java.sql.Time;
import java.util.Collection;

import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


public class NonUniqueObjectExceptionTest extends TestCaseForPFW {
	
	public void testNonUniqueObjectException() throws Exception {
		
		TestObject o1 = new TestObject("TestObject 0 at " + new Time(System.currentTimeMillis()));
		getPersistenceManager().commit();
		Long id = o1.getId();
		assertNotNull(id);
		getPersistenceManager().detach();

		
		System.out.println("before loading");
		getPersistenceManager().attach();
		Collection<TestObject> messages = new AllOfAKindQuery<TestObject>( getPersistenceManager(), TestObject.class ).execute();
		TestObject o2 = messages.iterator().next();

//		TestObject o2 = (TestObject)((HibernateUtility)getPersistenceUtility()).getSession().load(TestObject.class, id);
		System.out.println("before get desc o1");
		o2.getActive();
		System.out.println("before set desc o1");
		o2.setActive(true);
		System.out.println("before set desc o2");
		o1.setActive(true);
		assertTrue("objects are not the same", o1 == o2 );
		
		getPersistenceManager().printStatistics( System.out );
	}
}
