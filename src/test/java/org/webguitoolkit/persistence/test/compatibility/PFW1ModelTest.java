package org.webguitoolkit.persistence.test.compatibility;

import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.test.AbstractModelTest;

public class PFW1ModelTest extends AbstractModelTest<PFW1Object> {

	@Override
	protected PFW1Object create() throws Exception {
		return new PFW1Object("initial");
	}

	@Override
	protected void compare(PFW1Object source, PFW1Object loaded) throws Exception {
		assertEquals("text", source.getText(), loaded.getText() );
	}

	@Override
	protected void modify(PFW1Object object) throws Exception {
		object.setText("modified");
	}

	@Override
	protected Class<PFW1Object> getObjectClass() throws Exception {
		return PFW1Object.class;
	}

	@Override
	protected Class<IPersistable>[] getAnnotatedClasses() {
		return new Class[]{PFW1Object.class};
	}

	protected String getHibernateConfigId() {
		return "pfw1Config";
	}

}
