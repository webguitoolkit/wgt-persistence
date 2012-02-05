/**
 * 
 */
package org.webguitoolkit.persistence.test.model;

import org.hibernate.PropertyValueException;
import org.hibernate.cfg.Configuration;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.event.AuditEventListener;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionRemoveEventListener;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.webguitoolkit.persistence.PersistenceException;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.revisions.CommonRevisionEntry;
import org.webguitoolkit.persistence.test.BaseHibernateTest;
import org.webguitoolkit.persistence.test.testobjects.PersistentObjectPair1;
import org.webguitoolkit.persistence.test.testobjects.PersistentObjectPair2;
import org.webguitoolkit.persistence.test.testobjects.PersistentObjectPair3;
import org.webguitoolkit.persistence.util.HibernateUtility;

/**
 * @author i102389
 *
 */
public class OneToOneTest extends BaseHibernateTest {

	/**
	 * @param name
	 */
	public OneToOneTest(String name) {
		super(name);
	}

	/**
	 * 
	 */
	public OneToOneTest() {
	}

	/**
	 * @see com.endress.infoserve.persistence.test.BaseHibernateTest#getAnnotatedClasses()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Class<IPersistable>[] getAnnotatedClasses() {
		System.out.println("GETANNOTATEDCLASSES CALLED");
		return new Class[] {PersistentObjectPair1.class, PersistentObjectPair2.class, PersistentObjectPair3.class, CommonRevisionEntry.class};
	}

	/**
	 * @see com.endress.infoserve.persistence.test.BaseHibernateTest#getLog4jXmlFilename()
	 */
	@Override
	protected String getLog4jXmlFilename() {
		return TestCaseForPFW.LOG4J_PFW_XML_FILE;
	}
	
	/**
	 * @see com.endress.infoserve.persistence.test.BaseHibernateTest#getHibernateConfigId()
	 */
	@Override
	protected String getHibernateConfigId() {
		return getClass().toString() + "-hibernateconfigid";
	}

	/**
	 * The insert order is important for Hibernate if one required partner (foreign key column with nullable=false)
	 * is not yet persistent during save (PersistenceManager.commit()). This test uses the wrong insert order and
	 * should therefore fail.
	 * 
	 * 
	 * @throws PersistenceException
	 */
	public void test01RequiredPartnerPairing1() throws PersistenceException {
		PersistentObjectPair1 poPair1 = new PersistentObjectPair1("poPair1", null);
		PersistentObjectPair2 poPair2 = new PersistentObjectPair2("poPair2", poPair1);
		
		assertSame("RequiredPartner2 should be set", poPair2, poPair1.getRequiredPartner2());
		assertSame("RequiredPartner1 should be set", poPair1, poPair2.getRequiredPartner1());

		try {
			getPersistenceManager().commit();
			fail();
		}
		catch (PersistenceException pexc) {
			assertTrue("Wrong expected exception", pexc.getCause() instanceof PropertyValueException);
		}
	}

	/**
	 * The insert order is important for Hibernate if one required partner (foreign key column with nullable=false)
	 * is not yet persistent during save (PersistenceManager.commit()). This test uses the correct insert order and
	 * should therefore succeed.
	 * 
	 * @throws PersistenceException
	 */
	public void test02RequiredPartnerPairing2() throws PersistenceException {
		PersistentObjectPair2 poPair2 = new PersistentObjectPair2("poPair2", null);
		PersistentObjectPair1 poPair1 = new PersistentObjectPair1("poPair1", poPair2);
		
		assertSame("RequiredPartner2 should be set", poPair2, poPair1.getRequiredPartner2());
		assertSame("RequiredPartner1 should be set", poPair1, poPair2.getRequiredPartner1());

		getPersistenceManager().commit("user1");
		
		HibernateUtility hu = getHibernateSession();
		AuditReader auditReader = AuditReaderFactory.get(hu.getSession());

		assertEquals("wrong number of expected revision", 1, auditReader.getRevisions(PersistentObjectPair1.class, poPair1.getId()).size());
		CommonRevisionEntry latestRevision = auditReader.findRevision(CommonRevisionEntry.class, new Integer(1));
		assertNotNull("expected revision is not found", latestRevision);
		assertEquals("wrong user for revision", "user1", latestRevision.getUsername());
		PersistentObjectPair1 revObject = auditReader.find(PersistentObjectPair1.class, poPair1.getId(), 1);
		assertNotNull("revision object should be stored", revObject);
		assertEquals("message text should be the same", poPair1.getMessage(), revObject.getMessage());
		assertEquals("version number should be the same", poPair1.getVersionNo(), revObject.getVersionNo());
		assertEquals("object id should be the same", poPair1.getId(), revObject.getId());
		
		poPair1.setMessage("updated message");
		getPersistenceManager().commit();

		assertEquals("wrong number of expected revision", 2, auditReader.getRevisions(PersistentObjectPair1.class, poPair1.getId()).size());
		latestRevision = auditReader.findRevision(CommonRevisionEntry.class, new Integer(2));
		assertEquals("wrong user for revision", PersistenceManager.DEFAULT_COMMIT_USERID, latestRevision.getUsername());
		assertNotNull("expected revision is not found", latestRevision);
		PersistentObjectPair1 revObject2 = auditReader.find(PersistentObjectPair1.class, poPair1.getId(), 1);
		assertNotNull("revision object should be stored", revObject2);
		assertFalse("message text should have been changed", poPair1.getMessage().equals(revObject2.getMessage()));
		assertFalse("version number should have been changed", poPair1.getVersionNo() == revObject2.getVersionNo());
		assertEquals("object id should be the same", poPair1.getId(), revObject2.getId());
		
		poPair1.markDeleted();
		poPair2.markDeleted();
		
		getPersistenceManager().commit("user1");

		// the last object should be the same as the deleted object (in general: the last revision is always the same as the current object)
		assertEquals("wrong number of expected revision", 3, auditReader.getRevisions(PersistentObjectPair1.class, poPair1.getId()).size());
		latestRevision = auditReader.findRevision(CommonRevisionEntry.class, new Integer(3));
		assertNotNull("expected revision is not found", latestRevision);
		assertEquals("wrong user for revision", "user1", latestRevision.getUsername());
		PersistentObjectPair1 revObject3 = auditReader.find(PersistentObjectPair1.class, poPair1.getId(), 3);
		assertNull("deleted objects should not be included in latest revision", revObject3);
//		assertNotNull("revision object should be stored", revObject3);
//		assertEquals("message text should be the same", poPair1.getMessage(), revObject3.getMessage());
//		//assertEquals("version number should be the same", poPair1.getVersionNo(), revObject3.getVersionNo());
//		assertEquals("object id should be the same", poPair1.getId(), revObject3.getId());
		
	}

