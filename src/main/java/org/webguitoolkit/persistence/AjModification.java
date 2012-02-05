package org.webguitoolkit.persistence;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.RelationManagement;
import org.webguitoolkit.persistence.model.RelationManagement.Mode;
import org.webguitoolkit.persistence.model.RelationManagementHelper;

/**
 * When activating this code it has impact on all depended projects
 * 
 * @author i102415
 */
@Aspect
public abstract class AjModification extends AjPointcuts {

	/**
	 * 
	 * @param po
	 * @param value
	 */
	@Around("onAttributeChange(po, value)")
	public void cutLengthOnAttributeChange(ProceedingJoinPoint thisJoinPoint, Persistable po, Object value) throws Throwable {
		// TODO-MH document
		po.listModifications().add(thisJoinPoint.getSignature().getName() + " ( " + value + " )");

		String propertyName = thisJoinPoint.getSignature().getName();
		if (propertyName.startsWith("set")) {
			propertyName = propertyName.substring(3, 4).toLowerCase() + propertyName.substring(4);

			if (isTransient(po.getClass(), propertyName)) { // no mark changed
				thisJoinPoint.proceed(new Object[] { po, value });
				return;
			}

			// cut value length
			if (value instanceof String) {
				String val = (String)value;
				int length = po.getPropertyLength(propertyName);
				if (length > 0 && val.length() > length) {
					value = val.substring(0, length);
				}
			}
		}

		po.markModified(ModificationType.CHANGED);
		thisJoinPoint.proceed(new Object[] { po, value });
	}

	/**
	 * 
	 * @param po
	 * @param key
	 * @param value
	 */
	@Around("onPropertyAdd(po, key, value)")
	public void cutLengthOnPropertyAdd(ProceedingJoinPoint thisJoinPoint, Persistable po, String key, String value) throws Throwable {
		// TODO-MH document
		// cut value length
		String propertyName = thisJoinPoint.getSignature().getName();
		if (propertyName.startsWith("add")) {
			propertyName = propertyName.substring(3, 4).toLowerCase() + propertyName.substring(4);
			if (isTransient(po.getClass(), propertyName)) { // no mark changed
				thisJoinPoint.proceed(new Object[] { po, key, value });
				return;
			}

			if (value instanceof String) {
				if (propertyName.endsWith("Property"))
					propertyName = propertyName.replaceAll("Property", "Properties");

				int indexLength = po.getPropertyLength(propertyName + ".index");
				if (indexLength > 0 && key.length() > indexLength) {
					key = key.substring(0, indexLength);
				}
				int elementLength = po.getPropertyLength(propertyName + ".element");
				if (elementLength > 0 && value.length() > elementLength) {
					value = value.substring(0, elementLength);
				}
			}
		}

		po.markModified(ModificationType.CHANGED);
		thisJoinPoint.proceed(new Object[] { po, key, value });
	}

	/**
	 * Attach the relationship partner and do the relationship management
	 * 
	 * @param po
	 * @param partner
	 */
	@Before("onRelationChange(po, partner)")
	public void beforeRelationChange(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		attach(partner);
		manageRelation(po, partner, thisJoinPoint.getSignature());
	}

	/**
	 * Log the changes and handle markChanged()
	 * 
	 * @param po
	 * @param partner
	 */
	@After("onRelationChange(po, partner)")
	public void afterRelationChange(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		// create change log information
		po.listModifications().add(thisJoinPoint.getSignature().getName() + " ( " + partner + " )");
		if (!((MethodSignature)thisJoinPoint.getSignature()).getMethod().isAnnotationPresent(RelationManagement.class)) {
			po.markModified(ModificationType.CHANGED);
		}
	}

	/**
	 * Maintain the bidirectional relationship creation between two IPersistables. Attach the relationship partner and do the
	 * relationship management
	 * 
	 * @param po
	 * @param partner
	 */
	@Before("onRelationAdd(po, partner)")
	public void beforeRelationAdd(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		attach(partner);
		manageRelation(po, partner, thisJoinPoint.getSignature());
	}

