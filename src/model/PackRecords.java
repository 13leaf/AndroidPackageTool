package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tools.Files;

public class PackRecords implements Serializable {

	private PackRecords() {

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 228906698830325820L;

	private HashMap<String, LinkedList<String>> macroIDS = new HashMap<String, LinkedList<String>>();
	
	private boolean wantBatch;
	
	private boolean selectAll;
	
	private String latestSearch;
	
	private HashMap<String, Integer> projectVersions=new HashMap<String, Integer>();
	
	public static final String CONFIG_PROJECT_ID="ProjectConfiguration"+serialVersionUID;
	
	public void addMacro(String macroID, String value) {
		if (isEmpty(macroID) || isEmpty(value))
			return;
		if (macroIDS.get(macroID) == null) {
			macroIDS.put(macroID, new LinkedList<String>());
		}
		LinkedList<String> values = macroIDS.get(macroID);
		if (!values.contains(value)) {
			values.add(value);
		}
	}

	public void addMacroList(List<Map<String, String>> runValues) {
		for (Map<String, String> value : runValues) {
			for (String key : value.keySet()) {
				addMacro(key, value.get(key));
			}
		}
	}
	
	/**
	 * ID为project配置文件的绝对路径
	 * @param projectID
	 * @return -1表示没有记录
	 */
	public int getProjectVersion(String projectID)
	{
		if(projectVersions==null) projectVersions=new HashMap<String, Integer>();
		if(projectVersions.containsKey(projectID)){
			return projectVersions.get(projectID);
		}
		return -1;
	}
	
	/**
	 * Id为project配置文件的绝对路径
	 * @param projectID
	 * @param version
	 */
	public void setProjectVersion(String projectID,int version)
	{
		if(projectVersions==null) projectVersions=new HashMap<String, Integer>();
		if(projectVersions.containsKey(projectID)){
			System.err.println("覆盖"+projectID+"版本从:"+projectVersions.get(projectID)+"至"+version);
		}
		projectVersions.put(projectID, version);
	}

	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public String[] getMacroRecords(String macroID) {
		if (macroIDS.containsKey(macroID)) {
			return macroIDS.get(macroID).toArray(new String[macroIDS.get(macroID).size()]);
		} else {
			return new String[] {};
		}
	}
	
	public void savePreference(boolean wantBatch,boolean selectAll,String latestSearch)
	{
		this.wantBatch=wantBatch;
		this.selectAll=selectAll;
		this.latestSearch=latestSearch;
		saveRecords();
	}
	
	public boolean isWantBatch() {
		return wantBatch;
	}
	
	public boolean isSelectAll() {
		return selectAll;
	}
	
	public String getLatestSearch() {
		return latestSearch;
	}
	
	public static final String RECORD_PATH = "records";

	private static PackRecords instance;

	public static synchronized PackRecords loadRecords(String filePath) {
		if (instance == null) {
			File recordFile = new File(filePath);
			if (recordFile.exists()) {
				try {
					return (PackRecords) Files.loadObject(recordFile
							.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			instance=new PackRecords();
		}
		return instance;
	}
	
	public static synchronized PackRecords loadRecords()
	{
		return loadRecords(RECORD_PATH);
	}

	public void saveRecords() {
		try {
			Files.saveObject(new File(RECORD_PATH).getAbsolutePath(), this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		PackRecords records=PackRecords.loadRecords("D:\\PackTool4Test\\records");
		System.out.println(records);
	}

	@Override
	public String toString() {
		return "PackRecords [macroIDS=" + macroIDS + ", wantBatch=" + wantBatch
				+ ", selectAll=" + selectAll + ", latestSearch=" + latestSearch
				+ ", projectVersions=" + projectVersions + "]";
	}
}
