package javacommon.xsqlbuilder;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javacommon.xsqlbuilder.XsqlBuilder.XsqlFilterResult;
import javacommon.xsqlbuilder.safesql.EscapeBackslashAndSingleQuotesSafeSqlProcesser;
import javacommon.xsqlbuilder.safesql.EscapeSingleQuotesSafeSqlProcesser;
import javacommon.xsqlbuilder.testbean.BlogInfo;
import junit.framework.TestCase;


public class XsqlBuilderTest extends TestCase {
	XsqlBuilder builder = new XsqlBuilder();
	
	public void testApplyFilters() {
		Map filters = new HashMap();
		String sql = "select * from user where 1=1"
			+"/~ and username = '{username}'~/"
			+"/~ and pwd = '{password}'~/"
			+"/~ and age = '{age}'~/";
		XsqlFilterResult result = builder.applyFilters(sql, filters);
		
		assertEquals("select * from user where 1=1",result.getXsql());
		assertEquals(0,result.getAcceptedFilters().size());
		
		filters.put("username", "badqiu");
		filters.put("age", "age");
		result = builder.applyFilters(sql, filters);
		assertEquals("select * from user where 1=1 and username = '{username}' and age = '{age}'",result.getXsql());
		assertEquals(2,result.getAcceptedFilters().size());
		assertTrue(result.getAcceptedFilters().keySet().contains("username"));
		assertTrue(result.getAcceptedFilters().keySet().contains("age"));
	}
	
	public void testGetKeys() {
		StringBuffer str = new StringBuffer("{username} {badqiu}");
		List keys = builder.getKeyMetaDatas(str,0, str.length()).markKeys;
		assertTrue(keys.contains("username"));
		assertTrue(keys.contains("badqiu"));
		
		keys = builder.getKeyMetaDatas(str,"{username}".length(), str.length()).markKeys;
		assertTrue(keys.contains("badqiu"));
		assertFalse(keys.contains("username"));
		
		keys = builder.getKeyMetaDatas(str,0, "{username}".length()).markKeys;
		assertTrue(keys.contains("username"));
		assertFalse(keys.contains("badqiu"));
	}
	
	public void testApplyFiltersWithIllegalArgument() {
		String sql = "select * from user where 1=1"
			+"/~ and username~/{username}";
		Map filters = new HashMap();
		try {
			XsqlFilterResult result = builder.applyFilters(sql, filters);
			fail();
		}catch(IllegalArgumentException expected) {
			assertTrue(true);
		}
	}
	
	public void testApplyFiltersReplace() {
		String xsql = "select * from user /~order by [order] [orderDirection]~/";
		Map filters = new HashMap();
		XsqlFilterResult result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user ",result.getXsql());
		
		filters.put("order", "username");
		result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user ",result.getXsql());
		
		filters.put("orderDirection","DESC");
		result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user order by username DESC",result.getXsql());
	}
	
	public void testApplyFiltersWithMultiKeys() {
		String xsql = "select * from user /~order by {order} {orderDirection}~/";
		Map filters = new HashMap();
		XsqlFilterResult result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user ",result.getXsql());
		
		filters.put("order", "username");
		result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user ",result.getXsql());
		
		filters.put("orderDirection","DESC");
		result = builder.applyFilters(xsql, filters);
		assertEquals("select * from user order by {order} {orderDirection}",result.getXsql());
	}
	
	public void testGenerateSql() {
		Map filters = new HashMap();
		String sql = "select * from user where 1=1/~ and username = {username}~//~ and pwd = {password}~//~ and age = {age}~/";
		String result = builder.generateSql(sql, filters).getXsql().toString();
		assertEquals("select * from user where 1=1",result);
		
		filters.put("username", "badqiu");
		filters.put("age", "12");
		result = builder.generateSql(sql, filters).getXsql().toString();
		assertEquals("select * from user where 1=1 and username = ? and age = ?",result);
	}

