package javacommon.xsqlbuilder;

public class IbatisStyleXsqlBuilder extends XsqlBuilder{
	
	public IbatisStyleXsqlBuilder() {
		markKeyEndChar = "#";
		markKeyStartChar = "#";
		
		replaceKeyEndChar = "$";
		replaceKeyStartChar = "$";
	}
	
}
