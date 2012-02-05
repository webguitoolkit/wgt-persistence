/**
 * 
 */
package org.webguitoolkit.persistence.model.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;


/**
 * @author i102389
 *
 */
public class PersistentStringMap implements Map<String, String>, IPersistentWrapper<Map<String,String>> {

	private Map<String, String> delegate;
	private IPersistable owner;
	
	/**
	 * 
	 */
	public PersistentStringMap(IPersistable newOwner, Map<String, String> newDelegate) {
		setOwner(newOwner);
		setDelegate(newDelegate);
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		getDelegate().clear();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object lookupKey) {
		return getDelegate().containsKey(lookupKey);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object lookupValue) {
		return getDelegate().containsValue(lookupValue);
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<String, String>> entrySet() {
		return getDelegate().entrySet();
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public String get(Object lookupKey) {
		return getDelegate().get(lookupKey);
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return getDelegate().isEmpty();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return getDelegate().keySet();
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public String put(String key, String value) {
		owner.markModified(ModificationType.CHANGED);
		return getDelegate().put(key, value);
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public String remove(Object lookupKey) {
		owner.markModified(ModificationType.CHANGED);
		return getDelegate().remove(lookupKey);
	}

	/**
	 * @see java.util.Map#size()
	 */
	public int size() {
		return getDelegate().size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection<String> values() {
		return getDelegate().values();
	}

	/**
	 * @return the delegate
	 */
	public Map<String, String> getDelegate() {
		return delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	protected void setDelegate(Map<String, String> newDelegate) {
		delegate = newDelegate;
	}

	/**
	 * @return the owner
	 */
	protected IPersistable getOwner() {
		return owner;
	}

	/**
	 * @param newOwner the owner to set
	 */
	protected void setOwner(IPersistable newOwner) {
		owner = newOwner;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends String> copyMap) {
		owner.markModified(ModificationType.CHANGED);
		getDelegate().putAll(copyMap);
	}
}
