package org.webguitoolkit.persistence.test.model;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.webguitoolkit.persistence.ConcurrentChangeException;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.testobjects.TestObject;
import org.webguitoolkit.persistence.util.HibernateUtility;


public class ConcurrentTest extends TestCaseForPFW {

	public void test01Concurrent() throws Exception {
		TestObject message0 = new TestObject("Message 0 at " + new Time(System.currentTimeMillis()));
		TestObject message1 = new TestObject("Message 1 at " + new Time(System.currentTimeMillis()));
		TestObject message2 = new TestObject("Message 2 at " + new Time(System.currentTimeMillis()));
		TestObject message3 = new TestObject("Message 3 at " + new Time(System.currentTimeMillis()));
		message0.addMyMessage(message1);
		message0.addMyMessage(message2);
		message0.addMyMessage(message3);

		getPersistenceManager().commit();
		getPersistenceManager().close();

		assertFalse("Message 0 is still attached", isAttached(message0));
		message0.getMyMessages();
		message0.setText("some text");
		assertTrue("Message 0 is not attached", isAttached(message0));

		Collection<TestObject> messages = new AllOfAKindQuery<TestObject>(getPersistenceManager(), TestObject.class).execute();
		for (TestObject msg : messages) {
			System.out.println(msg.getText() + " " + isAttached(msg));
			msg.getMyMessages();
			msg.setText(msg.getText().substring(0, 5));
		}
		getPersistenceManager().commit();
		getPersistenceManager().close();

		new AllOfAKindQuery<TestObject>(getPersistenceManager(), TestObject.class).execute();
		for (TestObject msg : messages) {
			System.out.println(msg.getText());
		}
		message0.getText();
	}

	public void test02StaleObjectException() throws PersistenceException, HibernateException, SQLException {
		TestObject testMessage = new TestObject("Message 0 at " + new Time(System.currentTimeMillis()));
		long objectUid = testMessage.getObjectUId();
		getPersistenceManager().commit();

		// still attached to the PersistenceManager
		assertTrue("Message 0 is still attached", isAttached(testMessage));

		// now we perform a concurrent write on the same object (low level SQL)
		Statement stmt = ((HibernateUtility)getPersistenceManager().getPersistenceUtility()).getSession().connection().createStatement();
		stmt.execute("update TEST_PO set MESSAGE_TEXT = 'SQL-Updated Message 1', VERSION_NR=2 where OBJECTUID = " + objectUid);

		testMessage.setText("PFW-Updated Message 2");
		assertTrue("Message 0 is still attached", isAttached(testMessage));

		try {
			getPersistenceManager().commit();
			fail("Expected ConcurrentChangeException not thrown");
		}
		catch (ConcurrentChangeException e) {
			// expected exception
			assertEquals("Text is still changed for the object in memory", "SQL-Updated Message 1", testMessage.getText());

			getPersistenceManager().refresh(testMessage);
			assertTrue("Message 0 is still attached", isAttached(testMessage));
			// new object state from database after refresh
			assertEquals("Text has not been refreshed from DB", "SQL-Updated Message 1", testMessage.getText());
		}

		// after a refresh attempt to do the change again
		testMessage.setText("PFW-Updated Message 0 - 2nd attempt");
		getPersistenceManager().commit();
	}
}
