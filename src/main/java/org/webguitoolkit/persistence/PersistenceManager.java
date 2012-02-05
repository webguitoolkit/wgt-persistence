package org.webguitoolkit.persistence;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IQueryFactory;
import org.webguitoolkit.persistence.model.RelationManagement.Mode;
import org.webguitoolkit.persistence.model.revisions.CommonRevisionEntry;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;
import org.webguitoolkit.persistence.util.WeakHashSet;

/**
 * The Persistence manager is used to interact with the underlying DB by means of an OR mapping layer (e.g. Hibernate). Each user
 * session will have its own PersistenceManager. The PM is not directly dependent on a OR mapping implementation such as
 * Hibernate. The dependency is abstracted by using the IPersistenceUtility interface * To use the PersistenceManager you will
 * need an implementation of IPersistenceUtility e.g. the HibernateUtility. <br>
 * The PM shall be reused over request/response cycles for an user session in a web application. The functionality of the
 * Persistence Manager is :<br>
 * - keep a list of used persistent object i.e. those that are implementing IPersistable or extending Persistable<br>
 * - maintains a list of dirty object and provides access to the Query Factory and Model Factory.<br>
 * - providing functionality to perform database transactions<br>
 * - support for state-less web applications to detach and attach used objects to database sessions - transparent usage of
 * persistent objects, i.e. neither the application nor the model classes have to care about transactions<br>
 * - some logging support for change log
 * 
 * 
 * @author Peter, Martin
 * 
 */
public class PersistenceManager {

	public static final String DEFAULT_COMMIT_USERID = "UNKOWN";
	private static Log log = LogFactory.getLog(PersistenceManager.class);

	private IPersistenceUtility persistenceUtility;

	/**
	 * The set is used to check whether objects are already managed by the PersistenceManager.
	 */
	private Set<Persistable> dirtyObjects;
	/**
	 * The list is used to preserve the order in which objects are added to the PersistenceManager. This information is used
	 * during commit to prevent exceptions due to cascade dependencies.
	 */
	private List<Persistable> sortedDirtyObjects;
	/**
	 * the objects that are in use
	 */
	private WeakHashSet<Persistable> objectsInUse;
	private boolean isAttached = false;

	/**
	 * The latest revision info contains some information from the calling application about the user, main program, calling client, etc
	 * The latest revision info is only available during commit and might be used by the CommonRevisionListener to get more
	 * information about the revision which is about to be committed to the database.
	 * The calling application is expected to provide these information for each session/commit.
	 */
	private CommonRevisionEntry latestRevisionInfo;

	private Map<String, IQueryFactory> queryFactories;
	
	/**
	 * the relation management mode defines if the relation management is done always (AUTO) or can be disabled by the annotation when 
	 * the mode is manual
	 */
	private Mode relationManagementMode = Mode.AUTO;

	public PersistenceManager(IPersistenceUtility persUtil) {
		setPersistenceUtility(persUtil);
		dirtyObjects = new HashSet<Persistable>();
		objectsInUse = new WeakHashSet<Persistable>();
		queryFactories = new HashMap<String, IQueryFactory>();
		sortedDirtyObjects = new ArrayList<Persistable>();
		setLatestRevisionInfo(new CommonRevisionEntry(DEFAULT_COMMIT_USERID));
	}

	/**
	 * @see com.endress.infoserve.persistence.IPersistenceManager#markDirty(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void markDirty(Persistable po) {
		if (po != null) {
			boolean isAddedObjects = dirtyObjects.add(po);
			if (isAddedObjects) {
				// add only new objects to the list
				sortedDirtyObjects.add(po);
			}
		}
	}

	/**
	 * @see com.endress.infoserve.persistence.IPersistenceManager#unmarkDirty(org.webguitoolkit.persistence.model.IPersistable)
	 */
	public void unmarkDirty(IPersistable po) {
		if (po != null) {
			boolean isRemovedObject = dirtyObjects.remove(po);
			if (isRemovedObject) {
				// remove only existing objects from the list
				sortedDirtyObjects.remove(po);
			}
		}
	}

	/**
	 * Detaches all registered objects from the PersistenceUtility Session
	 */
	public void detach() {
		getPersistenceUtility().detach();
		isAttached = false;
	}

