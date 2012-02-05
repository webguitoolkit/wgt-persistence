/**
 * 
 */
package org.webguitoolkit.persistence.util.dto;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * A basic implementation of a generic object which stores all values as key-value pairs in a map. This generic object is typed to
 * a specific interface by the DTOModelFactory.
 * 
 * @author Wolfram Kaiser
 */
public class DTOProxy implements InvocationHandler {

	private Map<String, Object> valueMap;

	/**
	 * 
	 */
	public DTOProxy() {
		this(new HashMap<String, Object>());
	}

	public DTOProxy(Map<String, Object> newValueMap) {
		valueMap = newValueMap;
	}

	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result = null;
		String methodName = m.getName();
		if (methodName.startsWith("get")) {
			String name = methodName.substring(methodName.indexOf("get") + 3);
			result = valueMap.get(name);
			result = getDefaultValueForPrimitiveTypes(m, result);
		}
		else if (methodName.startsWith("set")) {
			String name = methodName.substring(methodName.indexOf("set") + 3);
			valueMap.put(name, args[0]);
		}
		else if (methodName.startsWith("is")) {
			String name = methodName.substring(methodName.indexOf("is") + 2);
			result = (valueMap.get(name));
			result = getDefaultValueForPrimitiveTypes(m, result);
		}
		else if (methodName.equals("equals")) {
			if (args.length == 1) {
				// proxy objects are equal if they are the same object
				result = new Boolean(proxy == args[0]);
			}
			else {
				result = Boolean.FALSE;
			}
		}
		else {
			result = m.invoke(this, args);
		}

		return result;
	}

	/**
	 * Primitive types must always have an default value
	 * 
	 * @param m
	 * @param result
	 * @return
	 */
	protected Object getDefaultValueForPrimitiveTypes(Method m, Object result) {
		if ((result == null) && m.getReturnType().isPrimitive()) {
			if (m.getReturnType() == java.lang.Boolean.TYPE) {
				result = Boolean.FALSE;
			}
			else if (m.getReturnType() == java.lang.Character.TYPE) {
				result = new Character('0');
			}
			else if (m.getReturnType() == java.lang.Byte.TYPE) {
				result = new Byte((byte)0);
			}
			else if (m.getReturnType() == java.lang.Short.TYPE) {
				result = new Short((short)0);
			}
			else if (m.getReturnType() == java.lang.Integer.TYPE) {
				result = new Integer(0);
			}
			else if (m.getReturnType() == java.lang.Long.TYPE) {
				result = new Long(0);
			}
			else if (m.getReturnType() == java.lang.Float.TYPE) {
				result = new Float(0f);
			}
			else if (m.getReturnType() == java.lang.Double.TYPE) {
				result = new Double(0d);
			}
		}
		return result;
	}

}
