/**
 * 
 */
package org.webguitoolkit.persistence.test.util.dto;

import org.webguitoolkit.persistence.test.model.TestCaseForPFW;
import org.webguitoolkit.persistence.test.testobjects.ITextable;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


/**
 * @author Wolfram Kaiser
 */
public class DTOModelFactoryTest extends TestCaseForPFW {

	private TestDTOModelFactory testFactory;

	/**
	 * @param name
	 */
	public DTOModelFactoryTest(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setTestFactory(new TestDTOModelFactory());
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @return the testFactory
	 */
	public TestDTOModelFactory getTestFactory() {
		return testFactory;
	}

	/**
	 * @param newTestFactory the testFactory to set
	 */
	public void setTestFactory(TestDTOModelFactory newTestFactory) {
		testFactory = newTestFactory;
	}

	public void test01createTextable() {
		ITextable transientTextable = getTestFactory().newTextable("MyTestText-Transient");
		assertEquals("data not set", "MyTestText-Transient", transientTextable.getText());

		ITextable persistentTextable = new TestObject("MyTestText-Persistent");

		getTestFactory().copyValues(persistentTextable, transientTextable);
		assertEquals("wrong data", "MyTestText-Persistent", transientTextable.getText());

		// reset object
		transientTextable = getTestFactory().newTextable("MyTestText-Transient");
		getTestFactory().copyValues(transientTextable, persistentTextable);
		assertEquals("wrong data", "MyTestText-Transient", persistentTextable.getText());
	}

	public void test02hashCode() {
		ITextable transientTextable = getTestFactory().newTextable("MyTestText-Transient");
		int hashCode = transientTextable.hashCode();
		assertTrue("hashCode should not be 0", hashCode != 0);
	}
}