	/**
	 * attaches all objects in use to the session
	 */
	public void attach() {
		// iterate over a copy because of possible ConcurrentModifications
		List<IPersistable> copy = new ArrayList<IPersistable>(objectsInUse);
		for (IPersistable po : copy) {
			if (po != null) {
				getPersistenceUtility().attach(po);
			}
		}
		isAttached = true;
	}

	/**
	 * This method will start a transaction manually, this can be useful when changing objects and call a query afterwards. The
	 * changed objects are then saved to the database (not committed!) and later you can commit the transaction or roll back it.
	 */
	public void transactionBegin() {
		getPersistenceUtility().beginTransaction();
	}

	/**
	 * saves all dirty objects and commits the transaction
	 * 
	 * @throws PersistenceException when there are errors saving the objects to the database
	 */
	public void transactionCommit() throws PersistenceException {
		commit();
	}

	/**
	 * saves all dirty objects and commits the transaction
	 * 
	 * @throws PersistenceException when there are errors saving the objects to the database
	 */
	public void transactionRollback() throws PersistenceException {
		rollback();
	}

	/**
	 * 
	 * Persist all changes made on the registered objects. Updates the modified, creates the new, removes the deleted by means of
	 * the PersistenceUtility. After successful work the objects change state will be reset. Note that objects may remain in the
	 * registered objects as long as there still strong references from the application to them. The cache will be combed out by
	 * the garbage collector if not strong references pointing to the Persistable in the registered objects.
	 */
	public void commit(String userid) throws PersistenceException {
		log.trace(">>> commitDataStore by " + userid );

		String originalUserId = getUserid();

		// concurrent modification exceptions should not occur because each thread should have
		// its own PersistenceManager. Thus, we do not need to work with a copied collection here anymore.
		// furthermore, we need the objects according to their insert order to the transaction

		List<Persistable> objects4Cleanup = null;
		try {
			setUserid(userid);

			// only starts a transaction when there is no transaction open
			getPersistenceUtility().beginTransaction();

			// save the objects to the database
			objects4Cleanup = saveDirtyObjects(userid);

			// commit the transaction
			getPersistenceUtility().commitTransaction();

		}
		catch (ConcurrentChangeException e) {
			try {
				log.fatal("********** Commit failed - rollback transaction ************  ", e);
				rollback();
			}
			catch (Exception e1) {
				log.fatal("********* Rollback failed - close session ***********  ", e1);
			}
			finally {
				try {
					close();
				}
				catch (PersistenceException e2) {
					log.fatal("******** Close failed - give up *********  ", e2);
					throw e2;
				}
			}
			throw e;
		}
		catch (PersistenceException e) {
			try {
				log.fatal("********** Commit failed - rollback transaction ************  ", e);
				rollback();
			}
			catch (Exception e1) {
				log.fatal("********* Rollback failed - close session ***********  ", e1);
			}
			finally {
				try {
					close();
				}
				catch (PersistenceException e2) {
					log.fatal("******** Close failed - give up *********  ", e2);
					throw e2;
				}
			}
			throw e;
		}
		finally {
			// MH 15.10.09
			// moved cleanup here cause rollback is not working if the objects where cleand in the save method
			cleanupDirtyObjects(objects4Cleanup);
			log.trace("<<< commitDataStore by " + userid);
			setUserid(originalUserId);
		}
	}

	/**
	 * iterates over all dirty objects and calls save on all new objects, update on all changed objects and delete on all deleted
	 * objects.
	 * 
	 * @param userid the user that has done the changes
	 * @throws PersistenceException
	 */
	protected List<Persistable> saveDirtyObjects(String userid) throws PersistenceException {

		// keep list of all objects for later cleanup
		// DM: prevent java.util.ConcurrentModificationException (po.reset calls unmarkDiryt which modifies the
		// dirtyObjects
		List<Persistable> dirtyObjectsForCleanup = new ArrayList<Persistable>(sortedDirtyObjects);

		List<Persistable> changedObjects = new ArrayList<Persistable>();
		List<Persistable> newObjects = new ArrayList<Persistable>();
		List<Persistable> deletedObjects = new ArrayList<Persistable>();

		for (Iterator<Persistable> it = dirtyObjectsForCleanup.iterator(); it.hasNext();) {
			Persistable po = it.next();
			switch( po.getModificationType() ){
				case NEW:
					po.setCreatedBy(userid);
					getPersistenceUtility().save(po);
					newObjects.add(po);
					addObjectInUse(po);
					break;
				case CHANGED:
					po.setModifiedBy(userid);
					getPersistenceUtility().update(po);
					changedObjects.add(po);
					break;
				case DELETED:
					getPersistenceUtility().delete(po);
					deletedObjects.add(po);
					removeObjectInUse(po);
					sortedDirtyObjects.remove(po);
					break;
			}
		}

		// writing objects may re-attach them to the session so we need to write them before we commit the
		// transaction
		writeChangeLog(changedObjects, newObjects, deletedObjects);

		// MH 15.10.09
		// moved cleanup to end of commit because rollback is not working if the objects where cleaned here
		return dirtyObjectsForCleanup;
	}

