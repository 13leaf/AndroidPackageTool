package tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AndroidTarget;

import build.BatchPack;
import build.BatchPack.ConfigError;

/**
 * 验证新配置的Project是否可行
 * @author 13leaf
 *
 */
public class Validator {

	public static void main(String[] args) {
		args=new String[]{"E:\\workSpace_advance\\AndroidPackageTool\\projects\\奥运新闻.project",""};
		System.out.println("check "+Arrays.toString(args));
		if(args.length==0)
		{
			System.err.println("Expect ProjectPath arg");
			return;
		}
		List<Map<String,String>> runValues=null;
		if(args.length>1)
		{
			runValues=new ArrayList<Map<String,String>>();
			String[] keyValues=args[1].split(",");
			HashMap<String, String> map=new HashMap<String, String>();
			for(String aKeyValue : keyValues)
			{
				String[] ss=aKeyValue.split(":");
				if(ss.length!=2) continue;
				map.put(ss[0], ss[1]);
			}
			runValues.add(map);
			
		}
		
		//ensure target exists
		try {
			Class.forName("model.AndroidTarget");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		String projectPath=args[0];
		try {
			BatchPack pack=new BatchPack(projectPath);
			pack.checkModifyAble();
			if(pack.getAttributes().getProperty("projectUrl")!=null)
			{
				//check reach svnAble
				if(!(pack.getSVNVersion()>0))
				{
					System.err.println("UnReachable svn projectUrl,Please go check out");
					System.exit(-1);
				}
			}
			String targetId=pack.getAttributes().getProperty("targetAndroidID");
			if(targetId==null || AndroidTarget.getPlatformTarget(Integer.parseInt(targetId))==null){
				System.err.println("Android "+targetId+" Target do not exists!Please go check out!");;
				System.exit(-1);
			}
			System.out.println("validate no problem");
			if(runValues!=null && runValues.size()!=0)
			{
				pack.doBatch(runValues);
				pack.setOnBatchListener(new BatchPack.onBatchListener() {
					
					@Override
					public void onTaskSuccess(BatchPack batch, Map<String, String> runValue) {
						System.out.println("release no problem");
					}
					
					@Override
					public void onTaskFail(BatchPack batch, Map<String, String> runValue) {
						System.err.println("release has problem,please do checkout");
					}
				});
				pack.doBatch(runValues);
			}
		} catch (ConfigError error) {
			error.printStackTrace();
			System.err.println("config error!check con");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ModifyValidate Error!");
		}
		
		System.exit(0);
		
	}
}
