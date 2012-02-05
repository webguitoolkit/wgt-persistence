/**
 * 
 */
package org.webguitoolkit.persistence.hibernate.dialect;

import java.sql.Types;

/**
 * With Hibernate 3.6.0 a new LongVarchar type is introduced to be mapped from TextType.
 * However, this type is not registered in the default SAPDBDialect in Hibernate.
 * 
 * @author i102389
 *
 */
public class SAPDBDialect extends org.hibernate.dialect.SAPDBDialect {

	/**
	 * 
	 */
	public SAPDBDialect() {
		super();
		registerColumnType( Types.LONGVARCHAR, "long varchar" );
	}

}
