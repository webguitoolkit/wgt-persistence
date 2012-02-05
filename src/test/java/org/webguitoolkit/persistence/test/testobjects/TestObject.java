package org.webguitoolkit.persistence.test.testobjects;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.model.RelationManagement;
import org.webguitoolkit.persistence.model.util.WrapWithObserver;


@Entity
@Table(name = "TEST_PO")
public class TestObject extends Persistable implements ITextable {

	@Transient
	private String transientValue;
	
	@Embedded
	private EmbeddedObject embedded;

	@Column(name="MESSAGE_TEXT", length=60 )
	private String text;

	@Column
	private boolean active;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY  )
	@JoinColumn(name = "NEXT_MESSAGE_ID")
	private TestObject nextMessage;

	@ManyToOne( cascade=CascadeType.ALL, fetch=FetchType.LAZY )
	@JoinColumn(name = "PARENT_MESSAGE_ID")
	private TestObject parentMessage;

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="parentMessage" )
	private Set<TestObject> myMessages;

	// MANY 2 MANY
	@ManyToMany( cascade=CascadeType.ALL, fetch=FetchType.LAZY  )
	@JoinTable(name = "MANY2MANY", joinColumns = { @JoinColumn(name = "TESTOBJECT_ID") }, inverseJoinColumns = { @JoinColumn(name = "RELATED_ID") })
	private Set<RelatedObject> many2many;

	// ONE 2 MANY
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="many2one" )
	private Set<RelatedObject> one2many;

	// ONE 2 ONE
	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "oneToOne", nullable = true)
	private RelatedObject one2one;

    @ElementCollection
    @MapKeyColumn(nullable=false, name="attr_key")
    @Column(name="attr_value")
    @CollectionTable(joinColumns={@JoinColumn(name="object_oid")}, name="TEST_ATTRIBUTES" )
	@OrderBy("origin ASC")
	private Map<String, String> attributes = new LinkedHashMap<String, String>();

	// ONE 2 MANY WRAPPED
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="many2oneWrapped" )
	@WrapWithObserver()
	@RelationManagement(thisSide="one2manyWrapped", relationSide="many2oneWrapped")
	private Set<RelatedObject> one2manyWrapped;

	/**
	 * Default constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	protected TestObject() {
		myMessages = new HashSet<TestObject>();
		many2many = new HashSet<RelatedObject>();
		one2many = new HashSet<RelatedObject>();
		one2manyWrapped = new HashSet<RelatedObject>();
	}
	
	public TestObject(String text) {
		this();
		this.text = text; 
	}

	/**
	 * @see com.endress.infoserve.persistence.test.testobjects.ITextable#getText()
	 */
	public String getText() {
		return text;
	}

	/**
	 * @see com.endress.infoserve.persistence.test.testobjects.ITextable#setText(java.lang.String)
	 */
	public void setText(String text) {
		this.text = text; 
	}

	public TestObject getNextMessage() {
		return nextMessage;
	}

	public void setNextMessage(TestObject nextMessage) {
		this.nextMessage = nextMessage;
	}
	
	public Set<TestObject> getMyMessages() {
		return myMessages;
	}

	public void setMyMessages(Set<TestObject> myMessages) {
		this.myMessages = myMessages;
	}
	
	@RelationManagement( thisSide="myMessages", relationSide="parentMessage")
	public void addMyMessage( TestObject myMessage ) {
		myMessages.add( myMessage );
	}

	@RelationManagement( thisSide="myMessages", relationSide="parentMessage")
	public void removeMyMessage( TestObject myMessage ) {
		myMessages.remove( myMessage );
	}

	@RelationManagement( thisSide="parentMessage", relationSide="myMessages")
	public void setParentMessage(TestObject parentMessage) {
		this.parentMessage = parentMessage;
	}

	public TestObject getParentMessage() {
		return parentMessage;
	}

	public Set<RelatedObject> getMany2many() {
		return many2many;
	}

	@RelationManagement( thisSide="many2many", relationSide="many2many")
	public void addMany2many(RelatedObject newRelated) {
		this.many2many.add( newRelated );
	}
	@RelationManagement( thisSide="many2many", relationSide="many2many")
	public void removeMany2many(RelatedObject newRelated) {
		this.many2many.remove( newRelated );
	}
	
	public String getTransientValue() {
		return transientValue;
	}
	public void setTransientValue(String transientValue) {
		this.transientValue = transientValue;
	}

	/**
	 * @param embedded the embedded to set
	 */
	public void setEmbedded(EmbeddedObject embedded) {
		this.embedded = embedded;
	}

	/**
	 * @return the embedded
	 */
	public EmbeddedObject getEmbedded() {
		return embedded;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public RelatedObject getOne2one() {
		return one2one;
	}

	@RelationManagement(thisSide = "one2one", relationSide = "one2one")
	public void setOne2one(RelatedObject one2one) {
		this.one2one = one2one;
	}

	public Set<RelatedObject> getOne2many() {
		return one2many;
	}

	@RelationManagement( thisSide="one2many", relationSide="many2one")
	public void addOne2many( RelatedObject many ) {
		this.one2many.add( many );
	}

	@RelationManagement( thisSide="one2many", relationSide="many2one")
	public void removeOne2many( RelatedObject many ) {
		this.one2many.remove( many );
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean getActive(){
		return active;
	}

	@Override
	public void markDeleted() {
		if( isDeleteable() ){
			for( RelatedObject o : one2many )
				o.markDeleted();
			super.markDeleted();
		}
	}

	/**
	 * @param one2manyWrapped the one2manyWrapped to set
	 */
	public void setOne2manyWrapped(Set<RelatedObject> one2manyWrapped) {
		this.one2manyWrapped = one2manyWrapped;
	}

	/**
	 * @return the one2manyWrapped
	 */
	public Set<RelatedObject> getOne2manyWrapped() {
		return one2manyWrapped;
	}

}
