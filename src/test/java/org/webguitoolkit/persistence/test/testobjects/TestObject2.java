package org.webguitoolkit.persistence.test.testobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.webguitoolkit.persistence.Persistable;


@Entity
@Table(name = "TEST_PO2")
/**
 * A second PersistentObject which is very similar to the other PersistentObject. It is used
 * to store it in a different database for testing parallel database connections.
 * 
 * @author Wolfram Kaiser
 */
public class TestObject2 extends Persistable implements ITextable {

	@Column(name="MESSAGE_TEXT", length=60 )
	private String text;

	/**
	 * Default constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	private TestObject2() {
	}
	
	public TestObject2(String text) {
		this.text = text; 
	}

	/**
	 * @see com.endress.infoserve.persistence.test.testobjects.ITextable#getText()
	 */
	public String getText() {
		return text;
	}

	/**
	 * @see com.endress.infoserve.persistence.test.testobjects.ITextable#setText(java.lang.String)
	 */
	public void setText(String text) {
		this.text = text; 
	}
}
