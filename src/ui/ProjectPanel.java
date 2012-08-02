package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.PackRecords;

import ui.MacroPanel.EditMacroListenner;

import build.BatchPack;

@SuppressWarnings("serial")
public class ProjectPanel extends JPanel{
	
	JCheckBox wantBatch=new JCheckBox();//勾选框，不选中则取消生成
	
	JLabel projectLabel=new JLabel();
	
	JButton buttonAdd=new JButton("+");//add macro
	
	BatchPack projectBatch;
	
	List<MacroPanel> macroPanels=new ArrayList<MacroPanel>();
	
	List<JButton> removeMacroButtons=new ArrayList<JButton>();
	
	JButton updateButton=new JButton("更新");
	
	Box contentBox=Box.createVerticalBox();//内容以纵向进行布局
	
	String[] macroNames;
	
	PackRecords records=PackRecords.loadRecords();
	
	boolean isShadow;
	
	private EditProjectListener listener;
	
	private ProgressDialog dialog=new ProgressDialog((Frame) getParent());
	
	private JLabel statusLabel=new JLabel();
	
	/**
	 * 对Macro编辑进行的Project级别监听器封装
	 * @author 13leaf
	 *
	 */
	public interface EditProjectListener{
		/**
		 * 编辑Project中的Macro时调用。
		 * @param projectSource 编辑行为所在的ProjectPanel实例
		 * @param macroSource 编辑行为所在的MacroPanel实例
		 * @param macroPanelIndex 该MacroPanel在ProjectPanel中的visibleID。(从1开始)
		 * @param macroNameIndex 产生编辑行为的MacroPanel中的对应macroID(从0开始)
		 */
		void editProject(ProjectPanel projectSource,MacroPanel macroSource,String editValue,int macroPanelIndex,int macroNameIndex);
	}
	
	/**
	 * 此处的projectName特指projects目录下的*.project。如test.project时，该处显示名称为test
	 * @param batchPack
	 * @param projectName
	 */
	public ProjectPanel(BatchPack batchPack,final String projectName)
	{
		projectBatch=batchPack;
		
		projectLabel.setText(projectName);
		
		
		setLayout(new BorderLayout());
		//init macroNames
		LinkedList<String> macroNameList=projectBatch.getMacros();
		macroNames=macroNameList.toArray(new String[macroNameList.size()]);
		
		JPanel titleBar=new JPanel();
		FlowLayout layoutManager=(FlowLayout) titleBar.getLayout();
		layoutManager.setAlignment(FlowLayout.LEFT);
		titleBar.add(statusLabel);ensureStatus();
		titleBar.add(updateButton);updateButton.setEnabled(projectBatch.updateAble());
		if(!projectBatch.isLibrary()){
			titleBar.add(wantBatch);wantBatch.setSelected(true);
		}
		titleBar.add(projectLabel);
		if(!projectBatch.isLibrary())
			titleBar.add(buttonAdd);
		add(titleBar,BorderLayout.NORTH);
		
		if(!projectBatch.isLibrary()){
			//init ui
			contentBox.setBorder(BorderFactory.createTitledBorder("批处理-"+projectName));
			add(contentBox,BorderLayout.CENTER);
		}
		
		if(projectBatch.isLibrary())//decorate border
		{
			setBorder(BorderFactory.createTitledBorder("库项目-"+projectName));
		}
		
		
//		addMacroPanel();
		init();
		//set listener
		buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addMacroPanel();
			}
		});
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.showProgressDialog("正在同步导入"+projectName+",请稍后...");
				new Thread(){
					public void run() {
						projectBatch.importFromSVN(true);
						records.setProjectVersion(projectBatch.getProjectConfigPath(), projectBatch.getSVNVersion());
						records.saveRecords();
						ensureStatus();
						dialog.dismissProgressDialog();
						PackLauncher.prepareProjectPanel(ProjectPanel.this);
					}
				}.start();
			}
		});
	}
	
	
	private void ensureStatus() {
		if(projectBatch.updateAble())
		{
			int version=records.getProjectVersion(projectBatch.getProjectConfigPath());
			if(version!=projectBatch.getSVNVersion())
			{
				statusLabel.setText(String.format("需更新(%s-%s)",version,projectBatch.getSVNVersion()));
			}else {
				statusLabel.setText("已最新("+version+")");
			}
		}else {
			statusLabel.setText("未同步");
		}
	}



	public void setEditProjectListener(EditProjectListener listener)
	{
		this.listener=listener;
	}
	
	/**
	 * get ProjectName
	 * @return
	 */
	public String getProjectName()
	{
		return projectLabel.getText();
	}
	
	/**
	 * 初始化
	 */
	public void init()
	{
		contentBox.removeAll();
		macroPanels.clear();
		removeMacroButtons.clear();
		addMacroPanel();
	}
	
	/**
	 * 是否选中要做Batch
	 * @return
	 */
	public boolean wantBatch()
	{
		return wantBatch.isSelected();
	}
	
	public BatchPack getProjectBatch()
	{
		return projectBatch;
	}

	/**
	 * 添加一个宏定义模板
	 * @return 返回添加的Panel
	 */
	private MacroPanel addMacroPanel() {
		MacroPanel macroPanel=new MacroPanel(projectBatch,macroNames);
		JButton removeButton=new JButton("-");
		contentBox.add(removeButton);
		contentBox.add(macroPanel);
		
		macroPanels.add(macroPanel);
		removeMacroButtons.add(removeButton);
		
		final int id=macroPanels.size()-1;
		//超懒的删除实现。。。就这样吧 = =!
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMacroButtons.get(id).setVisible(false);
				macroPanels.get(id).setVisible(false);
			}
		});
		macroPanel.updateUI();
		macroPanel.setEditMacroListioner(new EditMacroListenner() {
			
			@Override
			public void editMacro(MacroPanel source, String macroName,String editValue, int index) {
				if(listener!=null && ProjectPanel.this.wantBatch()){
					isShadow=false;//本实例的ProjectPanel处于编辑状态
					//刷新本Project内部的所有其它MacroPanel为shadow
					for(MacroPanel macroPanel : macroPanels)
						macroPanel.isShadow=true;
					source.isShadow=false;//仅标志本实例为非影子
					listener.editProject(ProjectPanel.this, source,editValue,
							findMacroPanelVisibleId(source), index);
				}
			}
		});
		return macroPanel;
