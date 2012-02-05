package org.webguitoolkit.persistence.query;

import org.webguitoolkit.persistence.model.IPersistable;

public interface IObjectQuery<T extends IPersistable> {
	public T execute() throws NonUniqueResultException;
}
