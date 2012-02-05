/*
 * (c) 2005, Endress+Hauser InfoServe
 */
package org.webguitoolkit.persistence;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OptimisticLock;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.util.UIDGenerator;

/**
 * Base class for the persistence framework. Classes which are indented to be made persistent should extend this class or
 * implement the IPersistable interface by themselves.
 * <p>
 * Provides some basic state management (new, changed, deleted)
 * <p>
 * Implements equals() and hashcode() based on getObjectUId()
 * <p>
 * Holds the object id as <code>long</code>
 * <p>
 * Provides creation date, version number, modification date and modifier as standard attributes.
 * 
 * @author peter
 */
@MappedSuperclass
public abstract class Persistable implements IPersistable {

	/**
	 * Can be used to create DB queries
	 */
	public static final String ATTRIBUTE_CREATED_AT = "createdAt";
	public static final String ATTRIBUTE_MODIFIED_AT = "modifiedAt";
	public static final String ATTRIBUTE_MODIFIED_BY = "modifiedBy";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_VERSION_NR = "versionNo";
	public static final String ATTRIBUTE_OJECT_UID = "objectUId";

	/*
	 * Fields which are not part of the persistent state of the object
	 */
	@Transient
	private ModificationType modificationType = ModificationType.NONE;
	@Transient
	private List<String> modifications = new ArrayList<String>(1);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "OBJ_ID")
	private long id;

	@Version
	@Column(name = "VERSION_NR", nullable = false)
	private long versionNo;

	@Column(name = "OBJECTUID")
	@Index(name = "objectuid_idx")
	private long objectUId;

	@Column(name = "CREATED_AT")
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column(name = "CREATED_BY", length = 32)
	@OptimisticLock(excluded = true)
	private String createdBy;

	@Column(name = "MODIFIED_AT")
	@OptimisticLock(excluded = true)
	private Timestamp modifiedAt;

	@Column(name = "MODIFIED_BY", length = 32)
	@OptimisticLock(excluded = true)
	private String modifiedBy;

	protected void setModificationTimestamp() {
		modifiedAt = new Timestamp(System.currentTimeMillis());
	}

	/* (non-Javadoc)
	 * @see org.webguitoolkit.persistence.AI#getCreatedAt()
	 */
	public Timestamp getCreatedAt() {
		return createdAt;
	}

	/* (non-Javadoc)
	 * @see org.webguitoolkit.persistence.AI#getModifiedBy()
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/* (non-Javadoc)
	 * @see org.webguitoolkit.persistence.AI#getModifiedAt()
	 */
	public Timestamp getModifiedAt() {
		return modifiedAt;
	}

	protected void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	protected void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Fields which are part of the persistent state of any persistent object
	 */

	/**
	 * Constructor that creates an unique ID
	 */
	protected Persistable() {
		super();
		setGeneratedObjectUId();
	}

	/*
	 * The id/oid must be defined in each persistent class because each class has currently a
	 * specific ID name in the database (e.g. user_id)
	 * 
	 * @Id
	 * @GeneratedValue(strategy = GenerationType.AUTO)
	 * @Column(name = "OBJ_ID")
	 */
	// private long id;

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

	/**
	 * Fields which are part of the persistent state of any persistent object
	 */

	protected void setGeneratedObjectUId() {
		objectUId = UIDGenerator.getInstance().getUID();
	}

	public long getVersionNo() {
		return versionNo;
	}

	public long getObjectUId() {
		return objectUId;
	}

	public void setObjectUId(long newuid) {
		this.objectUId = newuid;
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#markDeleted()
	 */
	public void markDeleted() {
		if (isDeleteable()) {
			markModified(ModificationType.DELETED);
			PersistenceContext.getPersistenceManager().markDirty(this);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#isMarkedDeleted()
	 */
	protected boolean isMarkedDeleted() {
		return modificationType == ModificationType.DELETED;
	}

	protected List<String> listModifications() {
		return modifications;
	}

	public void markChangedRelationship(IPersistable partner) {
		markModified(ModificationType.CHANGED);
		PersistenceContext.getPersistenceManager().markDirty(this);
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#reset()
	 */
	public void reset() {
		modificationType = ModificationType.NONE;
		PersistenceContext.getPersistenceManager().unmarkDirty(this);
		synchronized (this) {
			// calling modifications.clear() would attach the object to the Session again.
			modifications = new ArrayList<String>(1);
		}
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#isDeleteable()
	 */
	public boolean isDeleteable() {
		return true;
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#toString()
	 */
	public String toString() {
		String result = getClass().getName();
		result = result.substring(result.lastIndexOf('.') + 1);
		return result + "{ " + getKey() + " }";
	}

	/**
	 * This method returns the natural key of this object.
	 * 
	 * @return the key as Object
	 */
	public Object getKey() {
		return new Long(getObjectUId());
	}

	/**
	 * Default implementation that delivers getKey().toString();
	 * 
	 * @see org.webguitoolkit.persistence.model.IPersistable#getLogInfo()
	 */
	protected String getLogInfo() {
		return getKey().toString();
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#hashCode()
	 */
	public int hashCode() {
		return (int)getObjectUId();
	}

	/**
	 * @see org.webguitoolkit.persistence.model.IPersistable#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}

		Class<? extends IPersistable> thisClass = this.getClass();
		Class<? extends Object> otherClass = other.getClass();

		if (!thisClass.isAssignableFrom(otherClass) && !(otherClass.isAssignableFrom(thisClass))) {
			return false;
		}
		if (!(other instanceof IPersistable)) {
			return false;
		}
		IPersistable theOther = (IPersistable)other;
		boolean result = (getObjectUId() == theOther.getObjectUId());
		return result;
	}

	protected int getPropertyLength(String propertyName) {
		return PersistenceContext.getPersistenceManager().getPersistenceUtility().getPropertyLength(this, propertyName);
	}

	/**
	 * @param modificationType the modificationState to set
	 */
	public void markModified(ModificationType modificationType) {
		if (ModificationType.DELETED == modificationType && isDeleteable()) {
			this.modificationType = ModificationType.DELETED;
			PersistenceContext.getPersistenceManager().markDirty(this);
		}
		else if (ModificationType.CHANGED == modificationType && this.modificationType == ModificationType.NONE) {
			this.modificationType = ModificationType.CHANGED;
			PersistenceContext.getPersistenceManager().markDirty(this);
			setModificationTimestamp();
		}
		else if (ModificationType.NEW == modificationType) {
			this.modificationType = ModificationType.NEW;
			PersistenceContext.getPersistenceManager().markDirty(this);
		}
		else if (ModificationType.NONE == modificationType) {
			reset();
		}
	}

	/**
	 * @return the modificationState
	 */
	public ModificationType getModificationType() {
		return modificationType;
	}

	@PostLoad
	protected void postLoad() {
		System.out.println("post-load called!");
		// add/attach the object only if it is an IPersistable - some objects may be read from DB
		// but are not full IPersistable because the have only read-only data (e.g. views)
		PersistenceManager pm = PersistenceContext.getPersistenceManager();
		if (pm == null) {
			throw new RuntimeException("PersistenceManager must be set in context");
		}
		pm.addObjectInUse(this);
	}
}
