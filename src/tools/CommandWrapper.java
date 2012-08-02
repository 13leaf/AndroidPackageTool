package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import model.Attributes;

/**
 * 简单粗暴的Cmd进程包装。未实现命令执行结果的回调机制
 * @author 13leaf
 * 
 */
public class CommandWrapper {

	private Process cmd;

	private PrintWriter out;

	public static final String CMD_COMMAND="cmd";
	
	public final String COMMAND_END_FlAG="CommandWrapper's command End!"+Math.random();//利用回显来实现命令的同步
	
	private volatile boolean sychronizeEnd=false;//默认关闭
	
	private StringBuffer cmdOutBuffer=new StringBuffer();//执行命令后的输出缓冲区
	
	/**
	 * 初始化配置path
	 */
	public static String INIT_PATH="";
	
	/**
	 * 获得一个CommandWrapper，并添加path到环境变量
	 * @param INIT_PATH
	 * @return
	 */
	public static CommandWrapper getCommandWrapper()
	{
		try {
			Process cmdProcess=
					new ProcessBuilder(CMD_COMMAND).start();
			CommandWrapper wrapper=new CommandWrapper(cmdProcess);
			if(INIT_PATH!=null)
			{
				wrapper.doCommand("set path=%path%;"+INIT_PATH);
			}
			return wrapper;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 启动并绑定Command进程
	 * @param cmdProcess
	 */
	public CommandWrapper(Process cmdProcess) {
		this.cmd = cmdProcess;

		try {
			out = new PrintWriter(new OutputStreamWriter(cmd.getOutputStream(),"gbk"), true);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// show console
		new StreamGobbler(cmd.getInputStream(), "out",this).start();
		new StreamGobbler(cmd.getErrorStream(), "error",this).start();
	}
	
	/**
	 * 获得命令进程
	 * @return
	 */
	public Process getCommandProcess()
	{
		return cmd;
	}

	/**
	 * 执行多条命令
	 * 
	 * @param commands
	 */
	public void doCommands(String[] commands) {
		for (String command : commands) {
			doCommand(command);
		}
	}
	
	/**
	 * 执行一条命令。在命令未执行完毕时将一直等待
	 * @param command
	 */
	public String doSychronizeCommand(String command)
	{
		sychronizeEnd=false;
		doCommand(command+"&echo "+COMMAND_END_FlAG);
		while(!sychronizeEnd) ;//同步等待
		return cmdOutBuffer.toString();
	}
	
	/**
	 * 获取当前执行命令所在的路径
	 * @return
	 */
	public String getCurrentPath()
	{
		return doSychronizeCommand("cd").trim();//去除最后的\n
	}
	
	/**
	 * 执行一条命令。在命令未执行完毕时将一直等待
	 * @param command
	 * @param attributes
	 * @return
	 */
	public String doSychronizeCommand(String templateExp,Attributes attributes)
	{
		return doSychronizeCommand(TemplateParser.parseTemplate(templateExp, attributes));
	}

	/**
	 * 执行一行命令
	 * 
	 * @param command
	 */
	public void doCommand(String command) {
		out.println(command);// auto flush
	}

	/**
	 * 解析命令模板并调用
	 * 
	 * @param templateExp
	 * @param attributes
	 */
	public void doCommand(String templateExp, Attributes attributes) {
		doCommand(TemplateParser.parseTemplate(templateExp, attributes));
	}
	
	/**
	 * 上锁，等待进程同步退出后继续。调用该方法后等于废弃了Command。
	 * @return
	 */
	public int waitForExit()
	{
		out.println("exit");
		try {
			int value=cmd.waitFor();
			cmd.destroy();
			return value;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * 
	 * @author 13leaf
	 * 
	 */
	class StreamGobbler extends Thread {
		InputStream is;
		String type;
		CommandWrapper wrapper;
		
		StreamGobbler(InputStream is, String type,CommandWrapper wrapper) {
			this.is = is;
			this.type = type;
			this.wrapper=wrapper;
		}
		
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is,"gbk");
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				boolean sychronizeCommanding=false;
				while ((line = br.readLine()) != null)
				{
					if(!sychronizeCommanding 
							&& line.endsWith("echo "+COMMAND_END_FlAG))//同步命令开始
					{
						System.err.println("get sychronize command");
						sychronizeCommanding=true;
						cmdOutBuffer=new StringBuffer();//丢弃原先缓冲区
					}else if(line.endsWith(wrapper.COMMAND_END_FlAG)) {//执行结束
						wrapper.sychronizeEnd=true;
						sychronizeCommanding=false;//结束同步检查命令
					}
					else {
						System.out.println(type + ">" + line);
						if(sychronizeCommanding)
							cmdOutBuffer.append(line+"\n");//将内容添加到缓冲区
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		CommandWrapper.INIT_PATH="depends/android";
		CommandWrapper wrapper=getCommandWrapper();
		wrapper.doCommand("java -version");
//		wrapper.doCommand("notepad");
//		wrapper.doCommand("ping 127.0.0.1 -n 5");
//		wrapper.doCommand("ping 127.0.0.1 -n 5");
//		String echoContent=wrapper.doSychronizeCommand("ping 127.0.0.1 -n 5");
//		System.out.println("this is echo content:\n"+echoContent);
//		System.err.println("sychronized command?");
//		wrapper.doCommand("echo hello");
		wrapper.doCommand("cd /d E:\\workSpace_advance\\凤凰开卷\\IfengOpenBook");
		wrapper.waitForExit();
		System.err.println("synchronized end?");
	}

}
