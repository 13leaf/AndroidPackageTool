package model;

/**
 * 签名keyStore
 * @author 13leaf
 *
 */
public class KeyStore {

	public String keyPath;//签名文件路径
	
	public String keyPass;//私有密码
	
	public String storePass;//存储密码
	
	public String keyAlias;//别名

	public static KeyStore getTestKeyStore()
	{
		KeyStore keyStore=new KeyStore();
		return keyStore;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KeyStore [path=" + keyPath + ", keyPass=" + keyPass
				+ ", storePass=" + storePass + ", alias=" + keyAlias + "]";
	}
	
}
