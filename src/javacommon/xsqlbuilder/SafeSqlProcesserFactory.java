package javacommon.xsqlbuilder;

import javacommon.xsqlbuilder.safesql.EscapeBackslashAndSingleQuotesSafeSqlProcesser;
import javacommon.xsqlbuilder.safesql.EscapeSingleQuotesSafeSqlProcesser;

public class SafeSqlProcesserFactory {
	
	private SafeSqlProcesserFactory(){}
	
	public static SafeSqlProcesser getMysql() {
		return new EscapeBackslashAndSingleQuotesSafeSqlProcesser();
	}
	
	public static SafeSqlProcesser getPostgreSql() {
		return new EscapeBackslashAndSingleQuotesSafeSqlProcesser();
	}
	
	public static SafeSqlProcesser getMsSqlServer() {
		return new EscapeSingleQuotesSafeSqlProcesser();
	}
	
	public static SafeSqlProcesser getOracle() {
		return new EscapeSingleQuotesSafeSqlProcesser();
	}
	
	public static SafeSqlProcesser getDB2() {
		return new EscapeSingleQuotesSafeSqlProcesser();
	}
	
	public static SafeSqlProcesser getSybase() {
		return new EscapeSingleQuotesSafeSqlProcesser();
	}
}
