package ui;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import build.BatchPack;

import model.PackRecords;

@SuppressWarnings("serial")
public class MacroPanel extends JPanel{
	
	JLabel[] macroNameLabels;
	JTextField[] macroTextValues;
	AutoCompleteExtender[] autoCompleteWrapper;
	boolean isShadow;//批量添加的时候。正在编辑的是Main级别，其它所有的MacroPanel都是shadow级别
	PackRecords records=PackRecords.loadRecords();
	BatchPack projectBatchPack;
	
	FileFilter validFileFilter=new FileFilter() {
		
		@Override
		public String getDescription() {
			return "android Valid Files";
		}
		
		@Override
		public boolean accept(File f) {
			if(f.isDirectory()) return true;
			if(f.getName().indexOf('.')==-1) return false;
			String fileName=f.getName().substring(0,f.getName().indexOf('.'));
			return fileName.matches("[a-z0-9_]+");
		}
	};
	
	private EditMacroListenner listener;
	
	
	/**
	 * 定义一个编辑宏的监听器
	 * @author 13leaf
	 *
	 */
	public interface EditMacroListenner 
	{
		/**
		 * 在发生编辑行为的时候调用。
		 * @param source 编辑的MacroPanel
		 * @param macroName 编辑的名称
		 * @param index 编辑的Macro的id号。可以通过id号访问控件列表
		 */
		void editMacro(MacroPanel source,String macroName,String editValue,int index);
	}
	
	public MacroPanel(BatchPack batchPack,String... macroNames)
	{
		projectBatchPack=batchPack;//XXX 不怎么好的写法
		setBorder(BorderFactory.createEtchedBorder());
		
		macroNameLabels=new JLabel[macroNames.length];
		macroTextValues=new JTextField[macroNames.length];
		autoCompleteWrapper=new AutoCompleteExtender[macroNames.length];
		setLayout(new GridLayout(0, 1));
		for(int i=0;i<macroNames.length;i++)
		{
			JPanel panel=new JPanel();
			final int index=i;
			final String macroName=macroNames[i];
			JLabel macroNameLabel=new JLabel(macroName);
			panel.add(macroNameLabel);
			panel.add(new JLabel("="));
			JTextField tempTextField;
			if(projectBatchPack.isExchangeMacro(macroName))
			{
				FileChooser mChooser=new FileChooser();
				mChooser.setFileFilter(validFileFilter);
				panel.add(mChooser);
				tempTextField=mChooser.getTextField();
			}else {
				tempTextField=new JTextField(20);
				panel.add(tempTextField);
			}
			final JTextField macroTextValue=tempTextField;
			//
			macroNameLabels[i]=macroNameLabel;
			macroTextValues[i]=macroTextValue;
			autoCompleteWrapper[i]=new AutoCompleteExtender(macroTextValue,records.getMacroRecords(macroName), null);
			autoCompleteWrapper[i].setSizeFitComponent();
			autoCompleteWrapper[i].setMaxVisibleRows(6);
			
			macroTextValue.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					super.keyReleased(e);
					//set current macro is not shadow
					isShadow=false;
					if(listener!=null){
						//call back
						listener.editMacro(MacroPanel.this, macroName, macroTextValue.getText(), index);
					}
				}
			});
			add(panel);
		}
	}
	
	
	public void setEditMacroListioner(EditMacroListenner listener)
	{
		this.listener=listener;
	}
	
	
	/**
	 * 取得MacroPanel当前的宏替换值
	 * @return
	 */
	public Map<String, String> getRunValue()
	{
		Map<String, String> value=new HashMap<String, String>();
		for(int i=0;i<macroNameLabels.length;i++)
		{
			if(macroTextValues[i].getText().trim().length()!=0)//跳过空字段
				value.put(macroNameLabels[i].getText(), macroTextValues[i].getText());
		}
		return value;
	}

	/**
	 * 同步影子设置
	 * @param macroNameIndex
	 * @param editValue
	 */
	public void shadowMacro(int macroNameIndex, String editValue) {
		macroTextValues[macroNameIndex].setText(editValue);
	}
}
