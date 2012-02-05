package org.webguitoolkit.persistence.test.query;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


public class UncommittedChangeQueryTest extends TestCaseForPFW {
	public void testUncommitedQuery() throws Exception {
		HibernateUtility hu = (HibernateUtility) getPersistenceManager().getPersistenceUtility();
		TestObject s1 = new TestObject("Silo1");
		s1.setActive(false);
		TestObject s2 = new TestObject("Silo2");
		s2.setActive(true);
		List<TestObject> result = hu.getSession().createCriteria(TestObject.class).list();
		assertTrue(result == null || result.isEmpty());
		hu.beginTransaction();
		hu.save(s1);
		hu.save(s2);
		hu.commitTransaction();
		
		result = hu.getSession().createCriteria(TestObject.class).add(Restrictions.eq("active", true)).list();
		assertTrue(result != null && result.size() == 1);
		assertTrue(result.get(0).getText().equals("Silo2"));

		s1.setActive(true);
		s2.setActive(false);
		
		result = hu.getSession().createCriteria(TestObject.class).add(Restrictions.eq("active", true)).list();
		assertTrue(result != null && result.size() == 1);
		assertFalse(result.get(0).getText().equals("Silo1"));
		
		hu.beginTransaction();
		hu.update(s1);
		hu.update(s2);
		
		result = hu.getSession().createCriteria(TestObject.class).add(Restrictions.eq("active", true)).list();
		assertTrue(result != null && result.size() == 1);
		assertTrue(result.get(0).getText().equals("Silo1"));
	}
}