	/**
	 * After the maintenance of the relationship creation between two IPersistables mark as changed.
	 * 
	 * @param po
	 * @param partner
	 */
	@After("onRelationAdd(po, partner)")
	public void afterRelationAdd(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		// create change log information
		po.listModifications().add(thisJoinPoint.getSignature().getName() + " ( " + partner + " )");
		if (!((MethodSignature)thisJoinPoint.getSignature()).getMethod().isAnnotationPresent(RelationManagement.class)) {
			po.markModified(ModificationType.CHANGED);
		}
	}

	/**
	 * Maintain the bidirectional relationship removal between two IPersistables. Attach the relationship partner and do the
	 * relationship management
	 * 
	 * @param po
	 * @param partner
	 */
	@Before("onRelationRemove(po, partner)")
	public void beforeRelationRemove(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		attach(partner);
		manageRelation(po, partner, thisJoinPoint.getSignature());
	}

	/**
	 * After the maintenance of the relationship removal between two IPersistables mark as changed and do logging.
	 * 
	 * @param po
	 * @param partner
	 */
	@After("onRelationRemove(po, partner)")
	public void afterRelationRemove(JoinPoint thisJoinPoint, Persistable po, IPersistable partner) {
		// create change log information
		po.listModifications().add(thisJoinPoint.getSignature().getName() + " ( " + partner + " )");
		if (!((MethodSignature)thisJoinPoint.getSignature()).getMethod().isAnnotationPresent(RelationManagement.class)) {
			po.markModified(ModificationType.CHANGED);
		}
	}

	/**
	 * Attach to persistable to the current session.
	 * 
	 * @param po
	 */
	public void attach(IPersistable po) {
		if (po == null) {
			return;
		}
		// if the aspect is called while no PersistenceManager is initialized then no attachment should occur
		getPersistenceManager().getPersistenceUtility().attach(po);

	}

	/**
	 * Depending on the definition do the Relationship Management by means of the Relatable functionality.
	 * 
	 * @param po
	 * @param partner
	 * @param sig
	 */
	public void manageRelation(Persistable po, IPersistable partner, Signature sig) {
		if (po == null) {
			return;
		}
		String methodName = sig.getName();
		try {
			Method m = po.getClass().getMethod(methodName, getClassesFromSignature(sig));
			if (m.isAnnotationPresent(RelationManagement.class)) {
				RelationManagement rel = m.getAnnotation(RelationManagement.class);
				
				Mode relationManagementMode = getPersistenceManager().getRelationManagementMode();
				if( !( rel.mode() == Mode.MANUEL && relationManagementMode == Mode.MANUEL ) ){
					// get the two field names from the Annotation
					RelationManagementHelper.manageRelation(po, rel.thisSide(), partner, rel.relationSide(), methodName);
				}
				
				
			}
		}
		catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Error managing relation" + e);
		}
	}

	/**
	 * 
	 * @param sig
	 */
	protected Class<?>[] getClassesFromSignature(Signature sig) {
		String longString = sig.toLongString();
		String params = longString.substring(longString.indexOf('(') + 1, longString.indexOf(')'));
		StringTokenizer str = new StringTokenizer(params, ",");
		Class<?>[] result = new Class[str.countTokens()];
		for (int i = 0; str.hasMoreElements();) {
			String token = (String)str.nextElement();
			try {
				result[i] = Class.forName(token);
			}
			catch (ClassNotFoundException e) {
				Logger.getLogger(this.getClass()).error("Error resolving class" + e);
			}
		}
		return result;
	}

	/**
	 * Shortcut to PM
	 * 
	 * @param po
	 */
	public PersistenceManager getPersistenceManager() {
		return PersistenceContext.getPersistenceManager();
	}

	protected boolean isTransient(Class theClass, String property) {
		boolean isTransient = true;
		try {
			isTransient = theClass.getDeclaredField(property).isAnnotationPresent(Transient.class);
		}
		catch (SecurityException e) {
			Logger.getLogger(theClass).error("Exception when accessing field: " + property, e);
		}
		catch (NoSuchFieldException e) {
			if( theClass.equals( Object.class ) )
				return false;
			return isTransient( theClass.getSuperclass(), property );
		}
		return isTransient;
	}
}
