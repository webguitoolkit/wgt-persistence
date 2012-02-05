package org.webguitoolkit.persistence.test.jpa;

import java.util.Collection;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.query.jpa.AllOfAKindQuery;
import org.webguitoolkit.persistence.util.JPAUtility;

public abstract class AbstractJPAModelTest<T extends IPersistable> extends BaseJPATest {

	protected abstract T create() throws Exception;

	protected abstract void compare(T source, T loaded) throws Exception;

	protected abstract void modify(T object) throws Exception;

	protected abstract Class<T> getObjectClass() throws Exception;

	public void testCreation() throws Exception {
		T source = create();

		getPersistenceManager().commit();

		getPersistenceManager().close();

		T loaded = loadObjectFromSession(source);

		compare(source, loaded);
	}

	public void testModification() throws Exception {
		T source = create();

		getPersistenceManager().commit();

		modify(source);

		getPersistenceManager().commit();

		getPersistenceManager().close();

		T loaded = loadObjectFromSession(source);
		compare(source, loaded);
	}

	/**
	 * @param source
	 * @return
	 * @throws Exception
	 */
//	@SuppressWarnings("unchecked")
	protected T loadObjectFromSession(T source) throws Exception {
		return ((JPAUtility)getPersistenceUtility()).getEntityManager().find(getObjectClass(), source.getId());
	}

	public void testDeletion() throws Exception {
		T source = create();

		getPersistenceManager().commit();

		source.markDeleted();

		getPersistenceManager().commit();

		getPersistenceManager().close();

		AllOfAKindQuery<T> aok = new AllOfAKindQuery<T>(getPersistenceManager(), getObjectClass());
		Collection<T> result = aok.execute();

		assertFalse( getObjectClass().getSimpleName() + " is not deleted!", result.contains(source));
	}


}
