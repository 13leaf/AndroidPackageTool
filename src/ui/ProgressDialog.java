package ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog {

	JProgressBar progressBar=new JProgressBar();
	
	public ProgressDialog(Frame owner) {
		super(owner,true);
		setTitle("正在载入... ");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLocation(400, 400);
		setSize(400, 110);
		
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		add(progressBar,BorderLayout.CENTER);
		setVisible(false);
	}
	
	public void showProgressDialog(final String s)
	{
		new Thread(){
			public void run() {
				progressBar.setString(s);
				setVisible(true);				
			}
		}.start();
	}
	
	public void dismissProgressDialog()
	{
		setVisible(false);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2742562728413808691L;
	
	public static void main(String[] args) throws InterruptedException {
		final JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400,400);
		frame.setVisible(true);
		JButton button=new JButton("click me");
		frame.add(button);
		final ProgressDialog dialog=new ProgressDialog(frame);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.showProgressDialog("正在载入,请等待...");
				
				new Thread(){
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						dialog.dismissProgressDialog();
					}
				}.start();
			}
		});
		//TODO fixProgress
	}
}