	/**
	 * Commit the changes of the registered objects.
	 */
	public void commit() throws PersistenceException {
		commit(getUserid());
	}

	/**
	 * Roll back the transaction.
	 */
	public void rollback() throws PersistenceException {
		log.trace(">>> rollbackDataStore() ");

		// keep list of all objects for later cleanup
		// DM: prevent java.util.ConcurrentModificationException (po.reset calls unmarkDiryt which modifies the
		// dirtyObjects
		List<Persistable> dirtyObjectsForCleanup = new ArrayList<Persistable>(sortedDirtyObjects);

		try {

			for (Iterator<Persistable> it = dirtyObjectsForCleanup.iterator(); it.hasNext();) {
				Persistable po = it.next();
				switch( po.getModificationType() ){
					case NEW:
						// some new objects are marked as new and as changed. Thus,
						// undoing the new-operation takes precedence.
						removeObjectInUse(po);
						sortedDirtyObjects.remove(po);
						break;
					case CHANGED:
						// Refresh objects state
						getPersistenceUtility().refresh(po);
						break;
					case DELETED:
						// Refresh objects state
						getPersistenceUtility().refresh(po);
						// MH 15.10.09
						// added the object to the objectsInUse again
						addObjectInUse(po);
						break;
				}
			}
			getPersistenceUtility().rollbackTransaction();
		}
		catch (Exception e) {
			log.fatal("********** rollback failed - close session ", e);
			try {
				close();
			}
			catch (PersistenceException e2) {
				log.fatal("******** Close failed - give up *********  ", e2);
				throw e2;
			}
		}
		finally {
			cleanupDirtyObjects(dirtyObjectsForCleanup);
			log.trace("<<< rollbackDataStore");
		}
	}

	/**
	 * @param dirtyObjectsForCleanup
	 */
	protected void cleanupDirtyObjects(List<Persistable> dirtyObjectsForCleanup) {
		clearDirtyObjects();
		if (dirtyObjectsForCleanup != null) {
			for (Persistable po : dirtyObjectsForCleanup) {
				po.reset(); // modifies the dirtyObjects colletion as well - thus, work with a copy of the original
				// collection!
			}
		}
	}

	/**
	 * reload the passed Persistable from the underlying data store
	 */
	public void refresh(IPersistable o) throws PersistenceException {
		getPersistenceUtility().refresh(o);
	}

	/**
	 * reload the passed Persistables from the underlying data store
	 */
	public void refresh(Collection<? extends IPersistable> objects) throws PersistenceException {
		if (objects != null && !objects.isEmpty()) {
			for (IPersistable o : objects) {
				refresh(o);
			}
		}
	}

	/**
	 * @return the persistence utility
	 */
	public IPersistenceUtility getPersistenceUtility() {
		return persistenceUtility;
	}

	public void setPersistenceUtility(IPersistenceUtility newPersistenceUtility) {
		persistenceUtility = newPersistenceUtility;
	}

	/**
	 * close the current session at the PersistenceUtility
	 */
	public void close() throws PersistenceException {
		if (sortedDirtyObjects.size() > 0) {
			StringBuffer sb = new StringBuffer("Closing Session without commit!\nThere are still dirty objects in the session:\n");
			for (IPersistable pers : sortedDirtyObjects) {
				sb.append(pers.getClass().getSimpleName() + " ID: " + pers.getId());
			}
			log.warn(sb.toString());
		}
		getPersistenceUtility().close();

		// clean-up any remaining dirty objects so the PersistenceManager is in a consistent state after closing
		clearDirtyObjects();
		isAttached = false;
	}

	/**
	 * @return the current "owner" of the PM
	 */
	public String getUserid() {
		String currentUserId = getLatestRevisionInfo().getUsername();
		if (StringUtils.isEmpty(currentUserId)) {
			currentUserId = DEFAULT_COMMIT_USERID;
		}
		return currentUserId;
	}