	public void testGenerateHql1() {
		Map filters = new HashMap();
		String sql = "select * from user where 1=1/~ and username = {username}~//~ and pwd = {password}~//~ and age = {age}~/";
		String result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user where 1=1",result);
		
		filters.put("username", "badqiu");
		filters.put("age", "12");
		result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user where 1=1 and username = :username and age = :age",result);
		
		result = builder.generateHql("select * from user /~where password={password}~/ order by username desc", filters).getXsql().toString();
		assertEquals("select * from user  order by username desc",result);
	}
	
	public void testGenerateHql2() {
		Map filters = new HashMap();
		String sql = "select * from user /~where username={username} and age=[age]~//~ order by {orderBy} [order]~/";
		String result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user ",result);
		
		filters.put("username", "badqiu");
		filters.put("age", "12");
		result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user where username=:username and age=12",result);
		
		filters.put("orderBy", "username");
		result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user where username=:username and age=12",result);
		
		filters.put("order", "ASC");
		result = builder.generateHql(sql, filters).getXsql().toString();
		assertEquals("select * from user where username=:username and age=12 order by :orderBy ASC",result);
	}
	
	public void testPerformence() {
		String sql = "select * from user where 1=1"
			+"/~ and username = {username}~/"
			+"/~ and pwd = '[password]'~/"
			+"/~ and age = {age}~/";
		long startTime = System.currentTimeMillis();
		Map filters = new HashMap();
		filters.put("username", "badqiu");
		filters.put("password", "123");
		filters.put("age", "age");
		String result = null;
		
		int count = 100000;
		for(int i = 0; i < count; i++) {
			result = builder.generateSql(sql, filters).getXsql();
		}
		long endTime = System.currentTimeMillis();
		long costTime = endTime-startTime;
		System.out.println("[PerformenceTest],count:"+100000+" costTime:"+costTime+"ms");
		
		assertEquals(result,"select * from user where 1=1 and username = ? and pwd = '123' and age = ?");
	}
	
	public void testGenerateSql2() {
		Map filters = new HashMap();
		String sql = "select * from user /~where username=[username]~/";
		String result = builder.generateSql(sql, filters).getXsql();
		assertEquals("select * from user ",result);
		filters.put("username", "abc");
		result = builder.generateSql(sql, filters).getXsql();
		assertEquals("select * from user where username=abc",result);
	}
	
	public void testDataModifier() {
		Map filters = new HashMap();
		
		String sql = "select * from user " +
				"/~age={age?long}~/" +
				"/~birthDate={birthDate?timestamp}~/" +
				"/~num=[num?string]~/";
		filters.put("age", "20");
		filters.put("birthDate", "1990-10-10 10:10:10.111");
		filters.put("num", new Long(10));
		XsqlFilterResult result = builder.applyFilters(sql, filters);
		Map acceptedFilters = result.getAcceptedFilters();
		assertEquals(new Long(20),acceptedFilters.get("age"));
		assertTrue(acceptedFilters.get("birthDate") instanceof Timestamp);
		assertNull(acceptedFilters.get("num"));
		assertEquals("select * from user age={age}birthDate={birthDate}num=10",result.getXsql());
	}
	
	public void testDataModifierWithEmptyValue() {
		Map filters = new HashMap();
		
		String sql = "select * from user " +
				"/~age={age?long}~/";
		filters.put("age", "");
		XsqlFilterResult result = builder.applyFilters(sql, filters);
		assertEquals("select * from user ",result.getXsql());
	}
	
