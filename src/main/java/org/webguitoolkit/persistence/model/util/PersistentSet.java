package org.webguitoolkit.persistence.model.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.RelationManagementHelper;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;


public class PersistentSet<T extends IPersistable> implements Set<T>, IPersistentWrapper<Set<T>> {

	private Set<T> delegate;
	private IPersistable owner;
	private String otherSide;
	private boolean isRelationManaged;
	private String thisSide;

	public PersistentSet( IPersistable owner, Set<T> delegate ) {
		super();
		this.delegate = delegate;
		this.owner = owner;
	}
	
	public PersistentSet( IPersistable owner, Set<T> delegate, String thisSide, String otherSide ) {
		super();
		this.delegate = delegate;
		this.owner = owner;
		this.thisSide = thisSide;
		this.otherSide = otherSide;
		if( thisSide != null )
			isRelationManaged = true;
	}

	public Set<T> getDelegate() {
		return delegate;
	}
	
	public boolean add(T arg0) {
		owner.markModified(ModificationType.CHANGED);
		if( isRelationManaged ){
			RelationManagementHelper.manageRelation(owner, thisSide, arg0, otherSide, "add"+thisSide );
		}
		return delegate.add(arg0);
	}

	public boolean addAll(Collection<? extends T> arg0) {
		owner.markModified(ModificationType.CHANGED);
		if( isRelationManaged ){
			for( T t : arg0 )
				RelationManagementHelper.manageRelation(owner, thisSide, t, otherSide, "add"+thisSide );
		}
		return delegate.addAll(arg0);
	}

	public void clear() {
		if( isRelationManaged ){
			Collection<T> clone = new HashSet<T>( delegate );
			for( T t : clone )
				RelationManagementHelper.manageRelation(owner, thisSide, t, otherSide, "remove"+thisSide );
		}
		else
			delegate.clear();
	}

	public boolean contains(T o) {
		return delegate.contains(o);
	}


	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Iterator<T> iterator() {
		return new PersistentIterator<T>( owner, delegate.iterator() );
	}

	public boolean remove( Object o ) {
		owner.markModified(ModificationType.CHANGED);
		if( isRelationManaged ){
			RelationManagementHelper.manageRelation(owner, thisSide, o, otherSide, "remove"+thisSide );
			return true;
		}
		else
			return delegate.remove(o);
	}

	public int size() {
		return delegate.size();
	}

	public Object[] toArray() {
		return delegate.toArray();
	}

	public Object[] toArray(T[] arg0) {
		return delegate.toArray(arg0);
	}

	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll( c );
	}

	public boolean removeAll(Collection<?> c) {
		owner.markModified(ModificationType.CHANGED);
		if( isRelationManaged ){
			for( Object o : c )
				RelationManagementHelper.manageRelation(owner, thisSide, o, otherSide, "remove"+thisSide );
			return true;
		}
		else
			return delegate.removeAll( c );
	}

	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll( c );
	}

	public <T2> T2[] toArray(T2[] a) {
		return delegate.toArray( a );
	}

}
