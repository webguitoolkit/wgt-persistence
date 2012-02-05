package org.webguitoolkit.persistence.test.model;

import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.test.testobjects.TestObject;

public class MarkChangedTest extends TestCaseForPFW{

	public void testMarkChanged() throws Exception {
		TestObject object = new TestObject("Object");
		assertNotSame("Mark Changed is initial set",  ModificationType.CHANGED, object.getModificationType());
		assertTrue("Dirty Objects do not contain new object", getPersistenceManager().getDirtyObjects().contains( object ) );

		getPersistenceManager().clearDirtyObjects();
		object.reset();
		
		object.setTransientValue( "test" ); 
		assertNotSame("Mark Changed is set at transient value",  ModificationType.CHANGED, object.getModificationType());
		assertFalse("Dirty Objects contain transient edited object", getPersistenceManager().getDirtyObjects().contains( object ) );

		getPersistenceManager().clearDirtyObjects();
		object.reset();

		object.setActive( true );
		assertEquals("Mark Changed is not set at non transient value",  ModificationType.CHANGED, object.getModificationType());
		assertTrue("Dirty Objects do not contain non transient edited object", getPersistenceManager().getDirtyObjects().contains( object ) );
		object.reset();
	}

}
