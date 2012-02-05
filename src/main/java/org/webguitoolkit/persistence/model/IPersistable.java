/*
 * (c) 2005, Endress+Hauser InfoServe
 */
package org.webguitoolkit.persistence.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Definition of persistence capable objects handled by the Persistence
 * framework. Mandatory implementation for all objects which are indented to be
 * made persistent.
 * <p>
 * Provides some basic state management (new, changed, deleted)
 * <p>
 * Implements equals() and hashcode()
 * <p>
 * Holds the object id as <code>long</code>
 * <p>
 * Provides creation date, version number, modification date and modifier as
 * standard attributes.
 * 
 * @author peter
 * 
 * Changes : 24-03-2009 added getLogInfo() method
 */
public interface IPersistable {
	
	public enum ModificationType {
		NONE, NEW, CHANGED, DELETED
	}

	
	/**
	 * The object id is not intended to be used in any application context but to
	 * identify objects in the datastore and to determine object identity in the
	 * equals() method.
	 * 
	 * @return the objects ID
	 */
	long getId();

	/**
	 * Mark the object as deleted if isDeleteable() returns true.
	 */
	void markDeleted();

	/**
	 * This method returns the natural key of this object. This method has to be
	 * implemented by subclasses
	 * 
	 * @return the key as Object
	 */
	Object getKey();

	/**
	 * This method has to be overwritten by subclasses to indicate whether its
	 * state allows deletion. This default implemetation returns always true.
	 * 
	 * @return true
	 */
	boolean isDeleteable();

	/**
	 * @return a string containing the class name (without package) and the result
	 *         of getKey(). E.g. "Tank { Tank1 }"
	 */
	String toString();

	/**
	 * @return the objects key hashcode calculated from the getKey() result.
	 * @see java.lang.Object#hashCode()
	 */
	int hashCode();

	/**
	 * This equals method uses the getKey() results to compare the two objects.
	 * Checks if the objects are identical. Checks if the classes are the same.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	boolean equals(Object other);



	/**
	 * @return the generated object UID (not the database id for this object)
	 */
	long getObjectUId();
	
	/**
	 * @param modificationType the modificationType to set
	 */
	void markModified(ModificationType modificationType);

	/**
	 * @return the modificationState
	 */
	ModificationType getModificationType();
	
	
	public abstract String getCreatedBy();

	public abstract Timestamp getCreatedAt();

	public abstract String getModifiedBy();

	public abstract Timestamp getModifiedAt();

}