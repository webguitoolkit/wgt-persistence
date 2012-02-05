package org.webguitoolkit.persistence.hibernate.dialect;
import org.hibernate.dialect.DB2Dialect;

/**
 * @author i102454
 *
 */
public class DB2HibernateDialect  extends DB2Dialect {

    public DB2HibernateDialect() {
        super();
    }

    //Overwrites existing db2 driver class to support UTF-8 tables
	public String getTableTypeString() {
		return " CCSID UNICODE ";
	}
	public boolean supportsCascadeDelete() {
		return true;
	}
}
