package org.webguitoolkit.persistence.model.util;

import java.util.List;
import java.util.Set;

import org.webguitoolkit.persistence.model.IPersistable;

public class ObserverHelper<T extends IPersistable> {
	
	public PersistentList<T> wrapWithObserver( IPersistable owner, List<T> list ){
		return new PersistentList<T>(owner, list);
	}
	public PersistentSet<T> wrapWithObserver( IPersistable owner, Set<T> list ){
		return new PersistentSet<T>(owner, list);
	}
	public PersistentSet<T> wrapWithObserver( IPersistable owner, Set<T> list, String thisSide, String otherSide ){
		return new PersistentSet<T>(owner, list, thisSide, otherSide );
	}
}
