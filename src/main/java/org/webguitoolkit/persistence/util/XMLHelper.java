package org.webguitoolkit.persistence.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.webguitoolkit.persistence.Persistable;
import org.webguitoolkit.persistence.PersistenceContext;
import org.webguitoolkit.persistence.PersistenceManager;
import org.webguitoolkit.persistence.model.IPersistable;
import org.webguitoolkit.persistence.model.IPersistable.ModificationType;
import org.webguitoolkit.persistence.model.compatibility.PersistablePFW2;


/**
 * The XMLHelper is intended to serialize and deserialize IPersistable instances
 * to XML. The used XML Schema is quite simple and consist of only 6 Tags. <br>
 * The idea is that you pass one IPersistable to the getXmlDocument() method and
 * the function "crawls" thru the tree of dependent instances to serialize them
 * all (if recursive=true). <br>
 * <br>
 * The algorithm for serialization works as follows:<br>
 * 1) Find all dependend IPersistables starting for the passed instance. That
 * will be done by means of Java reflection. Each class in the object tree will
 * be analyzed for fields of the type IPersistable. The current value will be
 * added to the list of instances to be serialized. each of these instances will
 * result in a &lt,object&gt; XML element later on. <br>
 * 2) Find all attributes of the instances by means of reflection. Threfore the
 * list of fields will be analyzed. each field "xxx" that has a getter method
 * named geXxxx() will be taken into account for serialization, assuming that
 * persistent attributes have to have a getter method. 3) No Ipersistable
 * references to Java instances are serialized as well but not as separate
 * &lt;object&gt; elements but as "inline xml". <br>
 * 3) For each xml element the name of the attribute and the Java type is
 * serialized as attibutes. <br>
 * 4) The serialization stops before Object.class and
 * PersistentModelObject.class. That means that the createdAt, changeBy, etc
 * attributes are not part of the serialized content. <br>
 * 5) The references to other IPersistables (references and relationships) are
 * serialized by using the getKey() method. That implies the uniquness of getKey
 * for all instances. The result of getKey() will be treated as identifier in
 * the &lt,object&gt; element.<br>
 * <br>
 * The algorithm for de-serialization works as follows:<br>
 * 
 * <br>
 * <br>
 * Prerequisites for using this helper:<br> - Naming convention for attributes.
 * <br>
 * The field aaa has to have a getter named getAaa() <br>
 * Unique getKey() result<br>
 * Usage of a ModelFactory located in the same package as the implementing model
 * classes<br>
 * Follow the Java bean conventions. getter and setter for all persistent
 * attributes including collections of references and relationships<br>
 * overwrite the newModelObject(String) method in your Model factory to enable
 * dynamic creation of model instances for de-serialization.
 * 
 * <pre>
 * public Object newModelObject(String className) throws InstantiationException, IllegalAccessException,
 * 		ClassNotFoundException {
 * 	return (IPersistable) Class.forName(className).newInstance();
 * }
 * </pre>
 * 
 * Supported Datatypes:<br>
 * Types supported by org.apache.commons.beanutils.BeanUtils <br>
 * java.sql.Date<br>
 * java.sql.Timestamp<br>
 * java.util.Date<br>
 * <br>
 * Know issues:<br>
 * PZ : Handling of TimeZones <br>
 * PZ (12.12.) : arrays of native types (e.g. int)
 * 
 * PZ (12.12.) : NEED TO REWRITE THIS CODE -- 8(
 * 
 * @author peter.zaretzke@infoserve.endress.com
 * 
 */
