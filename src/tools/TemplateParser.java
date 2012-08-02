package tools;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 模板解析类。关于模板详见 {@link IName}，目前只支持最多两层嵌套的属性。<br>
 * 实现扩展属性可以扩展Attributes类，为其添加字段。当查找到对应名称的属性后，会将字段的实例值进行toString()处理。
 * @author 13leaf
 *
 */
public class TemplateParser {

	/**
	 * 解析模板表达式，将其中的属性占位符替换成为Attributes中包含的对应字段值。
	 * @param templateExp
	 * @param attributes
	 * @return
	 */
	public static String parseTemplate(String templateExp,Properties attributes){
		//提取template中的属性名称
		Pattern pattern=Pattern.compile("\\$\\{(.+?)\\}");
		Matcher matcher=pattern.matcher(templateExp);
		
		StringBuffer sb=new StringBuffer();
		while(matcher.find()){
			String attrName=matcher.group(1);
			try {
				Object attrValue=attributes.get(attrName);
				if(attrValue==null) throw new RuntimeException(attrName+"未初始化");
				String value=attrValue.toString();
				
				value=value.replace("\\", "\\\\");//避免引起appendReplacement的转义
				matcher.appendReplacement(sb, value);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("出现严重异常");
			}
		}
		matcher.appendTail(sb);
		
		return sb.toString();
	}
	
}
