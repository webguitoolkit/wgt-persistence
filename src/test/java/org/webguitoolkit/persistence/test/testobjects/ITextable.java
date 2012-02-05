package org.webguitoolkit.persistence.test.testobjects;

import org.webguitoolkit.persistence.model.IPersistable;

public interface ITextable extends IPersistable{

	public String getText();

	public void setText(String text);

}