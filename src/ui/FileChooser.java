package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * 具备如下基本功能。<br>
 * 点击打开按钮选择要替换的文件。
 * @author 13leaf
 *
 */
public class FileChooser extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6755555406106572604L;
	
	JTextField filePath=new JTextField(20);
	JButton chooseButton=new JButton("打开");
	JFileChooser chooser=new JFileChooser(".");
	
	public FileChooser()
	{
		filePath.setEditable(false);//disable edit
		
		add(filePath);
		add(chooseButton);
		
		chooseButton.addActionListener(this);
	}
	
	public void setFileFilter(FileFilter filter)
	{
		chooser.setFileFilter(filter);
	}
	
	/**
	 * 获得选中文件的完整路径名称。若没有选中任何文件,则将返回空字符串
	 * @return
	 */
	public String getSelectedFile()
	{
		return filePath.getText();
	}
	
	public JTextField getTextField()
	{
		return filePath;
	}
	
	/**
	 * 是否选择了文件。
	 * @return
	 */
	public boolean hasSelectedFile()
	{
		return filePath.getText().length()!=0;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int returnVal=chooser.showOpenDialog(this);
		if(returnVal==JFileChooser.APPROVE_OPTION)
		{
			filePath.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
	
	public static void main(String[] args) {
		final JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400,400);
		frame.setVisible(true);
		final FileChooser mChooser=new FileChooser();
		frame.add(mChooser,BorderLayout.CENTER);
		JButton testButton=new JButton("test");
		frame.add(testButton,BorderLayout.SOUTH);
		frame.pack();
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(mChooser.getSelectedFile().length());
				JOptionPane.showMessageDialog(frame, mChooser.getSelectedFile());
			}
		});
	}
}
