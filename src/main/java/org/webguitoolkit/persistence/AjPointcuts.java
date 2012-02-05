package org.webguitoolkit.persistence;

import java.util.Collection;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.Pointcut;
import org.webguitoolkit.persistence.model.IPersistable;

/**
 * When activating this code it has impact on all depended projects
 * 
 * @author i102415
 */
@Aspect
public abstract class AjPointcuts {

	/*
	 * Creation observation
	 */
	@Pointcut("execution ( public org.webguitoolkit.persistence.Persistable+.new(..) )")
	void onCreation() {
	}

	/*
	 * Modification observation : setters for simple objects
	 */
	@Pointcut("execution (public void org.webguitoolkit.persistence.Persistable+.set*(!(org.webguitoolkit.persistence.model.IPersistable+)) ) && target(po) && args (value)")
	void onAttributeChange(Persistable po, Object value) {
	}

	/*
	 * Modification observation : add property for simple objects
	 */
	@Pointcut("execution (public void org.webguitoolkit.persistence.Persistable+.add*( String, String ) ) && target(po) && args (key, value)")
	void onPropertyAdd(Persistable po, String key, String value) {
	}

	/*
	 * Modification observation : setters for navigated objects
	 */
	@Pointcut("execution (public void org.webguitoolkit.persistence.Persistable+.set*(org.webguitoolkit.persistence.model.IPersistable+) ) && target(po) && args (partner)")
	void onRelationChange(Persistable po, IPersistable partner) {
	}

	/*
	 * catch all adders (IPersistable)
	 */
	@Pointcut("execution(public * org.webguitoolkit.persistence.Persistable+.add*(org.webguitoolkit.persistence.model.IPersistable+) ) && target(po) && args(partner)")
	void onRelationAdd(Persistable po, IPersistable partner) {
	}

	/*
	 * catch all removers (IPersistable)
	 */
	@Pointcut("execution (public * org.webguitoolkit.persistence.Persistable+.remove*(org.webguitoolkit.persistence.model.IPersistable+) ) && target(po) && args(partner)")
	void onRelationRemove(Persistable po, IPersistable partner) {
	}

	@Pointcut("execution ( public * org.webguitoolkit.persistence.Persistable+.set*( .. )  ) && target(po)")
	void onSetterCall(Persistable po) {
	}

	@Pointcut("execution ( public * org.webguitoolkit.persistence.Persistable+.add*( .. )  ) && target(po)")
	void onAddCall(Persistable po) {
	}

	@Pointcut("execution ( public * org.webguitoolkit.persistence.Persistable+.remove*( .. )  ) && target(po)")
	void onRemoveCall(Persistable po) {
	}

	@Pointcut("execution ( public void org.webguitoolkit.persistence.Persistable+.markDeleted()  ) && target(po)")
	void markDeletedCall(Persistable po) {
	}

	@Pointcut("call ( protected void org.webguitoolkit.persistence.Persistable.*( String, org.webguitoolkit.persistence.Persistable, String )  ) && target(po) && args(from, partner, to)")
	void onRelation(Persistable po, String from, Persistable partner, String to) {
	}

	@Pointcut("get( org.webguitoolkit.persistence.Persistable+ org.webguitoolkit.persistence.Persistable+.*  ) && target( po )")
	void onPersistableGet(Persistable po) {
	}

	@Pointcut("get( java.util.Collection+ org.webguitoolkit.persistence.Persistable+.*  ) && target( po )")
	void onCollectionGet(Persistable po) {
	}

	@Pointcut("set( java.util.Collection+ org.webguitoolkit.persistence.Persistable+.*  ) && args(input)")
	void onCollectionSet( Collection input ) {
	}

	@Pointcut("get (  java.util.Map+ org.webguitoolkit.persistence.Persistable+.*  ) && target( po )")
	void onMapGet(Persistable po) {
	}

	// pointcuts for listeners

	// pointcut onPostLoad(PostLoadEvent event): execution (public void HibernateEventListener.onPostLoad(PostLoadEvent))&&
	// args(event);
	//
	// pointcut onPostDelete(PostDeleteEvent event): execution (public void
	// HibernateEventListener.onPostDelete(PostDeleteEvent))&& args(event);

	@DeclareError(value = "preinitialization ( public org.webguitoolkit.persistence.Persistable+.new() )")
	public static final String errorPublicConstructorWithoutParameter = "do not define public constructors without parameter";

}
