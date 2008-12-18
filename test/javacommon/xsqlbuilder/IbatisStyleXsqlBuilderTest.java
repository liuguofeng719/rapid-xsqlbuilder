package javacommon.xsqlbuilder;

import java.util.HashMap;
import java.util.Map;

import javacommon.xsqlbuilder.XsqlBuilder.XsqlFilterResult;
import junit.framework.TestCase;

public class IbatisStyleXsqlBuilderTest extends TestCase {
	
	IbatisStyleXsqlBuilder builder = new IbatisStyleXsqlBuilder();
	
	public void test() {
		Map filters = new HashMap();
		String sql = "select * from user where 1=1"
			+"/~ and username = #username#~/"
			+"/~ and pwd = '$password$'~/"
			+"/~ and age = '$age$'~/";
		XsqlFilterResult result = builder.applyFilters(sql, filters);
		
		assertEquals("select * from user where 1=1",result.getXsql());
		assertEquals(0,result.getAcceptedFilters().size());
		
		filters.put("username", "badqiu");
		filters.put("age", "12");
		result = builder.applyFilters(sql, filters);
		assertEquals("select * from user where 1=1 and username = #username# and age = '12'",result.getXsql());
		assertEquals(1,result.getAcceptedFilters().size());
		assertTrue(result.getAcceptedFilters().keySet().contains("username"));
	}
}
