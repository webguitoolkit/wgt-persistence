package org.webguitoolkit.persistence.query;

import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.util.DatabaseProvider;


public abstract class BaseQuery extends DatabaseProvider {

	public BaseQuery( PersistenceManager pm ){
		super(pm);
	}

}
