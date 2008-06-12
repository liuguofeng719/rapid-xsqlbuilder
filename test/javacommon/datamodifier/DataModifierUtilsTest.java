package javacommon.datamodifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;

import javacommon.datamodifier.modifier.TimestampDataModifier;
import junit.framework.TestCase;

/**
 * 
 * @author badqiu
 *
 */
public class DataModifierUtilsTest extends TestCase {

	public void testAllModifier() throws Exception {
		Object value = DataModifierUtils.modify("status?default", null);
		assertEquals("",value);
		
		value = DataModifierUtils.modify("status?string", new Integer(33));
		assertEquals("33",value);
		
		value = DataModifierUtils.modify("status?int", "123");
		assertEquals(new Integer(123),value);
		
		value = DataModifierUtils.modify("status?long", "123");
		assertEquals(new Long(123),value);
		
		value = DataModifierUtils.modify("status?float", "123");
		assertEquals(new Float((short)123),value);
		
		value = DataModifierUtils.modify("status?double", "123");
		assertEquals(new Double((short)123),value);

		value = DataModifierUtils.modify("status?boolean", "true");
		assertEquals(new Boolean(true),value);
		
		value = DataModifierUtils.modify("status?short", "123");
		assertEquals(new Short((short)123),value);
		
		value = DataModifierUtils.modify("status?byte", "123");
		assertEquals(new Byte((byte)123),value);
		
		value = DataModifierUtils.modify("status?timestamp", "2005-10-10 10:10:10.12");
		assertTrue(value instanceof Timestamp);
		
		value = DataModifierUtils.modify("status?date", "2005-10-10 10:10:10");
		assertTrue(value instanceof java.util.Date);
		
		value = DataModifierUtils.modify("status?sqldate", "2005-10-10");
		assertTrue(value instanceof java.sql.Date);
		
		value = DataModifierUtils.modify("status?sqltime", "10:10:10");
		assertTrue(value instanceof java.sql.Time);
		
		value = DataModifierUtils.modify("status?BigInteger", "10");
		assertTrue(value instanceof BigInteger);
		
		value = DataModifierUtils.modify("status?BigDecimal", "10.0");
		assertTrue(value instanceof BigDecimal);
	}
	
	public void testModifierWithArgument() throws Exception {
		Object value = DataModifierUtils.modify("status?default(badqiu)", null);
		assertEquals("badqiu",value);
		
		try {
			value = DataModifierUtils.modify("status?timestamp(yyyy-MM-dd)", "10:10:10.12");
			assertNotNull(value);
			fail();
		}catch(Exception expect) {
			assertTrue(true);
		}
		
	}
	
	public void testMultipleModifiers() throws Exception {
		Object value = DataModifierUtils.modify("status?default(2005-10-10 10:10:10)?date", null);
		assertNotNull(value);
	}
	
	public void testNullArgument() throws Exception {
		Object value = DataModifierUtils.modify("status?int", null);
		assertNull(value);
	}
	
	public void testRegisterNewModifer() throws Exception {
		DataModifierUtils.registerDataModifier("timestamp-fortest", new TimestampDataModifier("MM/dd/yyyy"));
		Timestamp result = (Timestamp)DataModifierUtils.modify("status?timestamp-fortest","5/6/2005");
		assertEquals(2005,1900+result.getYear());
		assertEquals(4,result.getMonth());
		assertEquals(5,result.getDay());
	}
	
	public void testDeregisterDataModifier() {
		DataModifierUtils.deregisterDataModifier("int");
		try {
			DataModifierUtils.modify("status?int", "1");
			fail();
		}catch(DataModifierSyntaxException expect) {
			assertTrue(true);
		}
	}
	
	public void testSyntax() throws Exception {
		try {
			DataModifierUtils.modify("status?default(1", null);
			fail();
		}catch(DataModifierSyntaxException expect) {
			assertTrue(true);
		}
	}
	
	public void testGetModifyVariable() {
		assertEquals("var",DataModifierUtils.getModifyVariable("var"));
		assertEquals("var",DataModifierUtils.getModifyVariable("var?date"));
	}
}
