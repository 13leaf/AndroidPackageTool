package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * 
 * @author 13leaf
 * 
 */
public class Files {

	public static Charset utf8Charset = Charset.forName("UTF-8");

	public static Charset defaultCharset = Charset.defaultCharset();

	/**
	 * 强行删除一个目录，包括这个目录下所有的子目录和文件
	 * 
	 * @param dir
	 *            目录
	 * @return 是否删除成功
	 */
	public static boolean deleteDir(File dir) {
		if (null == dir || !dir.exists())
			return false;
		if (!dir.isDirectory())
			throw new RuntimeException("\"" + dir.getAbsolutePath()
					+ "\" should be a directory!");
		File[] files = dir.listFiles();
		boolean re = false;
		if (null != files) {
			if (files.length == 0)
				return dir.delete();
			for (File f : files) {
				if (f.isDirectory())
					re |= deleteDir(f);
				else
					re |= f.delete();
			}
			re |= dir.delete();
		}
		return re;
	}
	
	/**
	 * 清空一个文件下的所有内容,但不删除目录
	 * @param dir
	 * @return 是否清除成功
	 */
	public static boolean clearDir(File dir)
	{
		return deleteDir(dir) && dir.mkdir();
	}

	/**
	 * 读取一个指定编码格式的文本文件。若设置wrapLine为false，则读取的String将去除换行符。
	 * 
	 * @param file
	 * @param 是否包含换行符
	 * @return
	 */
	public static String readTextFile(File file, boolean wrapLine,
			Charset charset) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), charset));
			String line = reader.readLine();
			do {
				sb.append(line);
				if (wrapLine)
					sb.append("\r\n");
				line = reader.readLine();
			} while (line != null);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 使用自动侦测的默认格式读取文件
	 * 
	 * @param file
	 * @param wrapLine
	 * @return
	 */
	public static String readTextFile(File file, boolean wrapLine) {
		//
		return readTextFile(file, wrapLine, defaultCharset);
	}

	/**
	 * 拷贝一个文件
	 * 
	 * @param src
	 * @param destination
	 */
	public static boolean copyFile(File src, File destination) {
		try {
			if(src.getCanonicalPath().equals(destination.getCanonicalPath()))
				return true;
			if (!destination.exists())
				destination.createNewFile();
			FileInputStream inputStream = new FileInputStream(src);
			FileOutputStream outputStream = new FileOutputStream(destination);
			int data = 0;
			while ((data = inputStream.read()) != -1) {
				outputStream.write(data);
			}
			inputStream.close();
			outputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 读取一个配置文件。(去除换行符),进行正则匹配
	 * 
	 * @param file
	 * @param regex
	 * @return
	 */
	public static Matcher parseFile(File file, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(readTextFile(file, false));
	}

	/**
	 * 重命名一个文件。若重命名失败将返回null
	 * 
	 * @param file
	 * @param newName
	 * @return 返回重命名后的文件
	 * @return
	 */
	public static File renameFile(File file, String newName) {
		String oldName = file.getName();
		int endId = file.getAbsolutePath().lastIndexOf(oldName);
		String newPath = file.getAbsolutePath().substring(0, endId) + newName;
		File dest = new File(newPath);
		if (dest.exists())
			System.out.println(dest.delete() ? "delete old file"
					: "delete old file fail" + dest.getAbsolutePath());
		boolean success = file.renameTo(dest);
		if (success)
			return dest;
		else
			return null;
	}

	/**
	 * 保存文本文件，以指定格式来保存。
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean saveTextFile(File file, String content,
			Charset charset) {
		try {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(file), charset));
			out.print(content);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 保存文件，以默认的文件编码来保存
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean saveTextFile(File file, String content) {
		return saveTextFile(file, content, defaultCharset);
	}

	/**
	 * 判断一个路径是否是绝对路径
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isAbsolutePath(String path) {
		File file = new File(path);
		return file.isAbsolute();
	}

	/**
	 * 若该路径是绝对路径，则直接返回。否则以parentPath作为父文件夹，获得绝对路径
	 * 
	 * @param parentPath
	 * @param relative
	 * @return
	 */
	public static String convert2AbsolutePath(String parentPath, String relative) {
		if (isAbsolutePath(relative))
			return relative;
		if ("".equals(parentPath))
			parentPath = new File("").getAbsolutePath();
		File file = new File(parentPath, relative);
		return file.getAbsolutePath();
	}

	public static Object loadObject(String filePath) throws IOException,
			ClassNotFoundException {
		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				filePath));
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}

	/**
	 * 从起始文件到达目标文件的相对路径。<br>
	 * 若两个文件不在一个驱动器上则将抛出异常。
	 * 
	 * @param original
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public static String toRelativePath(File original, File target)
			throws IOException {
		String relativePath = "";
		String originalPath = original.getCanonicalPath();
		String targetPath = target.getCanonicalPath();
		String publicDir = getParentDir(originalPath, targetPath);
		
		if(null==publicDir) throw new IOException(String.format("original:%s\ntarget:%s have no public directory!", originalPath,targetPath));
		if(originalPath.equals(publicDir)) return "."+targetPath.substring(publicDir.length(), targetPath.length());
		relativePath=toParentRelative(originalPath,publicDir);
		return "."+relativePath+targetPath.substring(publicDir.length(),targetPath.length());
	}
	
	
	private static String toParentRelative(String originalPath, String parentPath) {
		String relative=originalPath.substring(parentPath.length(),originalPath.length());
		return relative.replaceAll("[^\\\\]+", "..");
	}

	/**
	 * 返回两个路径的公共路径部分。若没有公共路径部分,则返回null
	 * @param path1
	 * @param path2
	 * @return
	 * @throws IOException
	 */
	public static String getParentDir(String path1, String path2)
			throws IOException {
		if (path1.contains(".") || path2.contains(".")) {// not a canonicalPath
			path1 = new File(path1).getCanonicalPath();
			path2 = new File(path2).getCanonicalPath();
		}
		char[] originalChars = path1.toCharArray();
		char[] targetChars = path2.toCharArray();
		int i = -1;
		while (++i < originalChars.length && i < targetChars.length) {// to get the latest both public start
			if (originalChars[i] != targetChars[i]) {
				--i;// back
				break;
			}
		}
		if(i==-1) return null;
		else return path2.substring(0, i);
	}

	public static void saveObject(String filePath, Serializable serializable)
			throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				filePath));
		oos.writeObject(serializable);
		oos.close();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new File("E:\\workspace").getName());
