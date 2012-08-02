package model;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;


/**
 * 属性封装
 * @author 13leaf
 *
 */
public class Attributes extends Properties{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1189435565113013203L;
	
	public void setAndroidTarget(AndroidTarget target)
	{
		parseObject2Map(target);
	}
	
	public void setKeyStore(KeyStore keyStore)
	{
		parseObject2Map(keyStore);
	}
	
	public void setAndroidProject(AndroidProject project)
	{
		parseObject2Map(project);
	}
	
	public AndroidTarget getAndroidTarget()
	{
		return (AndroidTarget) getObject4Map(AndroidTarget.class);
	}
	
	public AndroidProject getAndroidProject()
	{
		return (AndroidProject) getObject4Map(AndroidProject.class);
	}
	
	public KeyStore getKeyStore()
	{
		return (KeyStore) getObject4Map(KeyStore.class);
	}
	
	@Override
	public synchronized Object put(Object key, Object value) {
		
		Object testValue=get(key);
		if(testValue!=null && !testValue.equals(value))
			System.err.println("警告:键("+key+")产生冲突:"+testValue+"  将被覆写为  "+value);
		
		return super.put(key, value);
	}
	
	
	/**
	 * 将顶级属性push到Properties中。只检索String类型
	 * @param object
	 */
	public void parseObject2Map(Object object)
	{
		final Class<?> clazz=object.getClass();
		for(Field field : clazz.getDeclaredFields())
		{
			field.setAccessible(true);
			try {
				Object value=field.get(object);
				if(value!=null && field.getType()==String.class)
				{
					put(field.getName(), field.get(object));
				}
			} catch (IllegalArgumentException e) {
				//ignore
			} catch (IllegalAccessException e) {
				//ignore
			}
		}
	}
	
	/**
	 * 从Attributes属性中构建一个指定class的Javabean对象。仅设置顶级属性
	 * @param clazz
	 */
	public Object getObject4Map(Class<?> clazz)
	{
		Object newInstance=null;
		try {
			newInstance=clazz.getConstructors()[0].newInstance();
		} catch (Throwable e) {
			//ignore
		}
		if(newInstance==null) return null;
		
		for(Field field: clazz.getDeclaredFields())
		{
			field.setAccessible(true);
			Object value=get(field.getName());
			try {
				field.set(newInstance,value);
			}catch(Throwable throwable){
				//ignore
			}
		}
		return newInstance;
	}
	
	public static void main(String[] args) {
		AndroidProject project=new AndroidProject();
		project.projectName="testProject";
		project.projectPath="E:\\testProject";
		
		AndroidTarget target=AndroidTarget.getPlatformTarget(10);
		
		KeyStore keyStore=KeyStore.getTestKeyStore();
		
		Attributes attributes=new Attributes();
		attributes.setAndroidProject(project);
		attributes.setAndroidTarget(target);
		attributes.setKeyStore(keyStore);
		
		
		System.out.println(attributes.getKeyStore());
		System.out.println(attributes.getAndroidProject());
		System.out.println(attributes.getAndroidTarget());
		
		System.out.println(attributes);
		
		try {
			attributes.store(new FileWriter(new File("E:\\testAttributes.txt")), "this is test");
			attributes.storeToXML(new FileOutputStream(new File("E:\\testAttributes.xml")), "this is xml test");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
