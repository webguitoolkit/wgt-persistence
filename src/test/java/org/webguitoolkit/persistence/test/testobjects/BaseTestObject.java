package org.webguitoolkit.persistence.test.testobjects;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.webguitoolkit.persistence.Persistable;

@MappedSuperclass
public abstract class BaseTestObject extends Persistable{
	
	private BaseTestObject(){
		super();
	}
	
	@Column(name="MESSAGE_TEXT", length=60 )
	private String text;

}
