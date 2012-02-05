package org.webguitoolkit.persistence.test.query;

import org.hibernate.Criteria;
import org.webguitoolkit.persistence.query.hibernate.AbstractPagedQuery;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


public class PagedQueryTest extends TestCaseForPFW {

	public void testPageQuery() throws Exception{
		for( int i = 1; i<100; i++ ){
			new TestObject( "Message"+i );
		
		}
		getPersistenceManager().commit();
		
		AbstractPagedQuery<TestObject> query = new AbstractPagedQuery<TestObject>( getPersistenceManager(), 10 ) {
			@Override
			protected Criteria getSearchCriteria() {
				return getDBSession().createCriteria( getSearchClass() );
			}
			@Override
			protected Class<TestObject> getSearchClass() { return TestObject.class; }
		};

		int i=0;
		while( query.hasNext() ){
			query.next();
			i++;
		}
		assertEquals("Not the right number of pages", 10, i );
	}
}
