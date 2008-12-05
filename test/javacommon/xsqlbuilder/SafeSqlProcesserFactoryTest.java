package javacommon.xsqlbuilder;

import javacommon.xsqlbuilder.safesql.DirectReturnSafeSqlProcesser;
import javacommon.xsqlbuilder.safesql.EscapeBackslashAndSingleQuotesSafeSqlProcesser;
import javacommon.xsqlbuilder.safesql.EscapeSingleQuotesSafeSqlProcesser;
import junit.framework.TestCase;

import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.FirebirdDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;

public class SafeSqlProcesserFactoryTest extends TestCase {

	public void testGetByHibernateDialect() {
		Dialect mysql = new MySQLDialect(); 
		SafeSqlProcesser result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(mysql);
		assertEquals(EscapeBackslashAndSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect postgre = new PostgreSQLDialect(); 
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(postgre);
		assertEquals(EscapeBackslashAndSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect sqlserver = new SQLServerDialect(); 
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(sqlserver);
		assertEquals(EscapeSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect oracle = new Oracle9Dialect(); 
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(oracle);
		assertEquals(EscapeSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect db2 = new DB2400Dialect(); 
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(db2);
		assertEquals(EscapeSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect sybase = new SybaseDialect();
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(sybase);
		assertEquals(EscapeSingleQuotesSafeSqlProcesser.class,result.getClass());
		
		Dialect other = new FirebirdDialect();
		result = SafeSqlProcesserFactory.getFromCacheByHibernateDialect(other);
		assertEquals(DirectReturnSafeSqlProcesser.class,result.getClass());
	}
	
}
