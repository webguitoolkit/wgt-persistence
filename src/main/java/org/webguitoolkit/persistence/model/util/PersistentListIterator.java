package org.webguitoolkit.persistence.model.util;

import java.util.ListIterator;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;



public class PersistentListIterator<T extends IPersistable> implements ListIterator<T>{

	private ListIterator<T> delegate;
	private IPersistable owner;

	public PersistentListIterator( IPersistable owner, ListIterator<T> delegate) {
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

	public boolean hasPrevious() {
		return delegate.hasPrevious();
	}

	public T previous() {
		return delegate.previous();
	}

	public int nextIndex() {
		return delegate.nextIndex();
	}

	public int previousIndex() {
		return delegate.previousIndex();
	}

	public void set(T e) {
		owner.markModified(ModificationType.CHANGED);
		delegate.set(e);
	}

	public void add(T e) {
		owner.markModified(ModificationType.CHANGED);
		delegate.add(e);
	}

}
