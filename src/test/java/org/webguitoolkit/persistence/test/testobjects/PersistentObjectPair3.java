/**
 * 
 */
package org.webguitoolkit.persistence.test.testobjects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.envers.Audited;
import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.model.RelationManagement;


/**
 * @author i102389
 *
 */
@Entity
@Audited
@Table(name="po_pair3")
@DiscriminatorColumn(name="class_id", discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorOptions(force=true)
@DiscriminatorValue(value="1013")
public class PersistentObjectPair3 extends Persistable {
	
	@OneToOne(mappedBy="optionalPartner3")
	private PersistentObjectPair1 optionalPartner1;
	
	@OneToOne(mappedBy="optionalPartner3")
	private PersistentObjectPair2 optionalPartner2;

	@Column(nullable=false)
	private String message;
	
	/**
	 * Default constructor required by Hibernate.
	 * JavaAssist (used By Hibernate Envers) requires the constructor to be at least protected
	 */
	@SuppressWarnings("unused")
	protected PersistentObjectPair3() {
	}
	
	public PersistentObjectPair3(String newMessage) {
		setMessage(newMessage);
	}

	/**
	 * @param newMessage the message to set
	 */
	public void setMessage(String newMessage) {
		message = newMessage;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param newOptionalPartner1 the optionalPartner1 to set
	 */
	@RelationManagement(thisSide="optionalPartner1", relationSide="optionalPartner3")
	public void setOptionalPartner1(PersistentObjectPair1 newOptionalPartner1) {
		optionalPartner1 = newOptionalPartner1;
	}

	/**
	 * @return the optionalPartner1
	 */
	public PersistentObjectPair1 getOptionalPartner1() {
		return optionalPartner1;
	}

	/**
	 * @param newOptionalPartner2 the optionalPartner2 to set
	 */
	@RelationManagement(thisSide="optionalPartner2", relationSide="optionalPartner3")
	public void setOptionalPartner2(PersistentObjectPair2 newOptionalPartner2) {
		optionalPartner2 = newOptionalPartner2;
	}

	/**
	 * @return the optionalPartner2
	 */
	public PersistentObjectPair2 getOptionalPartner2() {
		return optionalPartner2;
	}
}
