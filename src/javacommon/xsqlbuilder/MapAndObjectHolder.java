package javacommon.xsqlbuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

class MapAndObjectHolder implements Map {
	Map map;
	Object bean;

	public MapAndObjectHolder(Map map, Object bean) {
		super();
		this.map = map;
		this.bean = bean;
	}

	static Pattern ERROR_METHOD_PATTERN = Pattern.compile("^[^a-zA-Z_].*");
	Object getProperty(Object key) {
		if (map != null) {
			return map.get(key);
		}
		
		if(bean != null && bean instanceof Map) {
			return ((Map)bean).get(key);
		}
		
		Object result = null;
		if (bean != null && key instanceof String) {
			String prop = (String)key;
			
			if(ERROR_METHOD_PATTERN.matcher(prop).matches()) {
				return null;
			}
			
			try {
				result = PropertyUtils.getProperty(bean, prop);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
						"cannot get property value by property:" + key
								+ " on class:" + bean.getClass(), e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(
						"cannot get property value by property:" + key
								+ " on class:" + bean.getClass(), e);
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
		return result;
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public Set entrySet() {
		throw new UnsupportedOperationException();
	}

	public Object get(Object key) {
		return getProperty(key);
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public Set keySet() {
		throw new UnsupportedOperationException();
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map m) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	public Collection values() {
		throw new UnsupportedOperationException();
	}
}