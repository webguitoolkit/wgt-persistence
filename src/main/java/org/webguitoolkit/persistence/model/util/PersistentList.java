package org.webguitoolkit.persistence.model.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;


public class PersistentList<T extends IPersistable> implements List<T>, IPersistentWrapper<List<T>>{

	private List<T> delegate;
	private IPersistable owner;

	public PersistentList( IPersistable owner, List<T> delegate ) {
		super();
		this.delegate = delegate;
		this.owner = owner;
	}

	public List<T> getDelegate() {
		return delegate;
	}
	
	public boolean add(T arg0) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.add(arg0);
	}

	public boolean addAll(Collection<? extends T> arg0) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.addAll(arg0);
	}

	public void clear() {
		delegate.clear();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Iterator<T> iterator() {
		return new PersistentIterator<T>( owner, delegate.iterator() );
	}

	public boolean remove( Object o ) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.remove(o);
	}

	public int size() {
		return delegate.size();
	}

	public Object[] toArray() {
		return delegate.toArray();
	}

	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll( c );
	}

	public boolean removeAll(Collection<?> c) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.removeAll( c );
	}

	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll( c );
	}

	public <T2> T2[] toArray(T2[] a) {
		return delegate.toArray( a );
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.addAll(index, c);
	}

	public T get(int index) {
		return delegate.get(index);
	}

	public T set(int index, T element) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.set(index, element);
	}

	public void add(int index, T element) {
		owner.markModified(ModificationType.CHANGED);
		delegate.add(index, element);
	}

	public T remove(int index) {
		owner.markModified(ModificationType.CHANGED);
		return delegate.remove(index);
	}

	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return new PersistentListIterator<T>(owner, delegate.listIterator());
	}

	public ListIterator<T> listIterator(int index) {
		return new PersistentListIterator<T>(owner, delegate.listIterator(index));
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}
}
