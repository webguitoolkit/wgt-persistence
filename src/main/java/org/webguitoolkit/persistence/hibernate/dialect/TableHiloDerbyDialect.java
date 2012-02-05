package org.webguitoolkit.persistence.hibernate.dialect;

import org.hibernate.dialect.DerbyDialect;
import org.hibernate.id.TableHiLoGenerator;

/**
 * 
 * 
 * @author i01002534
 *
 */
public class TableHiloDerbyDialect extends DerbyDialect {

	public TableHiloDerbyDialect() {
		super();
	}

	@Override
	public Class getNativeIdentifierGeneratorClass() {
		return TableHiLoGenerator.class;
	}

}