/**
 * 
 */
package org.webguitoolkit.persistence.test.util.dto;

import org.webguitoolkit.persistence.test.testobjects.ITextable;
import org.webguitoolkit.persistence.util.dto.DTOModelFactory;


/**
 * @author Wolfram Kaiser
 */
public class TestDTOModelFactory extends DTOModelFactory {

	/**
	 * 
	 */
	public TestDTOModelFactory() {
	}

	public ITextable newTextable(String newText) {
		ITextable newObject = newInstance(ITextable.class);
		newObject.setText(newText);
		return newObject;
	}
}
