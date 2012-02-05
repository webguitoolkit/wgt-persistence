package org.webguitoolkit.persistence.test.util;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.webguitoolkit.persistence.util.UIDGenerator;

public class UIDGeneratorTest extends TestCase{

	public void testUID() throws Exception{
		Set<Long> ids = new HashSet<Long>();
		for( int i = 0; i < 10000; i++ ){
			ids.add( UIDGenerator.getInstance().getUID() );
			assertEquals("Double Id", i+1, ids.size() );
		}
	}
}
