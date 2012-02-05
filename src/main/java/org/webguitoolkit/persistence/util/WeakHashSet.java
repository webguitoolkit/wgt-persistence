package org.webguitoolkit.persistence.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Implements a weak HashSet backed by a WeakHashMap. Does not support NULL
 * elements. The functionality is to enable the garbage collector to remove
 * entries from the set in case that the are not strongly referenced from the
 * using application. The special usage in the PersistenceFramework is that the
 * registered objects are kept in the "cache" but automatically removed if not
 * referenced by the application anymore.
 * 
 * @author peter
 * 
 * @param <Typ>
 */

public class WeakHashSet<Typ> implements Set<Typ> {

	private Map<Typ, Object> base;

	public WeakHashSet() {
		super();
		base = new WeakHashMap<Typ, Object>();
	}

	/**
	 * Add the object to the Set if not already contained. Throws an
	 * <code>IllegalArgumentException</code> if object is NULL
	 * 
	 * @return <code>true</code> if the Set was changed, <code>false</code>
	 *         otherwise.
	 * 
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(Typ object) {
		if (object == null)
			throw new IllegalArgumentException("do not add NULL to this Set");
		if (base.containsKey(object))
			return false;
		base.put(object, null);
		return true;
	}

	/**
	 * Add all elements from collection to the Set using the add method.
	 * 
	 * @return <code>true</code> if the Set was changed, <code>false</code>
	 *         otherwise.
	 * 
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Typ> collection) {
		boolean result = false;
		for (Typ object : collection) {
			boolean added = add(object);
			if (!result)
				result = added;
		}
		return result;
	}

	/**
	 * Clear the Set by creating a new buffer
	 */
	public void clear() {
		base = new WeakHashMap<Typ, Object>();
	}

	/**
	 * @retrun <code>true</code> if object is in the list.
	 * 
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object object) {
		return base.containsKey(object);
	}

	/**
	 * Returns <code>true</code> if this Set contains all the elements of the
	 * collection.
	 * 
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> collection) {
		for (Object object : collection) {
			if (!base.containsKey(object))
				return false;
		}
		return true;
	}

	/**
	 * @retun true if the Sets size is 0.
	 */
	public boolean isEmpty() {
		return base.size() > 0;
	}

	/**
	 * @return an iterator for the Set.
	 */
	public Iterator<Typ> iterator() {
		return base.keySet().iterator();
	}

	/**
	 * @return <code>true</code> if the Set was changed.
	 * 
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return base.remove(o) != null;
	}

	/**
	 * @return <code>true</code> if the Set was changed.
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		int removed = 0;
		for (Object object : c) {
			if (remove(object))
				removed++;
		}
		return removed > 0;
	}

	/**
	 * Throws an UnsupportedOperationException
	 */
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * @Returns the number of elements in this Set.
	 * @see java.util.Set#size()
	 */
	public int size() {
		return base.size();
	}

	/**
	 * @return an array containing all of the elements in this set based on the
	 *         buffers keySet().
	 */
	public Object[] toArray() {
		return base.keySet().toArray();
	}

	/**
	 * @return an array containing all of the elements in this set based on the
	 *         buffers keySet().
	 */
	@SuppressWarnings("hiding")
	public <Typ> Typ[] toArray(Typ[] a) {
		return (Typ[]) base.keySet().toArray(a);
	}
}
