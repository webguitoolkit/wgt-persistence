package org.webguitoolkit.persistence.test.model;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.test.BaseHibernateTest;
import org.webguitoolkit.persistence.test.testobjects.RelatedObject;
import org.webguitoolkit.persistence.test.testobjects.TestObject;


public abstract class TestCaseForPFW extends BaseHibernateTest{

	public static final String LOG4J_PFW_XML_FILE = "/log4j-pfw.xml";

	/**
	 * 
	 */
	public TestCaseForPFW() {
		super();
	}

	/**
	 * @param name
	 */
	public TestCaseForPFW(String name) {
		super(name);
	}

	@Override
	@SuppressWarnings( value="unchecked" )
	protected Class<IPersistable>[] getAnnotatedClasses() {
		return new Class[] { TestObject.class, RelatedObject.class };
	}

	/**
	 * @see org.webguitoolkit.persistence.test.BaseHibernateTest#getLog4jXmlFilename()
	 */
	@Override
	protected String getLog4jXmlFilename() {
		return LOG4J_PFW_XML_FILE;
	}
}
