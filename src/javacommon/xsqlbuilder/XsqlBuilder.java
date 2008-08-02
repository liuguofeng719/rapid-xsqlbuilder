package javacommon.xsqlbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javacommon.datamodifier.DataModifierUtils;
import javacommon.xsqlbuilder.safesql.DirectReturnSafeSqlProcesser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用于动态构造sql语句,与SafeSqlProcesser集成提供防止sql注入攻击,与DataModifier集成完成数据类型的转换
 * 动态构造sql示例:
 * <pre>
	String xsql = "select * from user where 1=1
		/~ and username = {username} ~/
		/~ and password = {password} ~/
		/~ and age = [age] ~/"
		/~ and sex = [sex] ~/"
		 
	<br/>
	
	Map filters = new HashMap();
	filters.put("username", "badqiu");
	filters.put("age", "12");
	filters.put("sex", "");
	
	<br/>
	
	XsqlFilterResult result = xsqlBuilder.applyFilters(xsql,filters);
	<br/>
	
	结果result.getXsql()将会等于
	select * from user where 1=1 and username={username} and age=12
	而<font color=red>/~ and password = {password} ~/"</font>这一段由于在filters中password不存在而没有被构造出来
	<font color=red>/~ and sex = [sex] ~/</font>由于sex的值为空串也没有被构造出来
	<br/>
	Map acceptedFilters = result.getAcceptedFilters();
	会等于:
	{username=badqiu}
	
	<br/>
	相关符号介绍:
	/~ segment... ~/ 为一个条件代码块
	{key} 过滤器中起标记作用的key,作为后面可以替换为sql的?,或是hql的:username标记
	[key] 将直接替换为key value
 * </pre>
 * 
 * <pre>
 * 数据类型转换示例:
 * select * from user where and 1=1 /~ age={age?int} ~/
 * 将会将Map filters中key=age的值转换为int类型
 * </pre>
 * 
 * @author badqiu
 *
 */
public class XsqlBuilder {

	final static Log logger = LogFactory.getLog(XsqlBuilder.class);
	
	private boolean isRemoveEmptyString = true;
	private SafeSqlProcesser safeSqlProcesser = DirectReturnSafeSqlProcesser.INSTANCE;
	
	public XsqlBuilder() {
	}
	
	public XsqlBuilder(boolean isRemoveEmptyStrings) {
		setRemoveEmptyString(isRemoveEmptyStrings);
	}

	public XsqlBuilder(SafeSqlProcesser safeSqlProcesser) {
		setSafeSqlProcesser(safeSqlProcesser);
	}
	
	public XsqlBuilder(boolean isRemoveEmptyStrings,SafeSqlProcesser safeSqlProcesser) {
		setRemoveEmptyString(isRemoveEmptyStrings);
		setSafeSqlProcesser(safeSqlProcesser);
	}
	
	/** 
	 * 是否移除空字符串,默认为true
	 **/
	public boolean isRemoveEmptyString() {
		return isRemoveEmptyString;
	}

	public void setRemoveEmptyString(boolean isRemoveEmptyStrings) {
		this.isRemoveEmptyString = isRemoveEmptyStrings;
	}

	public SafeSqlProcesser getSafeSqlProcesser() {
		return safeSqlProcesser;
	}

	public void setSafeSqlProcesser(SafeSqlProcesser safeSqlProcesser) {
		if(safeSqlProcesser == null)
			throw new NullPointerException("'safeSqlProcesser' property must be not null");
		this.safeSqlProcesser = safeSqlProcesser;
	}

