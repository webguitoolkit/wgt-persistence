package org.webguitoolkit.persistence.test.model.util;

import java.util.Set;

import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.RelatedObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject;

public class RelationManagementTest extends TestCaseForPFW {
	
	public void testPersistentSet() throws Exception{
		TestObject berlin = new TestObject("Berlin");
		RelatedObject silo1 = new RelatedObject("silo1");
		
		Set<RelatedObject> one2manyWrapped = berlin.getOne2manyWrapped();
		one2manyWrapped.add(silo1);

		assertTrue(berlin.getOne2manyWrapped().contains(silo1));
		assertTrue(silo1.getMany2oneWrapped() == berlin);
		
		getPersistenceManager().commit();
		
		RelatedObject silo2 = new RelatedObject("silo2");
		berlin.getOne2manyWrapped().add(silo2);
		
		assertTrue(berlin.getModificationType() == ModificationType.CHANGED );
	}
}
