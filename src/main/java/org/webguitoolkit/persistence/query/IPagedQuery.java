package org.webguitoolkit.persistence.query;

import java.util.Collection;

import org.webguitoolkit.persistence.model.IPersistable;


/**
 * Interface for paged queries see AbstractPagedQuery
 * 
 * @author Martin Hermann
 *
 * @param <T> the interface of the searched class
 */
public interface IPagedQuery<T extends IPersistable> {

	/**
	 * @return true if there are more results
	 */
	public boolean hasNext();

	/**
	 * @return the next portion of the result
	 */
	public Collection<T> next();
}
