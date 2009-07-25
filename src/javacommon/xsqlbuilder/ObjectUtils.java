package javacommon.xsqlbuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

class ObjectUtils {

	public static Object getProperty(Object obj,String key) {
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