//		repaint(getBounds());
		//how to notify repaint??
		
	}
	
	/**
	 * 返回可见位置号的MacroPanel.id从1开始。若visibleID小于最小的ID号，则尝试自动添加到指定ID号并返回
	 * @param visibleID
	 * @return
	 */
	public MacroPanel getMacroPanel(int visibleID)
	{
		int visibleCount=0;
		for(MacroPanel panel : macroPanels)
		{
			if(panel.isVisible()) visibleCount++;
			if(visibleCount==visibleID) return panel;
		}
		
		MacroPanel newMacroPanel=null;
		while(visibleCount<visibleID) {
			newMacroPanel=addMacroPanel();
			visibleCount++;
		}
		//not found
		return newMacroPanel;
	}
	
	/**
	 * 返回查找指定MacroPanel实例在ProjectPanel中的可见位置
	 * @param macroPanel
	 * @return 返回0表示查找失败。否则返回指定的id值
	 */
	public int findMacroPanelVisibleId(MacroPanel macroPanel)
	{
		int visibleCount=0;
		for(MacroPanel panel : macroPanels){
			if(panel.isVisible()) {
				visibleCount++;
				if(macroPanel==panel) return visibleCount;
			}
		}
		return visibleCount;
	}
	
	/**
	 * 获取批处理的宏替换列表
	 * @return
	 */
	public List<Map<String, String>> getRunValues()
	{
		List<Map<String, String>> list=new ArrayList<Map<String,String>>();
		
		for(MacroPanel panel : macroPanels)
		{
			if(panel.isVisible())//不Visible的即被删除的。。很懒吧?
			{
				Map<String, String> runValue=panel.getRunValue();
				if(runValue.size()!=0)//仅取非空字段
					list.add(runValue);
			}
		}
		return list;
	}

	/**
	 * 同步影子的宏设置
	 * @param macroPanelIndex
	 * @param macroNameIndex
	 * @param editValue
	 */
	public void shadowMacro(int macroPanelIndex, int macroNameIndex,
			String editValue) {
		
		MacroPanel shadowMacroPanel=getMacroPanel(macroPanelIndex);
		shadowMacroPanel.shadowMacro(macroNameIndex,editValue);
	}
}
