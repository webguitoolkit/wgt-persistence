package org.webguitoolkit.persistence.model.util;

import java.util.Iterator;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;


public class PersistentIterator<T extends IPersistable> implements Iterator<T>{

	private Iterator<T> delegate;
	private IPersistable owner;

	public PersistentIterator( IPersistable owner, Iterator<T> delegate) {
		super();
		this.owner = owner;
		this.delegate = delegate;
	}

	public boolean hasNext() {
		return delegate.hasNext();
	}

	public T next() {
		return delegate.next();
	}

	public void remove() {
		owner.markModified(ModificationType.CHANGED);
		delegate.remove();
	}

}