public class XMLHelper {
	private static final String VALUE = "value";
	private static final String KEY = "key";
	private static final String ENTRY = "entry";
	private static final String ARRAY = "array";
	private static final String MAP = "map";
	private static final String DOMAIN_HIBERNATE = "org.hibernate.";
	private static final String DOMAIN_ENDRESS = "com.endress.";
	private static final String REF = "ref";
	private static final String ID = "id";
	private static final String OBJECTSET = "objectset";
	private static final String OBJECT = "object";
	private static final String COLLECTION = "collection";
	private static final String NAME = "name";
	private static final String CLASS = "class";
	private static final String ITEM = "item";
	private static final String FIELD = "field";
	private static Log log = LogFactory.getLog(XMLHelper.class);
	private static ThreadLocal<Map<String, ?>> objects = new ThreadLocal<Map<String, ?>>();

	/**
	 * Get an XML document for this IPersistable
	 * 
	 * @param po
	 *            the object to be serialized
	 * @return a Jdom document containing the XML
	 */
	public static Document getXmlDocument(IPersistable po, boolean recursive) {
		Document xmldoc = new Document();
		xmldoc.setContent(getXML(po, recursive));
		return xmldoc;
	}

	/**
	 * Create a XML Element hierachy from the given IPersistable
	 * 
	 * @param po
	 * @param recursive
	 *            if true : follow references, if false serialize just this
	 *            object
	 * @return
	 */
	public static Element getXML(IPersistable po, boolean recursive) {
		if (!recursive)
			return getXMLElement(OBJECT, po, null);
		else {
			// get the object tree
			Map<Object, IPersistable> cache = new HashMap<Object, IPersistable>();
			traverseObjectTree(po, cache);
			// serialize the object tree
			Element result = new Element(OBJECTSET);
			result.setAttribute("anchor", po.getKey().toString());
			String className = getClassName(po);
			result.setAttribute(CLASS, className);
			for (Iterator<IPersistable> it = cache.values().iterator(); it.hasNext();) {
				IPersistable p = it.next();
				String classname = getClassName(p);
				classname = classname.substring(classname.lastIndexOf('.') + 1);
				result.addContent(getXMLElement(OBJECT, p, null));
			}
			return result;
		}
	}

	/**
	 * Clean-up the class name
	 * 
	 * @param o
	 * @return
	 */
	private static String getClassName(Object o) {
		String className = o.getClass().getName();
		int pos = className.indexOf('$');
		if (pos != -1) {
			className = className.substring(0, pos);
		}
		return className;
	}

	/**
	 * Find recursively all IPersistables that are relevant for serialization.
	 * 
	 * @param po
	 * @param cache
	 */
	private static void traverseObjectTree(IPersistable po, Map<Object, IPersistable> cache) {
		List<IPersistable> persitables = getPersistableLinks(po);
		for (Iterator<IPersistable> it = persitables.iterator(); it.hasNext();) {
			IPersistable p = it.next();
			if (cache.put(p.getKey(), p) == null) {
				traverseObjectTree(p, cache);
			}
		}
	}

