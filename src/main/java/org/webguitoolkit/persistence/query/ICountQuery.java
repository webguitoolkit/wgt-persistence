/*
 * File: IBaseCountQuery.java
 * Copyright (c) 2006, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query;

import org.webguitoolkit.persistence.model.IPersistable;

/**
 * Base class for querying the model and returning the count of found objects.
 * 
 * @author Martin Hermann
 */
public interface ICountQuery<T extends IPersistable> {
	public long execute();
}
