/**
 * 
 */
package org.webguitoolkit.persistence.test.testobjects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
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
@Table(name="po_pair2")
@DiscriminatorColumn(name="class_id", discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorOptions(force=true)
@DiscriminatorValue(value="1012")
public class PersistentObjectPair2 extends Persistable {

	@OneToOne(mappedBy="requiredPartner2")
	private PersistentObjectPair1 requiredPartner1;
	
	@OneToOne
	@JoinColumn(name="optional_partner3_oid", nullable=true)
	private PersistentObjectPair3 optionalPartner3;
	
	@Column(nullable=false)
	private String message;
	
	/**
	 * Default constructor required by Hibernate.
	 * JavaAssist (used By Hibernate Envers) requires the constructor to be at least protected
	 */
	@SuppressWarnings("unused")
	protected PersistentObjectPair2() {
	}
	
	public PersistentObjectPair2(String newMessage, PersistentObjectPair1 newRequiredPartner1) {
		setMessage(newMessage);
		setRequiredPartner1(newRequiredPartner1);
	}

	/**
	 * @param newRequiredPartner the requiredPartner to set
	 */
	@RelationManagement(thisSide="requiredPartner1", relationSide="requiredPartner2")
	public void setRequiredPartner1(PersistentObjectPair1 newRequiredPartner) {
		requiredPartner1 = newRequiredPartner;
	}

	/**
	 * @return the requiredPartner
	 */
	public PersistentObjectPair1 getRequiredPartner1() {
		return requiredPartner1;
	}

	/**
	 * @param newOptionalPartner3 the optionalPartner to set
	 */
	@RelationManagement(thisSide="optionalPartner3", relationSide="optionalPartner2")
	public void setOptionalPartner3(PersistentObjectPair3 newOptionalPartner3) {
		optionalPartner3 = newOptionalPartner3;
	}

	/**
	 * @return the optionalPartner
	 */
	public PersistentObjectPair3 getOptionalPartner3() {
		return optionalPartner3;
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
}
