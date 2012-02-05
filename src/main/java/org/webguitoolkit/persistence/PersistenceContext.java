package org.webguitoolkit.persistence;

public class PersistenceContext {

	private static ThreadLocal<PersistenceManager> pm = new ThreadLocal<PersistenceManager>();

	public static PersistenceManager getPersistenceManager() {
		return pm.get();
	}

	public static void setPersistenceManager(PersistenceManager pmgr) {
		pm.set(pmgr);
	}

}
