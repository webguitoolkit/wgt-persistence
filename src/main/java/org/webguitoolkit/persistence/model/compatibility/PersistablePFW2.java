/*
 * (c) 2005, Endress+Hauser InfoServe
 */
package org.webguitoolkit.persistence.model.compatibility;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.model.IPersistable;


/**
 * Base class for the persistence framework. Classes which are indented to be
 * made persistent should extend this class or implement the IPersistable
 * interface by themselves.
 * <p>
 * Provides some basic state management (new, changed, deleted)
 * <p>
 * Implements equals() and hashcode() based on getObjectUId()
 * <p>
 * Holds the object id as <code>long</code>
 * <p>
 * Provides creation date, version number, modification date and modifier as
 * standard attributes.
 * 
 * @author peter
 */
@AttributeOverrides( value={
		@AttributeOverride( name="versionNo", column = @Column(name="OBJ_VERSION") ),
		@AttributeOverride( name="objectUId", column = @Column(name="OBJ_UID") )
} )
@MappedSuperclass
public abstract class PersistablePFW2 extends Persistable implements IPersistable {
	
	/**
	 * Fields which are part of the persistent state of any persistent object
	 */

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "OBJ_ID")
	private long id;

	/**
	 * Constructor that creates an unique ID
	 */
	protected PersistablePFW2() {
		super();
		setGeneratedObjectUId();
	}

	// ------------------------------------------------------------------------------
	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#setId(long)
	 */
	protected void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}

}