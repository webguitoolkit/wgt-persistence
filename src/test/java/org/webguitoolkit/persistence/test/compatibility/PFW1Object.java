package org.webguitoolkit.persistence.test.compatibility;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.webguitoolkit.persistence.model.compatibility.PersistablePFW1;

@Entity
public class PFW1Object extends PersistablePFW1{
	
	@Column
	private String text;

	protected PFW1Object(){
		super();
	}
	
	public PFW1Object(String text ){
		this.text = text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

}
