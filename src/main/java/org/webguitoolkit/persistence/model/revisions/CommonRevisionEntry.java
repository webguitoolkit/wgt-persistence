/**
 * 
 */
package org.webguitoolkit.persistence.model.revisions;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 * A revision defines a set of changes within the database (during one transaction).
 * For each transaction with a revision-aware model class a new revision entry is created.
 * The RevisionEntry is a persistent class as well and can be queried with Hibernate helper classes.
 *  
 * A problem might be the table name and schema name as these settings are specific for each
 * project and cannot be defined in the super class. Thus, it might be necessary to make this
 * class abstract in order to require its subclassing.
 * 
 * @author i102389
 *
 */
@Entity
@Table(name="revisions")
@RevisionEntity(CommonRevisionListener.class)
public class CommonRevisionEntry extends DefaultRevisionEntity {

	private String username;
	private String programName;
	private String clientIP;
	private int criticality;
	
	/**
	 * 
	 */
	public CommonRevisionEntry() {
	}

	public CommonRevisionEntry(String newUsername) {
		setUsername(newUsername);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the programName
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * @return the clientIP
	 */
	public String getClientIP() {
		return clientIP;
	}

	/**
	 * @return the security
	 */
	public int getCriticality() {
		return criticality;
	}

	/**
	 * @param newUsername the username to set
	 */
	public void setUsername(String newUsername) {
		username = newUsername;
	}

	/**
	 * @param newProgramName the programName to set
	 */
	public void setProgramName(String newProgramName) {
		programName = newProgramName;
	}

	/**
	 * @param newClientIP the clientIP to set
	 */
	public void setClientIP(String newClientIP) {
		clientIP = newClientIP;
	}

	/**
	 * @param newCriticality the security to set
	 */
	public void setCriticality(int newCriticality) {
		criticality = newCriticality;
	}

}
