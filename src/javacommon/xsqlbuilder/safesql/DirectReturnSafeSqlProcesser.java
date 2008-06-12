package javacommon.xsqlbuilder.safesql;

import javacommon.xsqlbuilder.SafeSqlProcesser;

public class DirectReturnSafeSqlProcesser implements SafeSqlProcesser{
	
	public static SafeSqlProcesser INSTANCE = new DirectReturnSafeSqlProcesser();
	
	public String process(String value) {
		return value;
	}

}
