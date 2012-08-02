package model;
import java.util.Arrays;


public class AndroidProject {

	public String projectName;
	
	public String projectPath;
	
	public transient String[] libsPath;
	
	public String projectVersion;//程序版本号，通过AndroidManifest获得
	
	public String projectVersionCode;//程序版本子号，通过AndroidManifest获得
	
	public String projectEncode="";//程序的编码格式,此处设置空字符串，让其可存入Attributes
	
	public boolean library;//是否为库项目
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AndroidProject [projectName=" + projectName + ", projectPath="
				+ projectPath + ", libsPath=" + Arrays.toString(libsPath)
				+ ", projectVersion=" + projectVersion
				+ ", projectVersionCode=" + projectVersionCode + "]";
	}
}