	/**
	 * The insert order is unimportant for Hibernate with optional partner (foreign key column can be nullable).
	 * 
	 * @throws PersistenceException
	 */
	public void test03OptionalPartnerPairing1() throws PersistenceException {
		PersistentObjectPair2 poPair2 = new PersistentObjectPair2("poPair2", null);
		PersistentObjectPair1 poPair1 = new PersistentObjectPair1("poPair1", poPair2);
		PersistentObjectPair3 poPair3 = new PersistentObjectPair3("poPair3");
		
		poPair1.setOptionalPartner3(poPair3);
		poPair2.setOptionalPartner3(poPair3);
		
		assertSame("RequiredPartner2 should be set", poPair2, poPair1.getRequiredPartner2());
		assertSame("RequiredPartner1 should be set", poPair1, poPair2.getRequiredPartner1());
		assertSame("OptionalPartner3 should be set", poPair3, poPair1.getOptionalPartner3());
		assertSame("OptionalPartner3 should be set", poPair3, poPair2.getOptionalPartner3());
		
		getPersistenceManager().commit();
	}

	/**
	 * The insert order is unimportant for Hibernate with optional partner (foreign key column can be nullable).
	 * 
	 * @throws PersistenceException
	 */
	public void test04OptionalPartnerPairing2() throws PersistenceException {
		PersistentObjectPair3 poPair3 = new PersistentObjectPair3("poPair3");
		PersistentObjectPair2 poPair2 = new PersistentObjectPair2("poPair2", null);
		PersistentObjectPair1 poPair1 = new PersistentObjectPair1("poPair1", poPair2);
		
		poPair3.setOptionalPartner1(poPair1);
		poPair3.setOptionalPartner2(poPair2);
		
		assertSame("RequiredPartner2 should be set", poPair2, poPair1.getRequiredPartner2());
		assertSame("RequiredPartner1 should be set", poPair1, poPair2.getRequiredPartner1());
		assertSame("OptionalPartner3 should be set", poPair3, poPair1.getOptionalPartner3());
		assertSame("OptionalPartner3 should be set", poPair3, poPair2.getOptionalPartner3());
		
		getPersistenceManager().commit();
	}
	
	@Override
	protected Configuration createHibernateConfiguration( boolean createDrop ) {
		Configuration baseConfiguration = super.createHibernateConfiguration(createDrop);
		// configure Hibernate support for revisions 
		
		AuditEventListener auditEventListener = new AuditEventListener();
//		EJB3PostInsertEventListener ejbInsertEventListener = new EJB3PostInsertEventListener();
//		EJB3PostUpdateEventListener ejbUpdateEventListener = new EJB3PostUpdateEventListener();
//		EJB3PostDeleteEventListener ejbDeleteEventListener = new EJB3PostDeleteEventListener();
		baseConfiguration.setListeners("post-insert", new PostInsertEventListener[] {auditEventListener});
		baseConfiguration.setListeners("post-update", new PostUpdateEventListener[] {auditEventListener});
		baseConfiguration.setListeners("post-delete", new PostDeleteEventListener[] {auditEventListener});
		baseConfiguration.setListeners("pre-collection-update", new PreCollectionUpdateEventListener[] {auditEventListener});
		baseConfiguration.setListeners("pre-collection-remove", new PreCollectionRemoveEventListener[] {auditEventListener});
		baseConfiguration.setListeners("post-collection-recreate", new PostCollectionRecreateEventListener[] {auditEventListener});
		
		return baseConfiguration;
	}
	
	/**
	 * @return
	 */
	protected HibernateUtility getHibernateSession() {
		HibernateUtility hu = (HibernateUtility) getPersistenceManager().getPersistenceUtility().getDelegatePersistenceUtilityForClass(PersistentObjectPair1.class.getName());
		return hu;
	}
}