	/**
	 * 根据sourceXsql动态构造SQL语句,将{key}替换为?,[key]替换为key value
	 * @param sourceXsql 
	 * @param filters 过滤器,Map的key必须为String类型
	 * @return 
	 */
	public XsqlFilterResult generateSql(String sourceXsql, Map filters) {
		XsqlFilterResult sfr = applyFilters(sourceXsql, filters);
		return new XsqlFilterResult(replaceKeyMaskWithString(sfr.getXsql(), sfr.getAcceptedFilters(),"?"),sfr.getAcceptedFilters());
	}
	/**
	 * 根据sourceXsql动态构造Hibernate的hql语句,将{key}替换为:key,[key]替换为key value
	 * @param sourceXsql 
	 * @param filters 过滤器,Map的key必须为String类型
	 * @return 
	 */
	public XsqlFilterResult generateHql(String sourceXsql, Map filters) {
		
		XsqlFilterResult sfr = applyFilters(sourceXsql, filters);
		
        String resultHql = sfr.getXsql();
		for(Iterator it = sfr.getAcceptedFilters().keySet().iterator();it.hasNext();){
			Object key = it.next();
			resultHql = StringUtils.replace(resultHql, "{"+key+"}", ":"+key);
		}
        
		return new XsqlFilterResult(resultHql,sfr.getAcceptedFilters());
	}
	/**
	 * 将xsql中的{key}标志替换为一个字符串
	 * @param xsql 格式: select * from user where username = {username}
	 * @param acceptedFilters 过滤器
	 * @param str 输入的字符串
	 * @return 如果acceptedFilters存在{username}的key，则返回select * from user where username=str
	 * 		否则没有影响，直接返回xsql
	 */
	public static String replaceKeyMaskWithString(String xsql, Map acceptedFilters,String str) {
		for(Iterator it = acceptedFilters.keySet().iterator();it.hasNext();){
			Object key = it.next();
			xsql = StringUtils.replace(xsql, "{"+key+"}", str);
		}
		return xsql;
	}
	/**
	 * 将xsql中的{key}标志替换与key相对应的值
	 * @param xsql 格式: select * from user where username = {username}
	 * @param acceptedFilters 过滤器
	 * @param str 输入的字符串
	 * @return 如果acceptedFilters存在{username}相对应的key=username的值，则返回select * from user where username=keyvalue
	 * 		否则没有影响，直接返回xsql
	 */
	public static String replaceKeyMaskWithKeyValue(String xsql, Map acceptedFilters) {
		for(Iterator it = acceptedFilters.keySet().iterator();it.hasNext();){
			Object key = it.next();
			xsql = StringUtils.replace(xsql, "{"+key+"}", acceptedFilters.get(key).toString());
		}
		return xsql;
	}
	/**
	 * 使用过滤器过滤
	 * @param xsql 数据格式：select * from user /where ~username={username}~/
	 * 			其中/~,~/为一个条件的结束与开始标志,{username}为过滤使用的key,key取值为username
	 * 			如果username在filters作为key存在,则返回结果为select * from user where username={username}
	 * 			否则过滤后结果为select * from user
	 * @param filters 过滤器,key必需为String类型
	 * @return
	 */
	public XsqlFilterResult applyFilters(String xsql, Map filters) {
		return applyFilters(new StringBuffer(xsql), filters);
	}
	/**
	 * @see #applyFilters(String, Map)
	 */
	private XsqlFilterResult applyFilters(StringBuffer xsql, Map filters) {
		LinkedHashMap acceptedFilters = new LinkedHashMap();
		for (int i = 0, end = 0, start = xsql.indexOf("/~"); ((start = xsql.indexOf("/~", end)) >= 0); i++) {
			end = xsql.indexOf("~/", start);
			KeyMetaDatas metadatas = getKeyMetaDatas(xsql, start, end);
			
			if(metadatas.markKeys.isEmpty() && metadatas.replaceKeys.isEmpty()) 
				throw new IllegalArgumentException("Not key found in segment="+xsql.substring(start, end+2));
			
			if (isAcceptedAllKeys(filters,metadatas.markKeys) && isAcceptedAllKeys(filters, metadatas.replaceKeys)) {
				if(logger.isDebugEnabled()) {
					logger.debug("The filter markKeys=" + metadatas.markKeys+" replaceKeys="+metadatas.replaceKeys + " is accepted on segment="+xsql.substring(start,end+2));
				}
				String segment = xsql.substring(start+2,end);
				segment = mergeMarkKeysIntoAcceptedFilters(filters, acceptedFilters, metadatas, segment);
				segment = replaceReplaceKeysWithValues(filters, metadatas.replaceKeys, segment);
				xsql.replace(start, end+2, segment);
				end = start + segment.length();
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("The filter markKeys=" + metadatas.markKeys+" replaceKeys="+metadatas.replaceKeys+ " is removed from the query on segment="+xsql.substring(start,end+2));
				}
				xsql.replace(start, end + 2, "");
				end = start;
			}
		}
		return new XsqlFilterResult(xsql.toString(),acceptedFilters);
	}

	private String mergeMarkKeysIntoAcceptedFilters(Map filters, Map acceptedFilters, KeyMetaDatas metadatas, String segment) {
		for(int n = 0; n < metadatas.markKeys.size(); n++) {
			String dataModifierExpression = (String)metadatas.markKeys.get(n);
			String key = DataModifierUtils.getModifyVariable(dataModifierExpression);
			Object value = DataModifierUtils.modify(dataModifierExpression, filters.get(key));
			acceptedFilters.put(key, value);
			segment = StringUtils.replace(segment, "{"+dataModifierExpression+"}", "{"+key+"}");
		}
		return segment;
	}
	
	private String replaceReplaceKeysWithValues(Map filters, List replaceKeys, String segment) {
		for(int n = 0; n < replaceKeys.size(); n++) {
			String dataModifierExpression = (String)replaceKeys.get(n);
			String key = DataModifierUtils.getModifyVariable(dataModifierExpression);
			String value = DataModifierUtils.modify(dataModifierExpression, filters.get(key)).toString();
			value = safeSqlProcesser.process(value);
			segment = StringUtils.replace(segment, "["+dataModifierExpression+"]", value);
		}
		return segment;
	}
	
	private boolean isAcceptedAllKeys(Map filters, List keys) {
		for(int n = 0; n < keys.size(); n++) {
			String dataModifierExpression = (String)keys.get(n);
			String key = DataModifierUtils.getModifyVariable(dataModifierExpression);
			Object value = filters.get(key);
			if(!isValuePopulated(value, isRemoveEmptyString)) {
				return false;
			}
		}
		return true;
	}
	
	KeyMetaDatas getKeyMetaDatas(StringBuffer xsql,int start,int end) {
		List markKeys = getKeys(xsql, start, end,"{","}");
		List replaceKeys = getKeys(xsql, start, end, "[", "]");
		return new KeyMetaDatas(markKeys,replaceKeys);
	} 
	
	private List getKeys(StringBuffer xsql, int start, int end,String keyPrifix,String keySuffix) {
		List results = new ArrayList();
		int keyStart = start;
		int keyEnd = keyStart;
		while(true) {
			keyStart = xsql.indexOf(keyPrifix,keyStart);
			if(keyStart > end || keyStart < 0)
				break;
			keyEnd = xsql.indexOf(keySuffix,keyStart);
			if(keyEnd > end  || keyEnd <0)
				break;
			String key = xsql.substring(keyStart+1,keyEnd);
			results.add(key);
			keyStart = keyEnd;
		}
		return results;
	}

	protected boolean isValuePopulated(Object value, boolean isRemoveEmptyStrings) {
		if (value == null) {
			return false;
		} 
		if (isRemoveEmptyStrings && value instanceof String) {
			return ((String) value).length() > 0;
		} else {
			return true;
		}
	}
	
	class KeyMetaDatas {
		List markKeys;
		List replaceKeys;
		public KeyMetaDatas(List markKeys, List replaceKeys) {
			this.markKeys = markKeys;
			this.replaceKeys = replaceKeys;
		}
	}
	
	public static class XsqlFilterResult {
		private String xsql;
		private Map acceptedFilters;
		public XsqlFilterResult(String xsql, Map acceptedFilters) {
			this.setXsql(xsql);
			this.setAcceptedFilters(acceptedFilters);
		}
		public void setXsql(String xsql) {
			this.xsql = xsql;
		}
		public String getXsql() {
			return xsql;
		}
		public void setAcceptedFilters(Map acceptedFilters) {
			this.acceptedFilters = acceptedFilters;
		}
		public Map getAcceptedFilters() {
			return acceptedFilters;
		}
	}
	
	//copy from spring
	private static class StringUtils {
		public static String replace(String inString, String oldPattern, String newPattern) {
			if (inString == null) {
				return null;
			}
			if (oldPattern == null || newPattern == null) {
				return inString;
			}

			StringBuffer sbuf = new StringBuffer();
			// output StringBuffer we'll build up
			int pos = 0; // our position in the old string
			int index = inString.indexOf(oldPattern);
			// the index of an occurrence we've found, or -1
			int patLen = oldPattern.length();
			while (index >= 0) {
				sbuf.append(inString.substring(pos, index));
				sbuf.append(newPattern);
				pos = index + patLen;
				index = inString.indexOf(oldPattern, pos);
			}
			sbuf.append(inString.substring(pos));

			// remember to append any characters to the right of a match
			return sbuf.toString();
		}
	}
}
