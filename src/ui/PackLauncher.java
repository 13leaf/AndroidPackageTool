package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import tools.Files;
import tools.SVNHelper;

import model.AndroidProject;
import model.Attributes;
import model.PackRecords;

import build.BatchPack;
import build.PackManager;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

@SuppressWarnings("serial")
public class PackLauncher extends JFrame implements ActionListener {

	List<ProjectPanel> projectPanels = new ArrayList<ProjectPanel>();

	JScrollPane contentPane;

	JTextField quickFilter = new JTextField("");

	Box contentBox = Box.createVerticalBox();

	JProgressBar progressBar = new JProgressBar();

	JButton batchButton = new JButton("生成");

	JCheckBox isBatchAdd = new JCheckBox("批量添加", true);

	JButton clearButton = new JButton("清除");

	JCheckBox selectAll = new JCheckBox("全选", true);

	PackRecords records = PackRecords.loadRecords();
	
	static HashMap<String, String> publicLibraries=new HashMap<String, String>();//key 为库项目名称,value为库项目所在路径

	SVNHelper syncProjectConfigHelper;

	public static final int ACTION_FIND = 0;
	public static final int ACTION_RUN = 1;
	public static final int ACTION_REFRESH = 2;
	public static final int ACTION_HIDE_SHOW = 3;
	public static final int ACTION_SELECT_ALL = 4;// 全选
	public static final int ACTION_BATCH_SELECT = 5;// 批量增删
	public static final int ACTION_CLEAR = 6;

	public PackLauncher() {
		super("Android apk打包工具生成器v1.3  @author 13leaf");
		JPanel topPanel = new JPanel();
		QuickProjectFilter quickProjectFilter=new QuickProjectFilter();
		topPanel.setLayout(new GridLayout(0, 1));
		progressBar.setStringPainted(true);
		progressBar.setString("");
		topPanel.add(progressBar);
		topPanel.add(quickFilter);
		quickFilter.addKeyListener(quickProjectFilter);

		contentPane = new JScrollPane(contentBox);
		add(topPanel, BorderLayout.NORTH);
		add(contentPane, BorderLayout.CENTER);

		JPanel bottomTools = new JPanel();
		bottomTools.add(selectAll);
		selectAll.addActionListener(this);
		bottomTools.add(isBatchAdd);
		bottomTools.add(batchButton);
		batchButton.addActionListener(this);
		bottomTools.add(clearButton);
		clearButton.addActionListener(this);

		add(bottomTools, BorderLayout.SOUTH);

		setSize(430, 750);
		setLocation(200, 50);
		setVisible(true);
		addSystemHotKey();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		init();
		
		quickFilter.setText(records.getLatestSearch());
		quickProjectFilter.keyReleased(null);//trigger search
	}

	/**
	 * 读取project目录并完成初始化
	 */
	private void init() {
		contentBox.removeAll();// clear
		projectPanels.clear();// clear
		publicLibraries.clear();//clear

		File projectsDir = new File("projects");
		if (!projectsDir.exists())
			projectsDir.mkdir();
		File libraryDir=new File("libProjects");
		if(!libraryDir.exists())
			libraryDir.mkdir();

		syncProjectConfig();
		
		File[] libFiles=libraryDir.listFiles(new ProjectFileFilter());
		File[] projectFiles=projectsDir.listFiles(new ProjectFileFilter());

		List<File> projects=new ArrayList<File>();
		if(projectFiles!=null) projects.addAll(Arrays.asList(projectFiles));
		if(libFiles!=null) projects.addAll(Arrays.asList(libFiles));

		EditProjectHandler editHandler = new EditProjectHandler();
		for (File projectFile : projects) {
			if(projectFile.getName().equals(".project")) continue;//加强容错性
			BatchPack pack = new BatchPack(projectFile.getAbsolutePath());
			String fullName = projectFile.getName();
			String fileName = fullName.substring(0, fullName.lastIndexOf('.'));
			if (pack.needImport()) {
				batchButton.setEnabled(false);
				progressBar.setString("正在同步导入" + fileName);
				progressBar.setIndeterminate(true);
				pack.importFromSVN();
				records.setProjectVersion(projectFile.getAbsolutePath(),
						pack.getSVNVersion());// ensure latest version
				records.saveRecords();
				progressBar.setIndeterminate(false);
				progressBar.setString("");
				batchButton.setEnabled(true);
			}
			ProjectPanel projectPanel = new ProjectPanel(pack, fileName);
			projectPanel.setEditProjectListener(editHandler);
			projectPanel.updateUI();
			projectPanels.add(projectPanel);
			contentBox.add(projectPanel);
		}
		
		//prepare library
		for(ProjectPanel panel:projectPanels)
		{
			BatchPack pack=panel.getProjectBatch();
			if(!pack.isLibrary()) continue;
			Attributes attributes=pack.getAttributes();
			String projectPath=attributes.getProperty("projectPath");
			String projectName=new File(projectPath).getName();
			publicLibraries.put(projectName, projectPath);
		}
		//modify project to reference public library
		for(ProjectPanel panel: projectPanels)
		{
			prepareProjectPanel(panel);
		}
		contentBox.updateUI();
	}
	
