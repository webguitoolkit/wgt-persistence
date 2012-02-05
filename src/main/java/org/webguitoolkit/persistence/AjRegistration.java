package org.webguitoolkit.persistence;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.FieldSignature;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.model.RelationManagement;
import org.webguitoolkit.persistence.model.util.IPersistentWrapper;
import org.webguitoolkit.persistence.model.util.ObserverHelper;
import org.webguitoolkit.persistence.model.util.PersistentSet;
import org.webguitoolkit.persistence.model.util.WrapWithObserver;

/**
 * When activating this code it has impact on all depended projects
 * 
 * @author i102415
 */

@Aspect
public abstract class AjRegistration extends AjPointcuts {

	public static int callcount = 0;
	/**
	 * when the constructor is called
	 */
	@After("onCreation()")
	public void registerAfterCreation(JoinPoint thisJoinPoint) {
		Persistable me = (Persistable)thisJoinPoint.getThis();
		PersistenceManager pm = getPersistenceManager(me);
		if (pm == null) {
			throw new RuntimeException("PersistenceManager must be set in context");
		}
		me.markModified(ModificationType.NEW);
	}

	/**
	 * Attach if setXXX() is called.
	 * 
	 * @param po
	 */
	@Before("onSetterCall( po )")
	public void attachBeforeSetterCall(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Attach if addXXX() is called.
	 * 
	 * @param po
	 */
	@Before("onAddCall( po )")
	public void attachBeforeAddCall(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Attach if removeXXX() is called.
	 * 
	 * @param po
	 */
	@Before("onRemoveCall( po )")
	public void attachRemoveCall(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Attach if a relationship is managed.
	 * 
	 * @param po
	 * @param from
	 * @param partner
	 * @param to
	 */
	@Before("onRelation( po, from, partner,  to )")
	public void attachBeforeAddCall(JoinPoint thisJoinPoint, Persistable po, String from, Persistable partner, String to) {
		// TODO-PZ : Probably not needed anymore if we use the new style with annotations ??
		if (partner instanceof Persistable) {
			attach(partner);
		}
		if (po instanceof Persistable) {
			attach(po);
		}
	}

	/**
	 * Attach if a IPersistable is read.
	 * 
	 * @param po
	 */
	@Before("onPersistableGet( po )")
	public void attachBeforePersistableGet(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Attach if a collection of IPersistable is read.
	 * 
	 * @param po
	 */
	@Before("onCollectionGet( po )")
	public void attachBeforeCollectionGet(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	
	@Around("onCollectionSet( input )")
	public void unWrapCollectionOnSet(ProceedingJoinPoint thisJoinPoint, Collection input) throws Throwable{
		callcount++;
		IPersistable po = (IPersistable)thisJoinPoint.getTarget();
		if( input instanceof IPersistentWrapper<?>)
			thisJoinPoint.proceed( new Object[]{ ((IPersistentWrapper<?>)input).getDelegate() } );
		else
			thisJoinPoint.proceed( new Object[]{ input } );
	}
	
	/**
	 * Attach if a collection of IPersistable is read.
	 * 
	 * @param po
	 */
	@Around("onCollectionGet( po )")
	public Object wrapCollectionOnGet(ProceedingJoinPoint thisJoinPoint, Persistable po) throws Throwable{
		
		Object o = thisJoinPoint.proceed();
		Signature signature = thisJoinPoint.getSignature();
        if(signature instanceof FieldSignature ){
        	FieldSignature methodSignature = (FieldSignature) signature;
        	Field field = methodSignature.getField();
        	
        	if( field.isAnnotationPresent( WrapWithObserver.class ) ){
        		WrapWithObserver annotation = field.getAnnotation( WrapWithObserver.class );
        		if(field.isAnnotationPresent( RelationManagement.class ) ){
        			RelationManagement relationManagement = field.getAnnotation( RelationManagement.class );
        			if(  o instanceof Set<?> && !(o instanceof PersistentSet<?> ) ){
        				ObserverHelper observerHelper = new ObserverHelper();
        				o = observerHelper.wrapWithObserver(po, (Set<?>)o, relationManagement.thisSide(), relationManagement.relationSide() );
        			}
        		}
        		else{
        			if(  o instanceof Set<?> && !(o instanceof PersistentSet<?> ) ){
        				ObserverHelper observerHelper = new ObserverHelper();
        				o = observerHelper.wrapWithObserver(po, (Set<?>)o );
        			}
        		}
        	}
        }

        return o;
	} 

	/**
	 * Attach if a map of IPersistable is read.
	 * 
	 * @param po
	 */
	@Before("onMapGet( po )")
	public void attachBeforeMapGet(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Attach if a IPersistable is deleted.
	 * 
	 * @param po
	 */
	@Before("markDeletedCall( po )")
	public void attachBeforeMarkDeletedCall(JoinPoint thisJoinPoint, Persistable po) {
		attach(po);
	}

	/**
	 * Do the attachment if PersistenceManager is available.
	 * 
	 * @param po
	 */
	public void attach(Persistable po) {
		if (po == null) {
			return;
		}
		// if the aspect is called while no PersistenceManager is initialized then no registration should occur
		if (getPersistenceManager(po) != null) {
			getPersistenceManager(po).getPersistenceUtility().attach(po);
		}
	}

	/**
	 * Get the PersistenceManager from the PersistenceContext
	 * 
	 * @param po
	 */
	public PersistenceManager getPersistenceManager(Persistable po) {
		return PersistenceContext.getPersistenceManager();
	}

}
