package model;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.CommandWrapper;

/**
 * android平台
 * @author 13leaf
 *
 */
public class AndroidTarget {

	public String targetID;
	
	public String targetDescription;//目标描述
	
	public String targetVersion;//android版本号
	
	public String targetAndroidID;//android版本标示
	
	/**
	 * 目前的打包器暂不支持使用AddOn打包
	 */
	public String targetAddOn;//外置插件版
	
	
	private static Hashtable<Integer, AndroidTarget> systemTargets=new Hashtable<Integer, AndroidTarget>();//android sdk中包含的target列表
	
	private static Hashtable<String, Hashtable<Integer, AndroidTarget>> addOnTarget=new Hashtable<String, Hashtable<Integer,AndroidTarget>>();
	
	
	private static volatile boolean loadOk=false;
	
	//TODO 使用split ^-+重写这部分的target解析
	static{
		//do the static work
		CommandWrapper.INIT_PATH="E:\\SDK\\android-sdk-windows\\tools";
		CommandWrapper wrapper=CommandWrapper.getCommandWrapper();
		String echoContent=wrapper.doSychronizeCommand("android list");
		echoContent=echoContent.replaceAll("\n", "");
		Pattern pattern=Pattern.compile("id: (\\d+).+?(\\d+)(?=\").+?"
				+"Name: (.+?)\\s*Type.+?" 
					+"Skins.+?(?=id|Available)");
		//check the matcher
//		System.out.println(echoContent);
		Matcher matcher=pattern.matcher(echoContent);
		while(matcher.find()){
			AndroidTarget target=new AndroidTarget();
			target.targetID=matcher.group(1);
			target.targetAndroidID=matcher.group(2);
			
			String name=matcher.group(3);
			if(name.startsWith("Android"))
				target.targetVersion=name.split(" ")[1];
			else {
				target.targetAddOn=name;//add on
				AndroidTarget basedOnTarget=systemTargets.get(Integer.parseInt(target.targetID));
				if(basedOnTarget!=null)
					target.targetVersion=basedOnTarget.targetVersion;
			}
			target.targetDescription=matcher.group();
			if(target.targetAddOn==null)//platform
				systemTargets.put(Integer.parseInt(target.targetAndroidID), target);
			else {
				if(addOnTarget.get(target.targetAddOn)==null)
					addOnTarget.put(target.targetAddOn, new Hashtable<Integer, AndroidTarget>());
				try{
				addOnTarget.get(target.targetAddOn).put(Integer.parseInt(target.targetAndroidID), target);
				}catch(Exception ex){
					//ignore
				}
			}
			System.out.println(target);
		}
		loadOk=true;
		System.out.println("load android target complete,total load target:"+systemTargets.size());
//		System.out.println("load addon target complete,total load target:"+addOnTarget.size());
		wrapper.waitForExit();
		
	}
	
	/**
	 * 根据android平台id号返回一个非插件式平台列表
	 * @param id
	 * @return
	 */
	public static AndroidTarget getPlatformTarget(int platformID)
	{
		while(!loadOk) ;//同步等待
		
		return systemTargets.get(platformID);
	}
	
	/**
	 * 返回指定addOn名称的，指定基android平台id版本的平台。<br>
	 * 如google的addOnName是Google APIs
	 * @param addOnName
	 * @param platformID
	 * @return
	 */
	public static AndroidTarget getAddOnTarget(String addOnName,int platformID)
	{
		while(!loadOk) ;//同步等待
		
		Hashtable<Integer, AndroidTarget> oneAddOnTargets=addOnTarget.get(addOnName);
		
		if(oneAddOnTargets!=null)
			return oneAddOnTargets.get(platformID);
		else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AndroidTarget [targetID=" + targetID + ", targetDescription="
				+ targetDescription + ", targetVersion=" + targetVersion
				+ ", targetAndroidID=" + targetAndroidID + ", targetAddOn="
				+ targetAddOn + "]";
	}

	
	public static void main(String[] args) {
		
		System.out.println(getPlatformTarget(4));
		
		System.out.println(getAddOnTarget("Google APIs", 4));
		
		System.out.println("Android 1.5".split(" ")[1]);
		
		String test="id 1 abc abc2 abc3 id 2 def def2 def3 id 3 fgh1 fgh2 fgh3 id 4 ijk1 ijk2 ijk3";
		Pattern pattern=Pattern.compile("id (\\d) .+?(?=id|\\z)");//使用断言
		Matcher matcher=pattern.matcher(test);
		while(matcher.find()){
			System.out.println(matcher.group(1));
			System.out.println(matcher.group());
			
		}
	}



}