	/**
	 * Calculate the set of instances to be serialized by traversing the tree
	 * and collecting IPersistable links. Only the links that have a getter
	 * following the Bean naming convention are taken into account.
	 * 
	 * @param po
	 * @return
	 */
	private static List<IPersistable> getPersistableLinks(IPersistable po) {
		Class<?> c = po.getClass();
		List<IPersistable> result = new ArrayList<IPersistable>();
		result.add(po);
		while (!(c.equals(PersistablePFW2.class) || c.equals(Object.class))) {
			Field[] declaredFields = c.getDeclaredFields();
			for (int i = 0; i < declaredFields.length; i++) {
				String name = declaredFields[i].getName();
				if (name.indexOf('$') != -1)
					continue;
				try {
					Object oo;
					try {
						oo = PropertyUtils.getProperty(po, name);
					} catch (NoSuchMethodException e1) {
						// OK! no getter - no persistence
						oo = null;
					}

					if (oo instanceof IPersistable) {
						result.add((IPersistable) oo);
					} else {
						if (oo instanceof Collection) {
							try {
								Collection<?> col = (Collection<?>) oo;
								for (Iterator<?> it = col.iterator(); it.hasNext();) {
									Object o = (Object) it.next();
									if (o instanceof IPersistable) {
										result.add((IPersistable) o);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							if (oo instanceof Map) {
								Map<?, ?> map = (Map<?, ?>) oo;
								for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
									Map.Entry<?, ?> entry = (Entry<?, ?>) it.next();
									if (entry.getKey() instanceof IPersistable) {
										result.add((IPersistable) entry.getKey());
									}
									if (entry.getValue() instanceof IPersistable) {
										result.add((IPersistable) entry.getValue());
									}
								}
							} else {
								// check if array
								if (oo != null && oo.getClass().isArray()) {
									Class<?> clazz = oo.getClass();
									Class<?> dataType = clazz.getComponentType();
									if (dataType.isAssignableFrom(IPersistable.class)) {
										for (int j = 0; j < Array.getLength(oo); j++) {
											IPersistable p = (IPersistable) Array.get(oo, j);
											result.add(p);
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			c = c.getSuperclass();
		}
		return result;
	}

	/**
	 * Here is the XML created. Drills down recursively all non IPersistable
	 * objects.
	 * 
	 * @param tag
	 * @param po
	 * @param name
	 * @return
	 */
	private static Element getXMLElement(String tag, Object po, String name) {

		Element result = new Element(tag);
		if (name != null)
			result.setAttribute(NAME, name);

		if (po == null)
			return result;

		result.setAttribute(CLASS, getClassName(po));
		if (tag.equals(OBJECT) && po instanceof IPersistable)
			result.setAttribute(ID, ((IPersistable) po).getKey().toString());
		try {
			Map<String, Class<?>> persistentFieldName = getPerstistentFieldNames(po);
			if (persistentFieldName.size() == 0) {
				result.addContent(po.toString());
			} else
				// for (int i = 0; i < persistentFieldName.length; i++) {
				for (Iterator<Entry<String, Class<?>>> it = persistentFieldName.entrySet().iterator(); it.hasNext();) {
					Entry<String, Class<?>> entry = it.next();
					String fieldName = entry.getKey();
					Class<?> valueClass = entry.getValue();
					String className = valueClass.getName();
					Object value = PropertyUtils.getProperty(po, fieldName);
					if (value == null) {
						// seems to be empty
					} else if (value instanceof IPersistable) {
						// assuming a Reference or relationship
						Element e = getXmlReferenceElement(FIELD, (IPersistable) value, fieldName);
						result.addContent(e);
					} else if (value instanceof Collection) {
						Collection<?> javaCollection = (Collection<?>) value;
						Element xmlCollection = new Element(COLLECTION);
						xmlCollection.setAttribute(NAME, fieldName);
						xmlCollection.setAttribute(CLASS, className);
						if (javaCollection.size() == 0)
							continue;
						for (Iterator<?> it2 = javaCollection.iterator(); it2.hasNext();) {
							Object element = it2.next();
							Element e;
							if (element instanceof IPersistable) {
								e = getXmlReferenceElement(ITEM, (IPersistable) element, fieldName);
							} else {
								e = getXMLElement(FIELD, element, fieldName);
							}
							xmlCollection.addContent(e);
						}
						result.addContent(xmlCollection);
					} else if (value instanceof Map) {
						Map<?, ?> javaMap = (Map<?, ?>) value;
						Element xmlCollection = new Element(MAP);
						xmlCollection.setAttribute(NAME, fieldName);
						xmlCollection.setAttribute(CLASS, className);
						if (javaMap.size() == 0){
							continue;
						}
						for (Iterator<?> iter = javaMap.entrySet().iterator(); iter.hasNext();) {
							Map.Entry<?, ?> en = (Entry<?, ?>) iter.next();
							Object key = en.getKey();
							Object item = en.getValue();
							Element mapentry = new Element(ENTRY);
							Element e;
							Element mapkey = new Element(KEY);
							Element mapvalue = new Element(VALUE);
							mapentry.addContent(mapkey);
							mapentry.addContent(mapvalue);
							if (key instanceof IPersistable) {
								e = getXmlReferenceElement(ITEM, (IPersistable) key, null);
							} else {
								e = getXMLElement(FIELD, key, null);
							}
							mapkey.addContent(e);
							if (item instanceof IPersistable) {
								e = getXmlReferenceElement(ITEM, (IPersistable) item, null);
							} else {
								e = getXMLElement(FIELD, item, null);
							}
							mapvalue.addContent(e);
							xmlCollection.addContent(mapentry);
						}
						result.addContent(xmlCollection);
					} else if (value instanceof Object && value.getClass().getName().startsWith(DOMAIN_ENDRESS)) {
						Element structuredXmlDataElement = getXMLElement(FIELD, value, fieldName);
						result.addContent(structuredXmlDataElement);
					} else if (value instanceof TimeZone) {
						Element tz = new Element(FIELD);
						tz.setAttribute(NAME, fieldName);
						tz.setAttribute(CLASS, className);
						tz.addContent(((TimeZone) value).getID().toString());
						result.addContent(tz);
					} else {
						// check if array
						if (value.getClass().isArray()) {
							Element xmlArray = new Element(ARRAY);
							xmlArray.setAttribute(NAME, fieldName);
							xmlArray.setAttribute(CLASS, valueClass.getComponentType().getName());
							if (Array.getLength(value) == 0)
								continue;
							Object p = null;
							for (int j = 0; j < Array.getLength(value); j++) {
								p = Array.get(value, j);
								Element e;
								if (p instanceof IPersistable) {
									e = getXmlReferenceElement(ITEM, (IPersistable) p, fieldName);
								} else {
									e = getXMLElement(FIELD, p, fieldName);
								}
								xmlArray.addContent(e);
							}
							// if (p != null)
							// xmlArray.setAttribute(CLASS, className);
							result.addContent(xmlArray);
						} else if (value instanceof java.util.Date) {
							Element java = new Element(FIELD);
							java.setAttribute(NAME, fieldName);
							java.setAttribute(CLASS, className);
							java.addContent((((java.util.Date) value).getTime()) + "");
							result.addContent(java);
						} else if (value instanceof Object) {
							// handle the java types
							if (value instanceof java.sql.Date) {
							}
							Element java = new Element(FIELD);
							java.setAttribute(NAME, fieldName);
							java.setAttribute(CLASS, className);
							java.addContent(value.toString());
							result.addContent(java);
						} else {
							// TODO handle the native types ??
							// --> Seems to be done by BeanUtils anyway
						}
					}
				}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * Create a list (array) of probably persistent attribute of the object. The
	 * criteria is <br>
	 * 1. a field is declared <br>
	 * 2. a public getter with the name get&lt;field&gt; for the field is
	 * declared.
	 */
	public static Map<String, Class<?>> getPerstistentFieldNames(Object o) {
		Class<?> c = o.getClass();
		Map<String, Class<?>> result = new HashMap<String, Class<?>>();
		while (!c.equals(Object.class) && !c.equals(PersistablePFW2.class)) {
			Field[] declaredFields = c.getDeclaredFields();
			for (int i = 0; i < declaredFields.length; i++) {
				String name = declaredFields[i].getName();
				Class<?> type = declaredFields[i].getType();
				if (name.indexOf('$') == -1) {
					try {
						// check if a getter is available
						PropertyUtils.getProperty(o, name);
						result.put(name, type);
					} catch (NoSuchMethodException e) {
						// OK - no public getter - no persistence
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			c = c.getSuperclass();
		}
		return result;
	}

	/**
	 * Print a XML document in pretty XML format.
	 * 
	 * @param doc
	 *            the document to print
	 */
	static public void printXML(Document doc) {
		XMLOutputter outp = new XMLOutputter();
		outp.setFormat(Format.getPrettyFormat());
		try {
			outp.output(doc, System.out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a &lt;ref&gt; element pointing to another IPersistable
	 * 
	 * @param reference
	 * @param attributeName
	 * @return
	 */
	public static Element getXmlReferenceElement(String tag, IPersistable reference, String attributeName) {
		Element result = new Element(tag);
		result.setAttribute(NAME, attributeName);
		result.setAttribute(CLASS, reference.getClass().getName());
		try {
			Element e = new Element(REF);
			e.setAttribute(ID, reference.getKey().toString());
			result.addContent(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * Create a collection of IPersistable instances as they are described in
	 * the XML Input Stream
	 * 
	 * @param stream
	 * @return
	 */
	public static Collection<?> createObjectsFromXML(InputStream stream) {
		Collection<?> result = null;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(stream);
			result = createObjectsFromXML(doc);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Create a collection of IPersistable instances as they are described in
	 * the XML Input Stream
	 * 
	 * @param doc
	 * @return
	 */
	public static Collection<?> createObjectsFromXML(Document doc) {
		Map<String, Object> objectCache = new HashMap<String, Object>();
		objects.set(objectCache);
		// get <objectset>
		Element objectset = (Element) doc.getDescendants().next();
		// pass 1 : create instances
		for (Iterator<?> it = objectset.getChildren().iterator(); it.hasNext();) {
			Element element = (Element) it.next();
			String clazz = element.getAttribute(CLASS).getValue();
			String id = element.getAttribute(ID).getValue();
			try {
				Object persistable = createInstance(clazz);
				objectCache.put(id, persistable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// pass 2 : set fields
		for (Iterator<?> it = objectset.getChildren().iterator(); it.hasNext();) {
			Element element = (Element) it.next();
			String id = element.getAttribute(ID).getValue();
			Object bean = objectCache.get(id);
			if (bean == null)
				log.fatal("No bean found for " + id);
			for (Iterator<?> eit = element.getChildren().iterator(); eit.hasNext();) {
				Element item = (Element) eit.next();
				// now were should be able to set the value
				processXmlElement(bean, item);
			}
		}
		return objectCache.values();
	}

	/**
	 * 
	 * @param bean
	 *            the bean that shall be stuffed
	 * @param item
	 *            May be called with "field", "ref", "collection" element
	 */
	private static void processXmlElement(Object bean, Element item) {
		// get the fieldname of the field that schold be set
		String fieldName = item.getAttribute(NAME).getValue();

		Object result = null;
		// OK seems to be writeable
		if (item.getName().equals(COLLECTION)) {
			result = processCollection(bean, item);
		} else if (item.getName().equals(MAP)) {
			result = processMap(bean, item);
		} else if (item.getName().equals(FIELD)) {
			result = processField(bean, item);
		} else if (item.getName().equals(ARRAY)) {
			result = processArray(bean, item);
		} else {
			throw new RuntimeException("Unexpected Element " + item.getName());
		}
		try {
			BeanUtils.setProperty(bean, fieldName, result);
		} catch (Exception e) {
			log.fatal("**** ERROR : Can not set '" + fieldName + "' to " + result + ", Reason = " + e.getMessage());
			System.out.println("**** ERROR : Can not set '" + fieldName + "' to " + result + ", Reason = "
					+ e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private static Object processField(Object bean, Element field) {
		if (field.getAttribute(CLASS) == null)
			return null; // no class -> assuming NULL value
		String clazz = field.getAttribute(CLASS).getValue();
		Object result = null;
		try {

			Collection<?> children = field.getChildren();
			if (children.isEmpty()) {
				if (clazz.equals("java.util.Date")) {
					return new java.util.Date(Long.parseLong(field.getTextTrim()));
				} else if (clazz.equals("java.sql.Date")) {
					return new java.sql.Date(Long.parseLong(field.getTextTrim()));
				} else if (clazz.equals("java.sql.Timestamp")) {
					return new java.sql.Timestamp(Long.parseLong(field.getTextTrim()));
				} else
					return field.getTextTrim();
			} else {

				result = createInstance(clazz); // new PZ
				for (Iterator<?> it = children.iterator(); it.hasNext();) {
					Element element = (Element) it.next();
					// reference
					if (element.getName().equals(REF)) {
						String id = element.getAttributeValue(ID);
						result = ((Map<?, ?>) objects.get()).get(id);
					} else
						processXmlElement(result, element);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return result;
	}

	private static Object createInstance(String clazz) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		// org.hibernate
		if (clazz.startsWith(DOMAIN_ENDRESS)) {
			int pos = clazz.indexOf('$');
			if (pos != -1) {
				clazz = clazz.substring(0, pos);
			}
			PersistenceManager persistenceManager = PersistenceContext.getPersistenceManager();
			Object result = Class.forName(clazz).newInstance();
			if( result instanceof Persistable ){
				((Persistable)result).markModified(ModificationType.NEW);
				persistenceManager.addObjectInUse((Persistable)result);
			}
			return result;
		}
		if (clazz.startsWith(DOMAIN_HIBERNATE)) {
			// TODO remove work araound with
			// HibernateProxyHelper.getClassWithoutInitializingProxy();
			if (clazz.indexOf("Set") != -1)
				clazz = "java.util.HashSet";
			else if (clazz.indexOf("Map") != -1)
				clazz = "java.util.HashMap";
			else if (clazz.indexOf("List") != -1)
				clazz = "java.util.ArrayList";
		} else if (clazz.startsWith("java.util.")) {
			if (clazz.equals("java.util.Set"))
				clazz = "java.util.HashSet";
			if (clazz.equals("java.util.List"))
				clazz = "java.util.ArrayList";
			if (clazz.equals("java.util.Map"))
				clazz = "java.util.HashMap";
			// TODO : more types
		} else
			clazz = "java.lang.String";
		return Class.forName(clazz).newInstance();
	}

	@SuppressWarnings("unchecked")
	private static Collection<?> processCollection(Object bean, Element collection) {
		String clazz = collection.getAttribute(CLASS).getValue();
		Object result = null;
		try {
			result = createInstance(clazz);
			for (Iterator<?> it = collection.getChildren().iterator(); it.hasNext();) {
				Element element = (Element) it.next();
				((Collection<Object>) result).add(processField(bean, element));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return (Collection<?>) result;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " clazz = " + clazz + " " + result.getClass().getName());
			e.printStackTrace();
			return null;
		}
	}

	private static Object processArray(Object bean, Element collection) {
		String className = collection.getAttribute(CLASS).getValue();
		Object result = null;
		try {
			Class<?> clazz = Class.forName(className);
			int elements = collection.getChildren().size();
			int i = 0;
			result = Array.newInstance(clazz, elements);
			for (Iterator<?> it = collection.getChildren().iterator(); it.hasNext();) {
				Element element = (Element) it.next();
				Array.set(result, i++, processField(bean, element));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return result;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " clazz = " + className + " " + result.getClass().getName());
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static Map processMap(Object bean, Element collection) {
		String clazz = collection.getAttribute(CLASS).getValue();
		Map<Object, Object> result = null;
		try {
			result = (Map<Object, Object>) createInstance(clazz);
			for (Iterator<?> it = collection.getChildren().iterator(); it.hasNext();) {
				Element entry = (Element) it.next();
				Element key = entry.getChild(KEY);
				Object keyvalue = processField(bean, key.getChild(FIELD));
				Element value = entry.getChild(VALUE);
				Object mapvalue = processField(bean, value.getChild(FIELD));
				result.put(keyvalue, mapvalue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return result;
		} catch (Exception e) {
			System.out.println(e.getMessage() + " clazz = " + clazz + " " + result.getClass().getName());
			e.printStackTrace();
			return null;
		}
	}
}
