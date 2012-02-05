/*
 * File: IBaseLastDateQuery.java
 * Copyright (c) 2007, Endress+Hauser Infoserve GmbH & Co KG.
 */
package org.webguitoolkit.persistence.query;

import java.util.Date;

import org.webguitoolkit.persistence.model.IPersistable;


/**
 * @author Wolfram Kaiser
 *
 */
public interface ILastDateQuery<T extends IPersistable> {
	public Date execute();
}
