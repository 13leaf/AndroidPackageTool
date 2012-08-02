package tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Attributes;

/**
 * SVN辅助工具,辅助SVN来完成诸如程序更新等任务.
 * TODO project配置文件需要多配置如下内容
 * projectUrl 项目路径地址
 * exportDir=projectPath
 * -------
 * svn account=
 * userName
 * password
 * 
 * TODO 增加Base64来加密用户名、密码以及keystore的信息。
 * @author 13leaf
 *
 */
public class SVNHelper {
	
	private static final String INFO_TEMPLATE="svn info ${projectUrl} --username ${userName} --password ${password}";
	
	private static final String EXPORT_TEMPLATE="svn export --force ${projectUrl} ${exportDir} --username ${userName} --password ${password}";
	
	private static final Pattern versionPattern=Pattern.compile("\\D+(\\d+)");
	
	private Attributes attributes;
	
	private int headProjectVersion;
	
	private int latestProjectVersion;
	
	private CommandWrapper commander=CommandWrapper.getCommandWrapper();
	
	
	public SVNHelper(String projectUrl,String userName,String password)
	{
		attributes=new Attributes();
		attributes.put("userName", userName);
		attributes.put("password", password);
		setProjectUrl(projectUrl);
	}
	
	public void setProjectUrl(String projectUrl)
	{
		attributes.put("projectUrl", projectUrl);
		String info=commander.doSychronizeCommand(INFO_TEMPLATE, attributes);
		if(info.equals("")) return;//if svn fail
		Matcher matcher=versionPattern.matcher(info.split("\n")[4]);
		matcher.find();
		System.out.println("get head project version:"+matcher.group(1));
		headProjectVersion=Integer.parseInt(matcher.group(1));
		matcher=versionPattern.matcher(info.split("\n")[7]);
		matcher.find();
		System.out.println("get current projcet version:"+matcher.group(1));
		latestProjectVersion=Integer.parseInt(matcher.group(1));
	}
	
	public int getProjectVersion()
	{
		return latestProjectVersion;
	}
	
	public int getHeadProjectVersion()
	{
		return headProjectVersion;
	}
	
	public void exportProject(String exportDirPath)
	{
		attributes.put("exportDir", exportDirPath);
		commander.doSychronizeCommand(EXPORT_TEMPLATE, attributes);
	}

	public static void main(String[] args) {
		SVNHelper helper=new SVNHelper("http://192.168.19.115/svn/ifengclient_android/IfengVideo4HiPad","wangfeng","EfE5WRka");
		helper.exportProject("projects\\IfengVideo4HiPad");
//		helper.exportProject("D:/IfengWeekly_Current");
		System.out.println();
		System.out.println(helper.getProjectVersion());
		System.out.println(helper.getHeadProjectVersion());
		
	}
}