	public static void prepareProjectPanel(ProjectPanel panel)
	{
		BatchPack pack=panel.getProjectBatch();
		if(pack.isLibrary() || pack.isDebugMode()) return;
		AndroidProject project=pack.getAttributes().getAndroidProject();
		ArrayList<String> libPaths=PackManager.getRelativeLibraryPaths(project.projectPath);
		for (String libPath : libPaths) {
			String configer=Files.readTextFile(new File(project.projectPath,"project.properties"), true);
			String keyName=new File(libPath).getName();
			String libraryPath=publicLibraries.get(keyName);
			if(libraryPath==null) {
				System.err.println("error: not found dependency library "+keyName);
				continue;
			}
			libraryPath=Files.convert2AbsolutePath("", libraryPath);
			try {
				String relativePath=Files.toRelativePath(new File(project.projectPath), new File(libraryPath));
				configer=configer.replace(libPath, relativePath);
				Files.saveTextFile(new File(project.projectPath,"project.properties"),configer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void syncProjectConfig() {
		if (syncProjectConfigHelper == null) {
			Attributes attributes = new Attributes();
			try {
				attributes.loadFromXML(new FileInputStream("config.xml"));
				if (attributes.containsKey(BatchPack.KEY_ACCOUNT)) {
					attributes.loadFromXML(new FileInputStream(Files
							.convert2AbsolutePath(".",
									attributes.getProperty(BatchPack.KEY_ACCOUNT))));
				}
				syncProjectConfigHelper= new SVNHelper(attributes.getProperty("projectConfigUrl"),
						attributes.getProperty("userName"),
						attributes.getProperty("password"));
				int version=records.getProjectVersion(PackRecords.CONFIG_PROJECT_ID);
				if(version!=syncProjectConfigHelper.getProjectVersion()){
					syncProjectConfigHelper.exportProject(new File("projects").getAbsolutePath());
					syncProjectConfigHelper.setProjectUrl(attributes.getProperty("libProjectConfigUrl"));
					syncProjectConfigHelper.exportProject(new File("libProjects").getAbsolutePath());
				}
				records.setProjectVersion(PackRecords.CONFIG_PROJECT_ID, syncProjectConfigHelper.getProjectVersion());
				records.saveRecords();
				System.out.println("import latest config ok");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addSystemHotKey() {
		JIntellitype myJIntellitype = null;
		try {
			myJIntellitype = JIntellitype.getInstance();
		} catch (Exception e) {
			return;
		}
		// add run hotkey
		myJIntellitype.registerHotKey(ACTION_FIND, JIntellitype.MOD_CONTROL,
				'F');
		myJIntellitype
				.registerHotKey(ACTION_RUN, JIntellitype.MOD_CONTROL, 'R');
		myJIntellitype.registerSwingHotKey(ACTION_REFRESH,
				JIntellitype.MOD_CONTROL, KeyEvent.VK_F5);
		myJIntellitype.registerHotKey(ACTION_HIDE_SHOW, JIntellitype.MOD_WIN,
				'P');
		myJIntellitype.registerHotKey(ACTION_SELECT_ALL,
				JIntellitype.MOD_CONTROL, 'A');
		myJIntellitype.registerHotKey(ACTION_BATCH_SELECT,
				JIntellitype.MOD_CONTROL, 'B');
		myJIntellitype.registerHotKey(ACTION_CLEAR, JIntellitype.MOD_CONTROL,
				'K');

		myJIntellitype.addHotKeyListener(new HotkeyListener() {
			@Override
			public void onHotKey(int key) {
				if (PackLauncher.this.isFocused()) {
					if (key == ACTION_FIND) {
						System.out.println("catch hot key find");
						quickFilter.requestFocus();
					}
					if (key == ACTION_REFRESH) {
						System.out.println("catch hot key refresh");
						init();
					}
					if (key == ACTION_RUN) {
						System.out.println("catch hot key run");
						runBatch();
					}

					if (key == ACTION_SELECT_ALL) {
						selectAll.setSelected(!selectAll.isSelected());
					}
					if (key == ACTION_BATCH_SELECT) {
						isBatchAdd.setSelected(!isBatchAdd.isSelected());
					}
					if (key == ACTION_CLEAR) {
						for (ProjectPanel panel : projectPanels) {
							panel.init();
						}
					}
				}
				if (key == ACTION_HIDE_SHOW) {
					PackLauncher.this.setVisible(!PackLauncher.this.isVisible());
				}
			}
		});

	}

	@Override
	public void dispose() {
		try {
			JIntellitype.getInstance().cleanUp();// release resource
		} catch (Exception e) {
			// ignore
		}
		super.dispose();
	}

	public void runBatch() {
		if (!batchButton.isEnabled())
			return;// check

		progressBar.setString("正在生成");
		progressBar.setIndeterminate(true);
		batchButton.setEnabled(false);// 开始生成
		new Thread() {
			public void run() {
				// save preference
				records.savePreference(isBatchAdd.isSelected(),
						selectAll.isSelected(), quickFilter.getText());

				// do batch work
				final StringBuffer results = new StringBuffer();
				for (ProjectPanel projectPanel : projectPanels) {
					if (projectPanel.wantBatch() && projectPanel.isVisible()) {
						BatchPack batchPack = projectPanel.getProjectBatch();
						batchPack
								.setOnBatchListener(new BatchPack.onBatchListener() {
									@Override
									public void onTaskSuccess(BatchPack batch,
											Map<String, String> runValue) {
										results.append(batch.getAttributes()
												.getProperty("projectName")
												+ "-" + runValue + "生成成功!\n");
									}

									@Override
									public void onTaskFail(BatchPack batch,
											Map<String, String> runValue) {
										results.append(batch.getAttributes()
												.getProperty("projectName") + "-"+ runValue+ "生成失败!\n");
									}
								});
						List<Map<String, String>> runValues = projectPanel
								.getRunValues();
						if(runValues.size()==0) runValues.add(new HashMap<String, String>());//at least 1,so we can package 1
						records.addMacroList(runValues);
						records.saveRecords();
						try {
							batchPack.doBatch(runValues);
						} catch (Exception e) {
							e.printStackTrace();
							results.append(e.getMessage()+"\n");
						}
					}
				}
				progressBar.setString("");
				progressBar.setIndeterminate(false);
				// end batch
				batchButton.setEnabled(true);
				JOptionPane.showMessageDialog(PackLauncher.this, results);
			}

		}.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == clearButton) {
			for (ProjectPanel panel : projectPanels) {
				panel.init();
			}
		}

		if (e.getSource() == batchButton) {
			runBatch();
		}

		if (e.getSource() == selectAll) {
			for (ProjectPanel projectPanel : projectPanels) {
				projectPanel.wantBatch.setSelected(selectAll.isSelected());
			}
		}
	}

	class QuickProjectFilter extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {
			// do the quick search
			for (ProjectPanel projectPanel : projectPanels) {
				String name = projectPanel.getProjectName();
				if (name.toLowerCase().contains(
						quickFilter.getText().toLowerCase())) {
					projectPanel.setVisible(true);
				} else {
					projectPanel.setVisible(false);
				}
			}
		}
	}

	class EditProjectHandler implements ProjectPanel.EditProjectListener {
		@Override
		public void editProject(ProjectPanel projectSource,
				MacroPanel macroSource, String editValue, int macroPanelIndex,
				int macroNameIndex) {
			if (!isBatchAdd.isSelected())
				return;
			// 标记所有其它的Project的为影子(非编辑)状态
			for (ProjectPanel projectPanel : projectPanels)
				projectPanel.isShadow = true;
			projectSource.isShadow = false;

			// synchronize shadow
			for (ProjectPanel projectPanel : projectPanels) {
				if (projectPanel.isShadow && projectPanel.wantBatch()
						&& projectPanel.isVisible()) {
					projectPanel.shadowMacro(macroPanelIndex, macroNameIndex,
							editValue);
				}
			}

		}

	}

	class ProjectFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".project");
		}

	}

	public static void main(String[] args) {
		new PackLauncher();
	}

}
