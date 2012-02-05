package org.webguitoolkit.persistence.test.model;

import org.webguitoolkit.persistence.test.testobjects.TestObject;

public class TextLengthTest extends TestCaseForPFW {

	public static final String TEXT10 = "0123456789";
	public static final String TEXT20 = TEXT10+TEXT10;
	public static final String TEXT100 = TEXT20+TEXT20+TEXT20+TEXT20+TEXT20;
	public static final String TEXT500 = TEXT100+TEXT100+TEXT100+TEXT100+TEXT100;
	public static final String TEXT1000 = TEXT500+TEXT500;
	
	public void testCutLongText() throws Exception {
		TestObject msg = new TestObject("text");

		assertEquals("Text 100", 100, TEXT100.length() );

		msg.setText( TEXT100 );
		
		assertEquals("Text not cut", 60, msg.getText().length() );
	}
}
