package org.webguitoolkit.persistence.test.jpa;

import org.webguitoolkit.persistence.test.testobjects.TestObject;

public class SimpleModelTest extends AbstractJPAModelTest<TestObject> {

	@Override
	protected TestObject create() throws Exception {
		TestObject to = new TestObject("initText");
		to.setActive(true);
		return to;
	}

	@Override
	protected void compare(TestObject source, TestObject loaded) throws Exception {
		assertEquals("text", source.getText(), loaded.getText() );
		assertEquals("active", source.getActive(), loaded.getActive() );
		
	}

	@Override
	protected void modify(TestObject object) throws Exception {
		object.setText("modifiedText");
		object.setActive(false);
	}

	@Override
	protected Class<TestObject> getObjectClass() throws Exception {
		return TestObject.class;
	}
}
