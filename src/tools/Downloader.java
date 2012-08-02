package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {

	private final String[] testUrls={
			"http://st1.qifuni.com/uploads/userup/29530/1311591031-1525.1",
			"http://st1.qifuni.com/uploads/userup/1/aiqinghuanying.1",
			"http://st1.qifuni.com/uploads/userup/31944/20110714233610.1",
			"http://st1.qifuni.com/uploads/userup/19208/20110714181832.1",
			"http://st1.qifuni.com/uploads/userup/1305/20110713150401.1",
			"http://st1.qifuni.com/uploads/userup/15172/1310532M6-O54.1",
			"http://st1.qifuni.com/uploads/userup/23178/20110711234616.1",
			"http://st1.qifuni.com/uploads/userup/31944/20110711221756.1",
			"http://st1.qifuni.com/uploads/userup/31944/131013X44-C24.1",
			"http://st1.qifuni.com/uploads/userup/11627/1310119394-B29.1",
			"http://st1.qifuni.com/uploads/userup/11627/131011Za-3541.1",
			"http://st1.qifuni.com/uploads/userup/11627/131011WZ-L33.1",
			"http://st1.qifuni.com/uploads/userup/11627/13101035V-1K6.1",
			"http://st1.qifuni.com/uploads/userup/11627/1310103157-1002.1",
			"http://st1.qifuni.com/uploads/userup/11627/1310102J8-A19.1",
			"http://st1.qifuni.com/uploads/userup/11627/1310102443-2220.1",
			"http://st1.qifuni.com/uploads/userup/31702/1310101502-9311.1",
			"http://st1.qifuni.com/uploads/userup/11627/13101021M-O54.1",
			"http://st1.qifuni.com/uploads/userup/1588/1310044946-2R4.1",
			"http://st1.qifuni.com/uploads/userup/26005/1310011315-21U.1",
			"http://st1.qifuni.com/uploads/userup/31702/131000G02-3218.1",
			"http://st1.qifuni.com/uploads/userup/29530/130bF622-62F.1",
			"http://st1.qifuni.com/uploads/userup/8222/130bB918-D94.1",
			"http://st1.qifuni.com/uploads/userup/31702/130b59E1-GO.1",
			"http://st1.qifuni.com/uploads/userup/29968/130b02405-1632.1",
			"http://st1.qifuni.com/uploads/userup/1/huangruorumeng.1",
			"http://st1.qifuni.com/uploads/userup/14275/130a560b-3A4.1",
			"http://st1.qifuni.com/uploads/userup/31702/1309E3302-1U7.1",
			"http://st1.qifuni.com/uploads/userup/30663/1309C55a-23B.1",
			"http://st1.qifuni.com/uploads/userup/30663/1309C3V4-1H3.1",
			"http://st1.qifuni.com/uploads/userup/1/kexibushishaungyuzuo.1",
			"http://st1.qifuni.com/uploads/userup/1/xingfushiguang.1",
			"http://st1.qifuni.com/uploads/userup/31702/1309B0241-2039.1",
			"http://st1.qifuni.com/uploads/userup/1/yigerendeshiguang.1",
			"http://st1.qifuni.com/uploads/userup/1/zheyichengqingshenyuanqian.1",
			"http://st1.qifuni.com/uploads/userup/1/beishiguangyidongdechengshi.1",
			"http://st1.qifuni.com/uploads/userup/1/jietuo.1",
			"http://st1.qifuni.com/uploads/userup/1/tianyi.1",
			"http://st1.qifuni.com/uploads/userup/1/gq-zuilangmandeshi.1",
			"http://st1.qifuni.com/uploads/userup/31702/1309525B9-O43.1",
			"http://st1.qifuni.com/uploads/userup/7221/1309520W0-5308.1",
			"http://st1.qifuni.com/uploads/userup/31702/13094V638-Y40.1",
			"http://st1.qifuni.com/uploads/userup/32561/13094O0W-9543.1",
			"http://st1.qifuni.com/uploads/userup/16498/13094459C-S10.1",
			"http://st1.qifuni.com/uploads/userup/11627/13093532O-51c.1",
			"http://st1.qifuni.com/uploads/userup/31702/1309224Y3-3915.1",
			"http://st1.qifuni.com/uploads/userup/31702/13091631M-ED.1",
			"http://st1.qifuni.com/uploads/userup/16316/1309163324-G38.1",
			"http://st1.qifuni.com/uploads/userup/18983/130Z925Y-J55.1"
	};
	
	private final String[] testLabels={
			"殇之轮回",
			"雨季花季，坦然走过。",
			"我想念，有你一起说说话的时候",
			"唯美，遇见",
			"莫扎特-d小调幻想曲",
			"青涩时光静谧年",
			"comtine d'un autre ete l'apr",
			"我想有颗心,勇敢而宁静",
			"我们是微茫的存在",
			"神马怎么办（加速版）",
			"神马都是我的",
			"梦幻进行曲",
			"没烟抽的女人",
			"呼吸记忆中的气息",
			"告诉我",
			"彷徨.",
			"心的距离",
			"不知道的明天",
			"菊次郎的夏天 久石让",
			"静音沁琴",
			"爱与被爱",
			"家乡的傍晚",
			"雨夜",
			"旧情绵绵",
			"我们的纪念",
			"恍若如梦",
			"落花成伤",
			"爱的缠绵",
			"永恒",
			"初秋，风落的声音",
			"时光，缓缓流淌",
			"被风吹过的夏天",
			"心的等待",
			"一个人的时光",
			"这一程，情深缘浅",
			"被时光移动的城市",
			"找个理由来解脱",
			"一切都是天意",
			"最浪漫的事",
			"所谓幸福",
			"The Crave (海上钢琴师插曲)",
			"爱如流云",
			"如果当时",
			"当爱离去",
			" 瑶族舞曲.",
			"一树梨花",
			"等一个人的咖啡",
			"简单的节奏 希望带给你 恬静的心情",
			"海上钢琴师插曲",
	};
	
	private final File downloadDir;
	
	public Downloader(String dir){
		downloadDir=new File(dir);
		if(!downloadDir.exists()) 
			downloadDir.mkdirs();
		
	}
	
	public void downFile(String title,String url){
		try {
			URLConnection conn=new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setAllowUserInteraction(false);
			conn.setConnectTimeout(5000);//5秒超时
			
			InputStream is=conn.getInputStream();
			File targetFile=new File(downloadDir,title);
			targetFile.createNewFile();
			FileOutputStream fos=new FileOutputStream(targetFile);
			int data=-1;
			while((data=is.read())!=-1){
				fos.write(data);
			}
			is.close();
			fos.close();
			System.out.println("download "+title+"complete!");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		Downloader downloader=new Downloader("D:\\musics");
//		for(int i=0;i<downloader.testUrls.length;i++){
//			downloader.downFile(
//					downloader.testLabels[i]+".mp3",
//					downloader.testUrls[i]);
//		}
		
		Downloader downloader=new Downloader("G:\\");
		downloader.downFile("xuanlvzhu.swf", "http://www.xuanlvzhu.com/woodplay/cmp.swf");
	}
}
