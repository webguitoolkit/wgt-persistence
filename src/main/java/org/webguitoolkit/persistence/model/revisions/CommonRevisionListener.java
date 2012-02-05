/**
 * 
 */
package org.webguitoolkit.persistence.model.revisions;

import org.hibernate.envers.RevisionListener;
import org.webguitoolkit.persistence.PersistenceContext;

/**
 * @author i102389
 *
 */
public class CommonRevisionListener implements RevisionListener {

	/**
	 * 
	 */
	public CommonRevisionListener() {
	}
	
	/**
	 * Populate revisionEntry with information from the current commit provided by the PersistenceManager
	 */
    public void newRevision(Object revisionEntity) {
    	CommonRevisionEntry typedRevision = (CommonRevisionEntry) revisionEntity;
    	
    	CommonRevisionEntry latestRevisionInfo = PersistenceContext.getPersistenceManager().getLatestRevisionInfo();
    	typedRevision.setUsername(latestRevisionInfo.getUsername());
    	typedRevision.setClientIP(latestRevisionInfo.getClientIP());
    	typedRevision.setProgramName(latestRevisionInfo.getProgramName());
    	typedRevision.setCriticality(latestRevisionInfo.getCriticality());
    }

}