//		System.out.println(toParentRelative("E:\\workspace\\abc", "E:\\"));
		// String[] files=new
//		 File("E:\\workSpace\\IfengNewsForPhoneFixed\\..\\..\\workSpace_advance\\IfengLibrary").list();
//		 File file=new
//				 File("E:\\workSpace\\IfengNewsForPhoneFixed\\..\\..\\workSpace_advance\\IfengLibrary");
		// System.out.println(file.isAbsolute());
		// System.out.println(Arrays.toString(files));
		// System.out.println(convert2AbsolutePath(new
		// File("").getAbsolutePath(), "depends/jdk"));
		// System.out.println(Files.copyFile(new File("E:\\abc.txt"), new
		// File("E:\\study\\abc.txt")));//oo
		// System.out.println(Files.renameFile(new File("E:\\abc.txt"),
		// "cde.txt"));//oo
		/*
		 * String regExp="classpathentry\\s+" + "kind\\s*=\\s*\"lib\"\\s+" +
		 * "path\\s*=\\s*\"(.+?)\""; Matcher matcher=parseFile(new
		 * File("E:\\workSpace\\IfengVideo3_SVN\\.classpath"), regExp);
		 * while(matcher.find()) System.out.println(matcher.group(1));
		 */// oo

		// System.out.println(Files.readTextFile(new
		// File("E:\\workSpace\\IfengVideo3_SVN\\AndroidManifest.xml"),
		// true));//oo

		// System.out.println(Files.saveTextFile(new File("E:\\abc.txt"),
		// "测试一下"));//oo

		// System.out.println(Files.isAbsolutePath("E:\\abc.txt"));//oo
		// System.out.println(Files.isAbsolutePath("abc"));//oo
	}
}
