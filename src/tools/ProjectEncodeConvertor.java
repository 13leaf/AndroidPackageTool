package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class ProjectEncodeConvertor {

	private static FileCharsetDetector charsetDetector=new FileCharsetDetector();
	
	public static void main(String[] args) {
//		args=new String[]{"E:\\workSpace\\Trash\\src\\tool\\NumberVerificationCodeIdentifier.java"};
		
		args=new String[]{"E:\\workSpace_web\\MercuryAtmo\\WebContent\\olympic\\platform\\android"};
		
		if(args.length!=1){
			System.out.println("use this command: ProjectEncodeConvertor xxx\n" +
					"and then convert the project to utf8\n" +
					"example:java -jar ProjectEncodeConvertor projects\\AutoPackageDemo");
		}else {
			String projectPath=args[0];
			convertProjectEncode(new File(projectPath));
		}
//		detectProjectEncode(new File("E:\\workSpace\\AndroidPackageTool\\projects\\IfengOpenBook"), "UTF-8");
	}

	/**
	 * 递归调用,转换文件编码
	 * @param parentDir
	 */
	public static void convertProjectEncode(File parentDir) {
		if(parentDir.isFile())
		{
			convertFileEncode(parentDir);
			return;
		}
		for(File file : parentDir.listFiles())
		{
			if(file.isDirectory()) convertProjectEncode(file);
			else if(file.getName().endsWith(".java")){
				convertFileEncode(file);
			}
		}
	}
	
	/**
	 * 递归调用,转换文件编码
	 * @param parentDir
	 */
	public static void detectProjectEncode(File parentDir,String expect) {
		if(parentDir.isFile())
		{
			detectProjectEncode(parentDir,expect);
			return;
		}
		for(File file : parentDir.listFiles())
		{
			if(file.isDirectory()) detectProjectEncode(file,expect);
			else if(file.getName().endsWith(".java")){
				try {
					String charsetName=charsetDetector.guestFileEncoding(file,FileCharsetDetector.HINT_CHINESE);
					if(!charsetName.equals(expect))
						System.out.println(file.getName()+" -> "+charsetName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 自动识别读入编码，并转换文件编码至UTF8格式
	 * @param file
	 */
	public static void convertFileEncode(File file)
	{
		try {
			String charsetName=charsetDetector.guestFileEncoding(file,FileCharsetDetector.HINT_CHINESE);
			System.out.println("detect "+file.getName()+"'s encode is "+charsetName);
			String content=Files.readTextFile(file, true,Charset.forName(charsetName));
			Files.saveTextFile(file, content,Files.utf8Charset);
			System.out.println(file.getName()+"convert2Utf8 complete");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
