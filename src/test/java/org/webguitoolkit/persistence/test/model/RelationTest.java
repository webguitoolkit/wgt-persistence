package org.webguitoolkit.persistence.test.model;

import org.webguitoolkit.persistence.test.testobjects.RelatedObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject;

public class RelationTest extends TestCaseForPFW {

	public void test1FromMany() throws Exception{
		TestObject child = new TestObject("child");
		TestObject parent = new TestObject("parent");
			
		parent.addMyMessage( child );
		
		assertEquals( "child not updated", parent, child.getParentMessage() );
		
		parent.removeMyMessage( child );
		
		assertNull( "child not null", child.getParentMessage() );
	}

	public void test2FromOne() throws Exception{
		TestObject child = new TestObject("child");		
		TestObject parent = new TestObject("parent");
			
		child.setParentMessage( parent );

		assertTrue( "parent not updated", parent.getMyMessages().contains(child) );

		child.setParentMessage( null );

		assertFalse( "parent not updated", parent.getMyMessages().contains(child) );

	}
	
	public void test3Relation() throws Exception{
		TestObject rel1 = new TestObject("rel1");
				
		RelatedObject rel2 = new RelatedObject("rel2");
		
		rel1.addMany2many( rel2 );
		
		assertTrue( "parent not updated", rel2.getMany2many().contains(rel1) );

		rel1.removeMany2many( rel2 );

		assertFalse( "parent not updated", rel2.getMany2many().contains(rel1) );
	}

}
