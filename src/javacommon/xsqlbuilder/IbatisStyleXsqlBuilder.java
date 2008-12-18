package javacommon.xsqlbuilder;

/**
 * 支持Ibatis类似的语法
 * <pre>
 * 		String sql = "select * from user where 1=1"
 *			+"/~ and username = #username#~/"
 *			+"/~ and pwd = '$password$'~/";
 * </pre>
 * @author badqiu
 *
 */
public class IbatisStyleXsqlBuilder extends XsqlBuilder{
	
	public IbatisStyleXsqlBuilder() {
		markKeyEndChar = "#";
		markKeyStartChar = "#";
		
		replaceKeyEndChar = "$";
		replaceKeyStartChar = "$";
	}
	
}
