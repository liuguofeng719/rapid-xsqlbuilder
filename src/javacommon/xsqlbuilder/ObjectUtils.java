package javacommon.xsqlbuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

class ObjectUtils {
	
	public static Map describe(Object bean) {
		try {
			return BeanUtils.describe(bean);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("cannot describe bean properties on object class:"+bean.getClass(),e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("cannot describe bean properties on object class:"+bean.getClass(),e);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("cannot describe bean properties on object class:"+bean.getClass(),e);
		}
	}
	
	public static Map putAllAndDescribe(Map map,Object bean) {
		if(map == null && bean == null) {
			return null;
		}
		if(map == null) {
			return describe(bean);
		}else if(bean == null) {
			return map;
		}
		
		map.putAll(describe(bean));
		return map;
	}
	
	public static Object getProperty(Object obj,String key) {
		if(obj == null) return null;
		if(obj instanceof Map) {
			Map map = (Map)obj;
			return map.get(key);
		}else {
			try {
				return PropertyUtils.getProperty(obj, key);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("cannot get property value by property:"+key+" on object class:"+obj.getClass(),e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("cannot get property value by property:"+key+" on object class:"+obj.getClass(),e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("cannot get property value by property:"+key+" on object class:"+obj.getClass(),e);
			}
		}
	}
	
}
