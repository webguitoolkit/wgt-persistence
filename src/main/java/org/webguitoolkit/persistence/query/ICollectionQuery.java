package org.webguitoolkit.persistence.query;

import java.util.Collection;

import org.webguitoolkit.persistence.model.IPersistable;


public interface ICollectionQuery<T extends IPersistable> {
	public Collection<T> execute();
}
