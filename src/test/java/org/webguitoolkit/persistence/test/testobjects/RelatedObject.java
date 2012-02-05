package org.webguitoolkit.persistence.test.testobjects;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.model.RelationManagement;
import org.webguitoolkit.persistence.model.RelationManagement.Mode;


@Entity
@Table(name = "TEST_RO")

public class RelatedObject extends Persistable implements ITextable {

	@Column(name="MESSAGE_TEXT", length=60 )
	private String text;

	@ManyToMany(fetch=FetchType.LAZY, mappedBy="many2many")
	private Set<TestObject> many2many;

	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy="one2one", optional = true)
	private TestObject one2one;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = true)
	private TestObject many2one;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, optional = true)
	private TestObject many2oneWrapped;

	/**
	 * Default constructor required by Hibernate
	 */
	@SuppressWarnings("unused")
	protected RelatedObject() {
		many2many = new HashSet<TestObject>();
	}
	
	public RelatedObject(String text) {
		this.text = text; 
		many2many = new HashSet<TestObject>();
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

	// MANY 2 MANY
	public Set<TestObject> getMany2many() {
		return many2many;
	}

	@RelationManagement( thisSide="many2many", relationSide="many2many")
	public void addMany2many(TestObject newRelated) {
		this.many2many.add( newRelated );
	}
	@RelationManagement( thisSide="many2many", relationSide="many2many")
	public void removeMany2many(TestObject newRelated) {
		this.many2many.remove( newRelated );
	}

	
	// ONE 2 ONE
	public TestObject getOne2one() {
		return one2one;
	}

	@RelationManagement(thisSide = "one2one", relationSide = "one2one")
	public void setOne2one(TestObject one2one) {
		this.one2one = one2one;
	}

	// MANY 2 ONE
	public TestObject getMany2one() {
		return many2one;
	}
	
	@RelationManagement(thisSide = "many2one", relationSide = "one2many", mode=Mode.MANUEL )
	public void setMany2one(TestObject many2one) {
		this.many2one = many2one;
	}

	/**
	 * @param many2oneWrapped the many2oneWrapped to set
	 */
	public void setMany2oneWrapped(TestObject many2oneWrapped) {
		this.many2oneWrapped = many2oneWrapped;
	}

	/**
	 * @return the many2oneWrapped
	 */
	public TestObject getMany2oneWrapped() {
		return many2oneWrapped;
	}


}
