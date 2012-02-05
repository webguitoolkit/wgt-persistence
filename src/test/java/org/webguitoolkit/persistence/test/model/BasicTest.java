package org.webguitoolkit.persistence.test.model;

import java.sql.Time;
import java.util.Collection;

import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.query.ICollectionQuery;
import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.testobjects.ITextable;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


public class BasicTest extends TestCaseForPFW {

	/**
	 * Test the creation of new objects, commit and close
	 * 
	 * @throws Exception
	 */
	public void testInsert() throws Exception {
		String messageText = "JPA Message at " + new Time(System.currentTimeMillis());
		ITextable message = new TestObject(messageText);
		assertEquals("Message text should be the same", messageText, message.getText());
		
		getPersistenceManager().commit();
		getPersistenceManager().close();
	}

	/**
	 * Test if new objects are marked as new (creation aspect) , that they are
	 * registerd. Test if a query re-registers the object (registration aspect).
	 * 
	 * @throws Exception
	 */
	public void testRegistration() throws Exception {
		ITextable message = new TestObject(createMessageTextWithTimestamp());
		assertEquals("must be new",  ModificationType.NEW, message.getModificationType());
		getPersistenceManager().commit();
		assertNotSame("is new after commit",  ModificationType.NEW, message.getModificationType());

		getPersistenceManager().close();

//		QueryFactory queryFactory = new QueryFactory();
		ICollectionQuery<TestObject> allMsgQuery = new AllOfAKindQuery<TestObject>( getPersistenceManager(), TestObject.class );
		Collection<TestObject> result = allMsgQuery.execute();
		
//		Collection<IPersistable> result = queryFactory.newHQLQuery("from Message").execute();
		assertEquals("result size ", 1, result.size());
	}

	/**
	 * Test detach and attach
	 * 
	 * @throws Exception
	 */
	public void testAttach() throws Exception {
		ITextable message = new TestObject(createMessageTextWithTimestamp());
		assertEquals("must be new",  ModificationType.NEW, message.getModificationType());
		getPersistenceManager().commit();
		assertNotSame("is new after commit",  ModificationType.NEW, message.getModificationType());
		getPersistenceManager().detach();
		assertFalse(isAttached( message ));

		getPersistenceManager().close();
	}

	public void testUpdate() throws Exception {
		ITextable message = new TestObject(createMessageTextWithTimestamp());
		getPersistenceManager().commit();
		getPersistenceManager().close();

		QueryFactory queryFactory = new QueryFactory();
		Collection<IPersistable> result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
		TestObject newMessage = (TestObject) result.iterator().next();
		String newtext = "Changed: " + message.getText();
		newMessage.setText(newtext);
		assertEquals("must be changed",  ModificationType.CHANGED, newMessage.getModificationType());
		getPersistenceManager().commit();
		getPersistenceManager().close();
		result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
		newMessage = (TestObject) result.iterator().next();
		assertEquals("text new? ", newtext, newMessage.getText());
	}

	public void testDelete() throws Exception {
		String messageText = createMessageTextWithTimestamp();
		ITextable message = new TestObject(messageText);
		assertEquals("Message text should be the same", messageText, message.getText());
		
		getPersistenceManager().commit();
		getPersistenceManager().close();

		QueryFactory queryFactory = new QueryFactory();
		Collection<IPersistable> result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
		TestObject removeMessage = (TestObject) result.iterator().next();
		removeMessage.markDeleted();
		assertEquals("Object should be marked as deleted",  ModificationType.DELETED, removeMessage.getModificationType());
		
		getPersistenceManager().commit();
		result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 0, result.size());
		assertNotSame("Object should be marked as deleted",  ModificationType.DELETED, message.getModificationType());
	}

	public void testQuery() throws Exception {
		String messageText = createMessageTextWithTimestamp();
		ITextable message = new TestObject(messageText);
		assertEquals("Message text should be the same", messageText, message.getText());
		
		getPersistenceManager().commit();

		QueryFactory queryFactory = new QueryFactory();
		Collection<IPersistable> result = queryFactory.newHQLQuery("from TestObject").execute();
		assertEquals("result size ", 1, result.size());
	}

	public void testRollback() throws Exception {
		ITextable message1 = new TestObject(createMessageTextWithTimestamp());
		ITextable message2 = new TestObject(createMessageTextWithTimestamp());
		
		assertEquals("Wrong number of modified objects in transaction", 2, getPersistenceManager().getDirtyObjects().size());
		assertEquals("Object should not have OID", 0, message1.getId());
		assertEquals("Object should not have OID", 0, message2.getId());
		assertEquals("Before rollback/commit the object should be marked as new",  ModificationType.NEW, message1.getModificationType());
		assertEquals("Before rollback/commit the object should be marked as new",  ModificationType.NEW, message2.getModificationType());
		
		getPersistenceManager().rollback();
		
		assertEquals("Wrong number of modified objects in transaction", 0, getPersistenceManager().getDirtyObjects().size());
		assertEquals("Object should not have OID", 0, message1.getId());
		assertEquals("Object should not have OID", 0, message2.getId());
		assertNotSame("After rollback object should not be marked as new",  ModificationType.NEW, message1.getModificationType());
		assertNotSame("After rollback object should not be marked as new",  ModificationType.NEW, message2.getModificationType());
	}
	
	/**
	 * @return
	 */
	protected String createMessageTextWithTimestamp() {
		return "JPA TestObject at " + new Time(System.currentTimeMillis());
	}
}

class QueryFactory {
	public HQLQuery newHQLQuery(String hql) {
		return new HQLQuery(hql);
	}
}

class HQLQuery {

	private String hql;

	public HQLQuery(String hql) {
		this.hql = hql;
	}

	@SuppressWarnings("unchecked")
	public Collection<IPersistable> execute() {
		HibernateUtility hu = (HibernateUtility) PersistenceContext.getPersistenceManager().getPersistenceUtility();
		return hu.getSession().createQuery(hql).list();
	}
}