	public void testSafeSqlFilter() {
		Map filters = new HashMap();
		String sql = "select * from user " +
			"/~where name='[name]'~/";
		
		XsqlBuilder builder1 = new XsqlBuilder(true);
		filters.put("name", "bad'qiu");
		XsqlFilterResult result1 = builder1.applyFilters(sql, filters);
		assertEquals("select * from user where name='bad'qiu'",result1.getXsql());
		
		XsqlBuilder builder2 = new XsqlBuilder(true,new EscapeSingleQuotesSafeSqlProcesser());
		filters.put("name", "bad'qiu");
		XsqlFilterResult result2 = builder2.applyFilters(sql, filters);
		assertEquals("select * from user where name='bad''qiu'",result2.getXsql());
		
		XsqlBuilder builder3 = new XsqlBuilder(true,new EscapeBackslashAndSingleQuotesSafeSqlProcesser());
		filters.put("name", "bad'\\qiu");
		XsqlFilterResult result3 = builder3.applyFilters(sql, filters);
		assertEquals("select * from user where name='bad''\\\\qiu'",result3.getXsql());
	}
	
	public void testDemo() {
		// 清晰的sql语句,/~ ~/为一个语法块
		 String sql= "select * from user where 1=1 " 
		         + "/~ and username = {username} ~/"   
		         + "/~ and password = {password} ~/";   
		 
		 // filters为参数
		 Map filters = new HashMap();   
		 filters.put("username", "badqiu"); 
		 filters.put("sex", "F");  
		 
		 XsqlFilterResult result = new XsqlBuilder().generateHql(sql,filters);
		 
		 assertTrue(result.getAcceptedFilters().containsKey("username"));
		 assertFalse(result.getAcceptedFilters().containsKey("sex"));
		 assertEquals("select * from user where 1=1  and username = :username ", result.getXsql());
	}
	
	public void testWithNullArguments() {
		try {
			XsqlFilterResult result = new XsqlBuilder().generateHql(null,new HashMap());
		}catch(IllegalArgumentException e) {
			
		}
		XsqlFilterResult result = new XsqlBuilder().generateHql("",null);
		result = new XsqlBuilder().generateHql("",null,null);
	}
	
	public void testBean() {
		// 清晰的sql语句,/~ ~/为一个语法块
		 String sql= "select * from user where 1=1 " 
		         + "/~ and title = {title} ~/"   
		         + "/~ and sex = {sex} ~/"   
		         + "/~ and salary = {salary} ~/"   
		         + "/~ and age = [age] ~/";   
		 
		 // filters为参数
		 BlogInfo info = new BlogInfo();
		 info.setTitle("java");
		 XsqlFilterResult result = new XsqlBuilder().generateHql(sql,info);
		 
		 assertEquals("select * from user where 1=1  and title = :title  and sex = :sex  and age = 0 ", result.getXsql());
		 assertTrue(result.getAcceptedFilters().containsKey("sex"));
		 assertTrue(result.getAcceptedFilters().containsKey("title"));
		 assertFalse(result.getAcceptedFilters().containsKey("age"));

	}
	
	public void testMapAndObjectHolder() {
		// 清晰的sql语句,/~ ~/为一个语法块
		 String sql= "select * from user where 1=1 " 
		         + "/~ and title = {title} ~/"   
		         + "/~ and sex = {sex} ~/"   
		         + "/~ and salary = {salary} ~/"   
		         + "/~ and age = [age] ~/"   
		 		 + "/~ and mapKey = [mapKey] ~/";   
		 
		 // filters为参数
		 BlogInfo info = new BlogInfo();
		 info.setTitle("java");
		 Map hashMap = new HashMap();
		 hashMap.put("mapKey", "2009_mapKey");
		 MapAndObjectHolder holder = new MapAndObjectHolder(hashMap,info);
		 
		 XsqlFilterResult result = new XsqlBuilder().generateHql(sql,holder);
		 
		 assertEquals("select * from user where 1=1  and title = :title  and sex = :sex  and age = 0  and mapKey = 2009_mapKey ", result.getXsql());
		 assertTrue(result.getAcceptedFilters().containsKey("sex"));
		 assertTrue(result.getAcceptedFilters().containsKey("title"));
		 assertFalse(result.getAcceptedFilters().containsKey("age"));

	}
}
