package org.webguitoolkit.persistence.test.query;

import java.util.Collection;

import org.webguitoolkit.persistence.query.hibernate.AllOfAKindQuery;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.ITextable;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


public class AllOfAKindQueryTest extends TestCaseForPFW {

	public void testPageQuery() throws Exception{
		for( int i = 0; i<100; i++ ){
			new TestObject( "Message"+i );
		}
		getPersistenceManager().commit();
	
		AllOfAKindQuery<ITextable> allOfAKindQuery = new AllOfAKindQuery<ITextable>(getPersistenceManager(), TestObject.class);

		Collection<ITextable> result = allOfAKindQuery.execute();
		
		assertEquals("Not the right number of results", 100, result.size() );
	}
}
