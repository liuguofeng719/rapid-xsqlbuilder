package javacommon.xsqlbuilder;

import javacommon.xsqlbuilder.safesql.EscapeBackslashAndSingleQuotesSafeSqlProcesser;
import javacommon.xsqlbuilder.safesql.EscapeSingleQuotesSafeSqlProcesser;
/**
 * 工厂方法,提供不同数据库的SafeSqlProcesser实例生成工厂
 * @author badqiu
 *
 */
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