	/**
	 * The the userid for logging purposes
	 */
	public void setUserid(String newUserid) {
		getLatestRevisionInfo().setUsername(newUserid);
	}

	/**
	 * @return the list of modified objects.
	 */
	public Set<Persistable> getDirtyObjects() {
		return dirtyObjects;
	}

	/**
	 * 
	 */
	public void clearDirtyObjects() {
		if (dirtyObjects != null)
			dirtyObjects.clear();
		if (sortedDirtyObjects != null)
			sortedDirtyObjects.clear();
	}

	/**
	 * For debugging purposes.
	 */
	public boolean isDirty(IPersistable checkPersistable) {
		return dirtyObjects.contains(checkPersistable);
	}

	private void writeChangeLog(List<Persistable> changedObjects, List<Persistable> newObjects, List<Persistable> deletedObjects) {
		for (Iterator<Persistable> it = newObjects.iterator(); it.hasNext();) {
			Persistable po = it.next();
			log.info(po.getModifiedBy() + " CREATED " + po.getLogInfo());
		}
		for (Iterator<Persistable> it = deletedObjects.iterator(); it.hasNext();) {
			Persistable po = it.next();
			log.info(getUserid() + " DELETED " + po.getLogInfo());
		}
		for (Iterator<Persistable> it = changedObjects.iterator(); it.hasNext();) {
			Persistable po = it.next();
			SimpleDateFormat sd = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
			sd.setTimeZone(TimeZone.getTimeZone("GMT"));
			String modTime = "";
			if (po.getModifiedAt() != null) {
				modTime = sd.format(new Date(po.getModifiedAt().getTime()));
			}
			log.info(po.getModifiedBy() + " CHANGED at '" + modTime + "' " + po.getLogInfo() + " " + po.listModifications());
		}
	}

	public IQueryFactory findQueryFactory(String clazz) {
		return queryFactories.get(clazz);
	}

	public Map<String, IQueryFactory> getQueryFactories() {
		return queryFactories;
	}

	public void addQueryFactory(String modelClassname, IQueryFactory queryFactory) {
		queryFactories.put(modelClassname, queryFactory);
	}

	public Set<Persistable> getObjectsInUse() {
		return objectsInUse;
	}

	public void addObjectInUse(Persistable newObjectInSession) {
		objectsInUse.add(newObjectInSession);
	}

	public void removeObjectInUse(IPersistable oldObjectInSession) {
		objectsInUse.remove(oldObjectInSession);
	}
	
	/**
	 * @return the latestRevisionInfo
	 */
	public CommonRevisionEntry getLatestRevisionInfo() {
		return latestRevisionInfo;
	}

	/**
	 * @param newLatestRevisionInfo the latestRevisionInfo to set
	 */
	public void setLatestRevisionInfo(CommonRevisionEntry newLatestRevisionInfo) {
		latestRevisionInfo = newLatestRevisionInfo;
	}

	public void printStatistics(PrintStream out) {
		out.println("---------------------------------------------------------------------------");
		out.println("| PeristenceManager Statistics ");
		out.println("---------------------------------------------------------------------------");
		out.println("| User: " + getUserid());
		out.println("| ");
		out.println("| Objects in use: " + objectsInUse.size());
		for (Persistable usedObject : objectsInUse) {
			out.print("| " + usedObject.getClass().getSimpleName() + " " + usedObject.getLogInfo());
			IPersistenceUtility utility = getPersistenceUtility().getDelegatePersistenceUtilityForClass(usedObject.getClass().getName());
			if (utility instanceof HibernateUtility) {
				if (((HibernateUtility)utility).getSession().contains(usedObject))
					out.print(" * ");
			}
			out.println();
		}
		out.println("| * marks attached objects");
		out.println("| ");
		out.println("| Dirty Objects: " + dirtyObjects.size());
		for (Persistable dirtyObject : dirtyObjects) {
			out.println("| " + dirtyObject.getLogInfo());
		}
		out.println("| ");
		out.println("---------------------------------------------------------------------------");
	}

	protected Mode getRelationManagementMode() {
		return relationManagementMode;
	}
	public void setManualRelationManagement() {
		this.relationManagementMode = Mode.MANUEL;
	}

	public boolean isAttached() {
		return isAttached;
	}

}
