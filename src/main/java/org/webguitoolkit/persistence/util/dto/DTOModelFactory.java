/**
 * 
 */
package org.webguitoolkit.persistence.util.dto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.webguitoolkit.persistence.model.IPersistable;


/**
 * The DTOModelFactory can be used like other ModelFactories to create instances of a certain type (interfaces).
 * The difference is that this ModelFactory creates non-persistent instance which can be used as
 * data transfer objects (DTOs). The types of these DTO instances are created on the fly by using
 * Java dynamic proxy feature. Thus, the newInstance() method expects a set of interfaces for which
 * a dynamic class is created. This dynamic class just stores the values in a map.
 * <br>
 * Typically, the DTOModelFactory is subclassed and implements a specific ModelFactory interface
 * such as IUserModelFactory. The subclass then implements the newXYZ() factory methods and invokes
 * the newInstace() method like this:
 * 
 * <code>
 * public IAddress newAddress(String city) {
 * 	 IAddress newAddress = (IAddress) newInstance(new Class[] {IAddress.class});
 *   // do additional initialization stuff if necessary
 *   return newAddress;
 * }
 * </code>
 * <br>
 * The DTOModelFactory provides convenience methods for copying (public) values from a real model
 * object to a DTO object and vice versa.
 * <br>
 * Note: It should be evaluated whether the BeanUtils provide a better way to copy values to and from
 * real model objects and DTO objects.
 * 
 * @author Wolfram Kaiser
 */
public abstract class DTOModelFactory {

	/**
	 * 
	 */
	public DTOModelFactory() {
		super();
	}
	
	/**
	 * Whereas the newModelObject() method from the superclass works with classes (which can be
	 * instantiated) this method works with interfaces.
	 * 
	 * @param <T>
	 * @param requestedInterface
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends IPersistable> T newInstance(Class<T> requestedInterface) {
		return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {requestedInterface}, new DTOProxy());
	}

	@SuppressWarnings("unchecked")
	protected <T extends IPersistable> T newInstance(Class<? extends IPersistable>[] requestedInterfaces) {
		return (T)Proxy.newProxyInstance(getClass().getClassLoader(), requestedInterfaces, new DTOProxy());
	}


	public void copyViaReflection(Object source, Object target) {
		Method[] sourceMethods = source.getClass().getMethods();
		for (int methodCounter = 0; methodCounter < sourceMethods.length; methodCounter++) {
			Method getterMethod = sourceMethods[methodCounter];
			if (getterMethod.getParameterTypes().length == 0) {
				Method setterMethod = findCorrespondingSetterMethod(getterMethod, target.getClass());
				if (setterMethod != null) {
					try {
						Object copyValue = getterMethod.invoke(source, (Object[]) null);
						setterMethod.invoke(target, new Object[] {copyValue});
					}
					catch (IllegalArgumentException e) {
						// ignore and continue with next value
					}
					catch (IllegalAccessException e) {
						// ignore and continue with next value
					}
					catch (InvocationTargetException e) {
						// ignore and continue with next value
					}
				}				
			}
		}
	}
	
	/**
	 * Copy only primitive values and String values from the source object to the target object.
	 * Only those values/properties are copied for which a getter method in the source object and
	 * a setter method in the target object exists.
	 * 
	 * @param source
	 * @param target
	 */
	public void copyValues(Object source, Object target) {
		Method[] sourceMethods = source.getClass().getMethods();
		for (int methodCounter = 0; methodCounter < sourceMethods.length; methodCounter++) {
			Method getterMethod = sourceMethods[methodCounter];
			if ((getterMethod.getParameterTypes().length == 0) && 
					(getterMethod.getReturnType().isPrimitive()) || (getterMethod.getReturnType() == String.class)) {
				Method setterMethod = findCorrespondingSetterMethod(getterMethod, target.getClass());
				if (setterMethod != null) {
					try {
						Object copyValue = getterMethod.invoke(source, (Object[]) null);
						setterMethod.invoke(target, new Object[] {copyValue});
					}
					catch (IllegalArgumentException e) {
						// ignore and continue with next value
					}
					catch (IllegalAccessException e) {
						// ignore and continue with next value
					}
					catch (InvocationTargetException e) {
						// ignore and continue with next value
					}
				}
			}
		}
	}
	
	protected Method findCorrespondingSetterMethod(Method checkMethod, Class<?> targetClass) {
		Method foundSetter = null;
		
		String methodName = checkMethod.getName();
		if (methodName.startsWith("get")) {
			Class<?> returnType = checkMethod.getReturnType();
			String setterMethodName = "set" + methodName.substring(methodName.indexOf("get") + 3);
			try {
				foundSetter = targetClass.getMethod(setterMethodName, new Class[] {returnType});
			}
			catch (SecurityException e) {
				// ignore - no public setter found
			}
			catch (NoSuchMethodException e) {
				// ignore - no public setter found
			}
		}
		
		return foundSetter;
	}
}
