/*
 * (c) 2006, Endress+Hauser InfoServe GmbH & Co. KG
 */
package org.webguitoolkit.persistence.model;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.util.HibernateUtility;
import org.webguitoolkit.persistence.util.IPersistenceUtility;


/**
 * This class is intended to support the developer managing transient relationships between Java instances. It provides methode to
 * maintain the following relationships: <br>
 * one-to-one one-to-many many-to-many <br>
 * Prerequisites to use this features: <br>
 * The relationship is defined as member directly in this class - not in a superclass. <br>
 * The correct names are passed as string with the call. <br>
 * No SecurityManager is configured that restricts the access to Field members.
 * 
 * @author Peter
 */
public class RelationManagementHelper {

	private static Field getDeclaredField(Class<?> clazz, String field) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(field);
		}
		catch (NoSuchFieldException fe) {
			if (clazz.getSuperclass().equals(Object.class)) {
				throw fe;
			}
			return getDeclaredField(clazz.getSuperclass(), field);
		}
	}

	public static void manageRelation(Object me, String myFieldName, Object partner, String partnerFieldName, String methodName) {
		try {
			Field myField = getDeclaredField(me.getClass(), myFieldName);
			myField.setAccessible(true);
			me = getWithoutProxy(me);

			// REMOVE ME FROM MY OLD RELATED OBJECT
			Object myOldValue = myField.get(me);
			if (myOldValue != null) {
				attach(myOldValue);

				// ONE TO ONE and MANY TO ONE are affected, in 
				// collections there has nothing to be removed on relation changes

				if (myField.isAnnotationPresent(javax.persistence.OneToOne.class))
					setField(myOldValue, null, partnerFieldName);

				if (myField.isAnnotationPresent(javax.persistence.ManyToOne.class))
					removeFromCollection(myOldValue, me, partnerFieldName);
			}

			Field partnerField = null;
			if (partner != null) {
				partner = getWithoutProxy(partner);
				partnerField = getDeclaredField(partner.getClass(), partnerFieldName);
				partnerField.setAccessible(true);

				// REMOVE PARTNER FROM IS OLD RELATED OBJECT
				Object partnerOldValue = partnerField.get(partner);
				if (partnerOldValue != null) {
					attach(partnerOldValue);

					if (partnerField.isAnnotationPresent(javax.persistence.OneToOne.class))
						setField(partnerOldValue, null, myFieldName);

					// the partner must be moved to a different collection if we add it
					// to a new, therefore we have to remove it from the old one
					if (partnerField.isAnnotationPresent(javax.persistence.ManyToOne.class) && methodName.startsWith("add"))
						removeFromCollection(partnerOldValue, partner, myFieldName);
				}
			}

			// CREATE NEW RELATION

			// RELATION FOR COLLECTION (ONE TO MANY, MANY TO MANY)
			if (myField.isAnnotationPresent(javax.persistence.OneToMany.class)
					|| myField.isAnnotationPresent(javax.persistence.ManyToMany.class)) {
				if (methodName.startsWith("add")) {
					// addToCollection(me, partner, myField);
					if (myField.isAnnotationPresent(javax.persistence.OneToMany.class))
						setField(partner, me, partnerField);
					else
						addToCollection(partner, me, partnerField);
				}
				else if (methodName.startsWith("remove")) {
					// removeFromCollection(me, partner, myField);
					if (myField.isAnnotationPresent(javax.persistence.OneToMany.class))
						setField(partner, null, partnerField);
					else
						removeFromCollection(partner, me, partnerField);
				}
			}

			// RELATION FOR ONE TO ONE
			if (myField.isAnnotationPresent(javax.persistence.OneToOne.class)) {
				// setField(me, partner, myField);
				setField(partner, me, partnerField);
			}

			// RELATION FOR MANY TO ONE TO ONE
			if (myField.isAnnotationPresent(javax.persistence.ManyToOne.class)) {
				// setField(me, partner, myField);
				addToCollection(partner, me, partnerField);
			}

			// DO THE MARK CHANGED
			if (partnerField != null && checkIfMarkChange(partnerField))
				markChanged(partner);
			if (checkIfMarkChange(myField))
				markChanged(me);
		}
		catch (NoSuchFieldException fe) {
			Logger.getLogger(RelationManagementHelper.class).error("Wrong configuration of RelationManagement Annotation", fe);
			throw new RuntimeException(fe);
		}
		catch (IllegalAccessException e) {
			Logger.getLogger(RelationManagementHelper.class).error("Illegal Access", e);
			throw new RuntimeException(e);
		}
	}

	private static void setField(Object object, Object toSet, String fieldName) {
		if (object == null || fieldName == null)
			return;
		try {
			Field field = getDeclaredField(object.getClass(), fieldName);
			field.setAccessible(true);
			// if other side has a collection one2many or many2many
			setField(object, toSet, field);
		}
		catch (NoSuchFieldException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Wrong configuration of RelationManagement Annotation", ex);
			throw new RuntimeException(ex);
		}
	}

	private static void setField(Object object, Object toSet, Field field) {
		if (object == null || field == null)
			return;
		try {
			object = getWithoutProxy(object);
			field.set(object, toSet);
			if (checkIfMarkChange(field))
				markChanged(object);
		}
		catch (IllegalAccessException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Illegal Access", ex);
			throw new RuntimeException(ex);
		}
	}

	private static void removeFromCollection(Object object, Object toRemove, String fieldName) {
		if (object == null || toRemove == null || fieldName == null)
			return;
		try {
			Field field = getDeclaredField(object.getClass(), fieldName);
			field.setAccessible(true);
			removeFromCollection(object, toRemove, field);
		}
		catch (NoSuchFieldException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Wrong configuration of RelationManagement Annotation", ex);
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private static void removeFromCollection(Object object, Object toRemove, Field field) {
		if (object == null || toRemove == null || field == null)
			return;
		try {
			Class<?> fieldClass = field.getType();
			// if other side has a collection one2many or many2many
			if (Collection.class.isAssignableFrom(fieldClass)) {
				object = getWithoutProxy(object);
				Collection<IPersistable> coll = (Collection<IPersistable>)field.get(object);
				coll.remove(toRemove);
			}
		}
		catch (IllegalAccessException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Illegal Access", ex);
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unused")
	private static void addToCollection(Object object, Object toAdd, String fieldName) {
		if (object == null || toAdd == null || fieldName == null)
			return;
		try {
			Field field = getDeclaredField(object.getClass(), fieldName);
			field.setAccessible(true);
			addToCollection(object, toAdd, field);
		}
		catch (NoSuchFieldException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Wrong configuration of RelationManagement Annotation", ex);
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private static void addToCollection(Object object, Object toAdd, Field field) {
		if (object == null || toAdd == null || field == null)
			return;
		try {
			Class<?> fieldClass = field.getType();
			// if other side has a collection one2many or many2many
			if (Collection.class.isAssignableFrom(fieldClass)) {
				object = getWithoutProxy(object);
				Collection<IPersistable> coll = (Collection<IPersistable>)field.get(object);
				coll.add((IPersistable)toAdd);
			}
		}
		catch (IllegalAccessException ex) {
			Logger.getLogger(RelationManagementHelper.class).error("Illegal Access", ex);
			throw new RuntimeException(ex);
		}
	}

	private static Object getWithoutProxy(Object object) {
		if (object instanceof HibernateProxy) {
			object = ((HibernateProxy)object).getHibernateLazyInitializer().getImplementation();
		}
		return object;
	}

	private static void attach(Object o) {
		if (o instanceof IPersistable) {
			IPersistenceUtility pu = PersistenceContext.getPersistenceManager().getPersistenceUtility().getDelegatePersistenceUtilityForClass(
					o.getClass().getName());
			if (pu instanceof HibernateUtility) {
				if (!((HibernateUtility)pu).getSession().contains(o)) {
					pu.attach((IPersistable)o);
				}
			}
		}
	}

	private static boolean checkIfMarkChange(Field field) {
		if (field.isAnnotationPresent(OneToMany.class)) {
			OneToMany o2m = field.getAnnotation(OneToMany.class);
			if (StringUtils.isNotEmpty(o2m.mappedBy())) {
				return false;
			}
		}
		else if (field.isAnnotationPresent(ManyToMany.class)) {
			ManyToMany m2m = field.getAnnotation(ManyToMany.class);
			if (StringUtils.isNotEmpty(m2m.mappedBy())) {
				return false;
			}
		}
		else if (field.isAnnotationPresent(OneToOne.class)) {
			OneToOne o2o = field.getAnnotation(OneToOne.class);
			if (StringUtils.isNotEmpty(o2o.mappedBy())) {
				return false;
			}
		}
		// else if (field.isAnnotationPresent(ManyToOne.class)) {
		// ManyToOne m2o = field.getAnnotation(ManyToOne.class);
		// }
		return true;
	}

	/**
	 * Default implementation just calls markChanged()
	 */
	public static void markChanged(Object o) {
		if (o instanceof IPersistable) {
			((IPersistable)o).markModified( ModificationType.CHANGED );
		}
	}
}
