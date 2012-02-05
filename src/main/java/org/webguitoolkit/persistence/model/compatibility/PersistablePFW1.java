package org.webguitoolkit.persistence.model.compatibility;

import javax.persistence.MappedSuperclass;

import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.model.IPersistable;


/**
 * A base class for persistent object with better compatibility for older or already existing
 * PFW1 models and databases. Since PFW2 defines new attributs and columns for the technical
 * information such as version number PFW1 legacy classes/tables still require the old column
 * name or attribute name. 
 * 
 * @author Wolfram Kaiser
 */
@MappedSuperclass
//@AttributeOverrides( value={
//		@AttributeOverride( name="createdBy",  column = @Column() ),
//} )


public abstract class PersistablePFW1 extends Persistable implements IPersistable {

	protected PersistablePFW1() {
		super();
	}
}
