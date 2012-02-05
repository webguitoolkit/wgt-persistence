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
@Table(name="po_pair1")
@DiscriminatorColumn(name="class_id", discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorOptions(force=true)
@DiscriminatorValue(value="1011")
public class PersistentObjectPair1 extends Persistable {

	@OneToOne
	@JoinColumn(name="required_partner2_oid", nullable=false)
	private PersistentObjectPair2 requiredPartner2;
	
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
	protected PersistentObjectPair1() {
	}

	public PersistentObjectPair1(String newMessage, PersistentObjectPair2 newRequiredPartner2) {
		setMessage(newMessage);
		setRequiredPartner2(newRequiredPartner2);
	}
	
	/**
	 * @param newOptionalPartner3 the optionalPartner to set
	 */
	@RelationManagement(thisSide="optionalPartner3", relationSide="optionalPartner1")
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
	 * @param newRequiredPartner2 the requiredPartner to set
	 */
	@RelationManagement(thisSide="requiredPartner2", relationSide="requiredPartner1")
	public void setRequiredPartner2(PersistentObjectPair2 newRequiredPartner2) {
		requiredPartner2 = newRequiredPartner2;
	}

	/**
	 * @return the requiredPartner
	 */
	public PersistentObjectPair2 getRequiredPartner2() {
		return requiredPartner2;
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
