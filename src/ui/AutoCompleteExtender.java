package ui;

/*  
 * To change this template, choose Tools | Templates  
 * and open the template in the editor.  
 */   
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.JTextComponent;

import ui.AutoCompleteExtender.DataProvider.DataChangeListener;
   
/**  
 *  
 * @author Univasity  
 */   
public class AutoCompleteExtender {   
   
    public static final int DefaultMaxVisibleRows = 5;   
    /**  
     * 绑定的文本组件  
     */   
    private JTextComponent textComponent;   
    /**  
     * 用于显示结果列表的弹出菜单组件  
     */   
    private JPopupMenu popupMenu;   
    /**  
     * 用于展示匹配结果的列表组件  
     */   
    private JList resultList;   
    /**  
     * 为列表提供滚动支持的组件  
     */   
    private JScrollPane scrollPane;   
    /**  
     * 数据提供器  
     */   
    private DataProvider dataProvider;   
    /**  
     * 标记匹配数据是否发生改变  
     */   
    private boolean matchDataChanged;   
    /**  
     * 记录当前被匹配的文本  
     */   
    private String matchedText;   
    /**  
     * 原始的编辑文本  
     */   
    private String originalEditText;   
    /**  
     * 数据匹配器  
     */   
    private DataMatchHelper dataMatchHelper;   
    /**  
     * 确定监听器，默认为按下'回车键'或鼠标点选会被触发  
     */   
    private CommitListener commitListener;   
    
    /**  
     * 默认的数据改变监听器  
     */   
    private final DataChangeListener DefaultDataChangeListener = new DataChangeListener() {   
   
        public void dataChanged(int action, Object value) {   
            notifyDataChanged();   
        }   
    };   
    /**  
     * 线程池  
     */   
    private BlockingQueue<Runnable> queue; // 用于储存任务的队列   
    private ThreadPoolExecutor executor; // 线程池对象   
    private boolean matchDataAsync = false; // 是否异步进行匹配操作   
    private int resultListWidth;   
    private boolean widthWithScrollBar;   
    private boolean autoSizeToFit;   
   
    /**  
     * 指定绑定的对象，数据提供器和匹配器来构建一个对象  
     * @param textComponent 不能为null  
     * @param dataProvider 不能为null  
     * @param dataMatchHelper 如果为null，则使用默认的匹配器  
     */   
    public AutoCompleteExtender(JTextComponent textComponent, DataProvider dataProvider, DataMatchHelper dataMatchHelper) {   
        if (textComponent == null) {   
            /**  
             * 确保绑定的插件不为null  
             */   
            throw new IllegalArgumentException("textComponent不能为null!");   
        }   
        if (dataProvider == null) {   
            /**  
             * 确保数据提供器不为null  
             */   
            throw new IllegalArgumentException("dataProvider不能为null!");   
        }   
        this.textComponent = textComponent;   
        this.dataProvider = dataProvider;   
        this.dataProvider.setDataChangeListener(DefaultDataChangeListener);   
        if (dataMatchHelper == null) {   
            this.dataMatchHelper = new DefaultDataMatchHelper();   
        } else {   
            this.dataMatchHelper = dataMatchHelper;   
        }   
        /**  
         * 初始化数据  
         */   
        resetAll();   
    }   
   
    /**  
     * 指定绑定的对象，匹配数据和匹配器来构建一个对象  
     * @param textComponent 不能为null  
     * @param data 初始的匹配数据  
     * @param dataMatchHelper 如果为null，则使用默认的匹配器  
     */   
    public AutoCompleteExtender(JTextComponent textComponent, Object[] data, DataMatchHelper dataMatchHelper) {   
        if (textComponent == null) {   
            /**  
             * 确保绑定的插件不为null  
             */   
            throw new IllegalArgumentException("textComponent不能为null!");   
        }   
        this.textComponent = textComponent;   
        this.dataProvider = new DefaultDataProvider();   
        if (data != null) {   
            for (Object value : data) {   
                this.dataProvider.appendData(value);   
            }   
        }   
        this.dataProvider.setDataChangeListener(DefaultDataChangeListener);   
        if (dataMatchHelper == null) {   
            this.dataMatchHelper = new DefaultDataMatchHelper();   
        } else {   
            this.dataMatchHelper = dataMatchHelper;   
        }   
        /**  
         * 初始化数据  
         */   
        resetAll();   
    }   
   
    public DataProvider getDataProvider() {   
        return dataProvider;   
    }   
   
    /**  
     * 设置为默认配置，原有数据将被清空  
     */   
    public synchronized void resetAll() {   
        initTextComponent();   
        initResultList();   
        initValues();   
        setFocusOnTextComponent();   
        updateUI();   
    }   
   
    /**  
     * 刷新当前UI  
     */   
    public synchronized void updateUI() {   
        popupMenu.pack();   
        popupMenu.updateUI();   
    }   
   
    /**  
     * 清空匹配结果  
     */   
    public synchronized void clearMatchResult() {   
        collapse();   
        if (queue != null) {   
            queue.clear();   
        }   
        ((DefaultListModel) resultList.getModel()).removeAllElements();   
    }   
   
    /**  
     * 标记匹配数据改变了  
     */   
    private void notifyDataChanged() {   
        matchDataChanged = true;   
    }   
   
    public void setCommitListener(CommitListener commitListener) {   
        this.commitListener = commitListener;   
    }   
   
    /**  
     * 获取当前被匹配的文本  
     * @return  
     */   
    public synchronized String getMatchText() {   
        return matchedText;   
    }   
   
    /**  
     * 获取当前匹配结果  
     * @return  
     */   
    public synchronized Object[] getMatchResult() {   
        return ((DefaultListModel) resultList.getModel()).toArray();   
    }   
   
    /**  
     * 获取当前选中的值  
     * @return  
     */   
    public synchronized Object getSelectedValue() {   
        return resultList.getSelectedValue();   
    }   
   
    /**  
     * 确定指定的文本为最终选定  
     * @param text  
     */   
    public synchronized void commitText(String text) {   
        originalEditText = text;   
        textComponent.setText(text);   
        if (commitListener != null) {   
            commitListener.commit(text);   
        }   
    }   
   
    /**  
     * 获取当前选中项的索引值  
     * @return  
     */   
    public synchronized int getSelectedIndex() {   
        return resultList.getSelectedIndex();   
    }   
   
    /**  
     * 选中指定的索引值  
     * @param index  
     */   
    public synchronized void setSelectedIndex(int index) {   
        if (index < 0 || index >= getResultCount()) {   
            return;   
        }   
        resultList.setSelectedIndex(index);   
        // 使选中项处于可视范围内   
        resultList.ensureIndexIsVisible(index);   
    }   
   
    /**  
     * 打开结果列表(如果未成匹配，则自动执行匹配处理，如果无有效结果则不会被展开)(焦点会转移到列表)  
     * @return  
     */   
    public synchronized boolean expand() {   
        if (!hasMatched()) {   
            if (doMatch()) {   
                // 展开列表   
                updateExpandListUI();   
                popupMenu.show(textComponent, 0, textComponent.getHeight());   
            }   
        } else if (getResultCount() > 0) {   
            popupMenu.setVisible(true);   
        }   
        return popupMenu.isVisible();   
    }   
   
    /**  
     * 关闭结果列表(数据不会被清空，再次打开时直接重新显示)  
     */   
    public synchronized void collapse() {   
        removeSelectionInterval();   
        popupMenu.setVisible(false);   
    }   
   
    /**  
     * 判断结果列表是否被打开  
     * @return  
     */   
    public synchronized boolean isExpanded() {   
        return popupMenu.isVisible();   
    }   
   
    /**  
     * 获取当前结果列表的条目数  
     * @return  
     */   
    public synchronized int getResultCount() {   
        return ((DefaultListModel) resultList.getModel()).getSize();   
    }   
   
    /**  
     * 获取一次最多的显示行数(超出的部分需通过拖动滚动条显示)  
     * @param rows  
     */   
    public synchronized void setMaxVisibleRows(int rows) {   
        resultList.setVisibleRowCount(rows);   
    }   
   
    /**  
     * 把焦点设置到文本编辑框上  
     */   
    public synchronized void setFocusOnTextComponent() {   
        textComponent.requestFocus();   
    }   
   
    /**  
     * 把焦点设置到结果列表上  
     */   
    public synchronized void setFocusOnExpandList() {   
        resultList.requestFocus();   
    }   
   
    /**  
     * 判断焦点是否在文本编辑框上  
     * @return  
     */   
    public synchronized boolean isFocusOnTextComponent() {   
        return textComponent.isFocusOwner();   
    }   
   
    /**  
     * 判断焦点是否在结果列表上  
     * @return  
     */   
    public synchronized boolean isFocusOnExpandList() {   
        return resultList.isFocusOwner();   
    }   
   
    /**  
     * 取消当前列表上的选中状态(使selectedIndex==-1)  
     */   
    public synchronized void removeSelectionInterval() {   
        final int selectedIndex = resultList.getSelectedIndex();   
        resultList.removeSelectionInterval(selectedIndex, selectedIndex);   
    }   
   
    /**  
     * 判断是否已经匹配过了（匹配前应进行检测，避免重复匹配操作）  
     * @return  
     */   
    public synchronized boolean hasMatched() {   
        if (matchDataChanged) {   
            return false;   
        }   
        if (matchedText == null || matchedText.length() < 1) {   
            return false;   
        }   
        String text = textComponent.getText();   
        if (text == null || !text.equals(matchedText)) {   
            return false;   
        }   
        return true;   
    }   
   
    /**  
     * 执行匹配操作  
     * @return  
     */   
    public synchronized boolean doMatch() {   
        // 清空原有结果   
        clearMatchResult();   
   
        matchedText = textComponent.getText();   
        originalEditText = matchedText;   
        String keyWord = matchedText;   
        if (keyWord != null) {   
            keyWord = matchedText.trim();   
        }   
   
        if (dataMatchHelper != null) {   
            if (!dataMatchHelper.isMatchTextAccept(keyWord)) {   
                return false;   
            }   
        }   
   
        if (matchDataAsync) {   
            doMatchAsync(keyWord);   
            matchDataChanged = false;   
            return true;   
        } else {   
            doMatchSync(keyWord);   
            matchDataChanged = false;   
            return getResultCount() > 0;   
        }   
    }   
   
    /**  
     * 设置异步匹配数据  
     * @param async  
     */   
    public synchronized void setMatchDataAsync(boolean async) {   
        if (this.matchDataAsync != async) {   
            this.matchDataAsync = async;   
            if (async) {   
                queue = new LinkedBlockingQueue<Runnable>();   
                // 创建一个最多运行2个任务，支持10个任务， 允许延时20秒的线程池   
                executor = new ThreadPoolExecutor(2, 10, 20, TimeUnit.SECONDS, queue);   
            } else {   
                if (queue != null) {   
                    queue.clear();   
                }   
                if (executor != null) {   
                    executor.shutdown();   
                }   
                queue = null;   
                executor = null;   
            }   
        }   
    }   
   
    /**  
     * 判断当前是否异步匹配  
     * @return  
     */   
    public synchronized boolean isMatchDataAsync() {   
        return this.matchDataAsync;   
    }   
   
    /**  
     * 在结果列表上显示过于选中项的提示条  
     * @param asNeed 是否根据需要显示（true->文本长度超出显示范围时才显示）  
     */   
    public synchronized void showToolTipsWithSelectedValue(boolean asNeed) {   
        Object value = resultList.getSelectedValue();   
        if (value != null) {   
            // 显示提示   
            String txt = value.toString();   
            if (txt != null) {   
                if (asNeed) {   
                    // 超出范围才显示提示   
                    int txtW = SwingUtilities.computeStringWidth(resultList.getFontMetrics(resultList.getFont()), txt);   
                    if (txtW >= resultList.getFixedCellWidth()) {   
                        resultList.setToolTipText(txt);   
                        return;   
                    }   
                } else {   
                    resultList.setToolTipText(txt);   
                    return;   
                }   
            }   
        }   
        resultList.setToolTipText(null);   
    }   
   
    /**  
     * 在结果列表上显示指定的文本作为提示  
     * @param text  
     */   
    public void showToolTips(String text) {   
        if (text != null) {   
            resultList.setToolTipText(text);   
        } else {   
            resultList.setToolTipText(null);   
        }   
    }   
   
    /**  
     * 获取一次最多可见行数  
     * @return  
     */   
    public synchronized int getMaxVisibleRows() {   
        return resultList.getVisibleRowCount();   
    }   
   
    /**  
     * 获取结果列表单元项的宽度  
     * @return  
     */   
    public synchronized int getListCellWidth() {   
        return resultList.getFixedCellWidth();   
    }   
   
    /**  
     * 获取结果列表单元项的高度  
     * @return  
     */   
    public synchronized int getListCellHeight() {   
        return resultList.getFixedCellHeight();   
    }   
   
    public synchronized void setListCellSize(int cellWidth, int cellHeight) {   
        resultList.setFixedCellWidth(cellWidth);   
        resultList.setFixedCellHeight(cellHeight);   
        autoSizeToFit = false;   
        updateExpandListUI();   
    }   
   
    public synchronized void setListWidth(int width, boolean withScrollBar) {   
        this.resultListWidth = width;   
        this.widthWithScrollBar = withScrollBar;   
        autoSizeToFit = false;   
        updateExpandListUI();   
    }   
   
    /**  
     * 使大小贴合组件  
     */   
    public synchronized void setSizeFitComponent() {   
        autoSizeToFit = true;   
        updateExpandListUI();   
    }   
   
    /**  
     * 指定点是否在文本框范围内  
     * @param p  
     * @return  
     */   
    public synchronized boolean isTextFieldContains(Point p) {   
        if (p == null) {   
            return false;   
        }   
        return textComponent.contains(p);   
    }   
   
    /**  
     * 指定点是否在结果列表范围内  
     * @param p  
     * @return  
     */   
    public synchronized boolean isExpandListContains(Point p) {   
        if (p == null) {   
            return false;   
        }   
        return resultList.contains(p);   
    }   
   
    private synchronized void initTextComponent() {   
        textComponent.setVisible(true);   
        textComponent.setEnabled(true);   
        textComponent.setEditable(true);   
        // 必须先删除再添加，否则会重复....   
        textComponent.removeKeyListener(DefaultTextFieldKeyAdapter);   
        textComponent.addKeyListener(DefaultTextFieldKeyAdapter);   
    }   
   
    private synchronized void initResultList() {   
        /**  
         * list  
         */   
        if (resultList != null) {   
            resultList.removeAll();   
        } else {   
            resultList = new JList(new DefaultListModel());   
            resultList.addMouseListener(DefaultResultListMouseAdapter);   
            resultList.addMouseMotionListener(DefaultResultListMouseMotionAdapter);   
        }   
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   
        resultList.setVisibleRowCount(DefaultMaxVisibleRows);   
        // 允许提示框   
        ToolTipManager.sharedInstance().registerComponent(resultList);   
   
        /**  
         * scroll pane  
         */   
        if (scrollPane == null) {   
            scrollPane = new JScrollPane(resultList);   
        }   
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);   
   
        /**  
         * popup menu  
         */   
        if (popupMenu == null) {   
            popupMenu = new JPopupMenu();   
        }   
        popupMenu.add(scrollPane);   
        popupMenu.setVisible(false);   
        popupMenu.setFocusable(false);   
        popupMenu.setBorder(BorderFactory.createEmptyBorder()); // 去掉边框   
    }   
   
    private synchronized void initValues() {   
        setCommitListener(null);   
   
        matchedText = null;   
        matchDataChanged = true;   
        this.matchDataAsync = false;   
        originalEditText = textComponent.getText();   
    }   
   
    /**  
     * 根据给定的值执行匹配操作(该操作为异步的)  
     * @param content  
     * @return  
     */   
    private synchronized void doMatchAsync(String content) {   
        final String matchText = content;   
   
        if (queue != null) {   
            queue.clear();   
        }   
   
        executor.execute(new Runnable() {   
   
            public void run() {   
                /**  
                 * 进行匹配  
                 */   
                doMatchInner(matchText);   
                /**  
                 * 如果无匹配项，关闭当前显示  
                 */   
                if (getResultCount() > 0) {   
                    updateExpandListUI();   
                } else {   
                    collapse();   
                }   
            }   
        });   
    }   
   
    /**  
     * 根据给定的值执行匹配操作(该操作为同步的)  
     * @param content  
     * @return  
     */   
    private synchronized void doMatchSync(String content) {   
        /**  
         * 进行匹配  
         */   
        doMatchInner(content);   
    }   
   
    /**  
     * 处理匹配(内部调用)  
     * @param matchText  
     */   
    private void doMatchInner(String matchText) {   
        if (dataProvider != null) {   
            DefaultListModel listModel = (DefaultListModel) resultList.getModel();   
            for (Object value : dataProvider.toArray()) {   
                if (dataMatchHelper != null) {   
                    if (dataMatchHelper.isDataMatched(matchText, value)) {   
                        listModel.addElement(value);   
                    }   
                } else {   
                    // 直接添加   
                    listModel.addElement(value);   
                }   
            }   
        }   
    }   
   
    /**  
     * 设置当前选项为最终选定值  
     */   
    private void commitTextBySelectedValue() {   
        Object value = getSelectedValue();   
        if (value != null) {   
            commitText(value.toString());   
        }   
        collapse();   
    }   
   
    /**  
     * 转移焦点到文本编辑框，并关闭结果列表  
     */   
    private void changeFocusToTextField() {   
        // 取消选择   
        removeSelectionInterval();   
        // 转移焦点到文本框   
        setFocusOnTextComponent();   
        // 设置为原本编辑的文本值   
        textComponent.setText(originalEditText);   
    }   
   
    /**  
     * 设置当前选中项的值到文本框  
     */   
    private void showCurrentSelectedValue() {   
        Object value = getSelectedValue();   
        if (value != null) {   
            textComponent.setText(value.toString());   
        }   
    }   
   
    /**  
     * 刷新结果列表的显示(焦点会转移到列表)  
     */   
    private synchronized void updateExpandListUI() {   
        DefaultListModel listModel = (DefaultListModel) resultList.getModel();   
        int dataSize = listModel.getSize();   
   
        int preferredWidth = 0;   
        if (autoSizeToFit) {   
            /**  
             * 自动使大小贴合组件  
             */   
            resultList.setFixedCellWidth(textComponent.getWidth());   
            resultList.setFixedCellHeight(textComponent.getHeight());   
            preferredWidth = textComponent.getWidth();   
            if (dataSize > resultList.getVisibleRowCount()) {   
                preferredWidth += scrollPane.getVerticalScrollBar().getPreferredSize().width;   
            }   
        } else {   
            /**  
             * 使用自定义的大小  
             */   
            preferredWidth = resultListWidth;   
            if (dataSize > resultList.getVisibleRowCount()) {   
                if (!widthWithScrollBar) {   
                    preferredWidth += scrollPane.getVerticalScrollBar().getPreferredSize().width;   
                }   
            }   
        }   
   
        int preferredHeight = Math.min(resultList.getVisibleRowCount(), dataSize) * resultList.getFixedCellHeight() + 3; // 多预留一些空间，这个值可自己调整不是很准的   
   
        scrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));   
        resultList.updateUI();   
        popupMenu.pack();   
    }   
    /**  
     * 默认提供的结果列表上鼠标运动事件处理器  
     */   
    private MouseMotionAdapter DefaultResultListMouseMotionAdapter = new MouseMotionAdapter() {   
   
        @Override   
        public void mouseMoved(MouseEvent e) {   
            /**  
             * 该操作结果是:  
             * 选中鼠标所在选项，并显示提示  
             */   
            Point p = e.getPoint();   
            if (isExpandListContains(p)) {   
                /**  
                 * 鼠标在列表区域内移动时  
                 */   
                int index = p.y / getListCellHeight();   
                // 光标跟随   
                setSelectedIndex(index);   
                // 文本超长时显示提示   
                showToolTipsWithSelectedValue(true);   
                // 焦点回归到文本编辑框   
                setFocusOnTextComponent();   
            }   
        }   
    };   
    /**  
     * 默认提供的结果列表上鼠标按键事件处理器  
     */   
    private final MouseAdapter DefaultResultListMouseAdapter = new MouseAdapter() {   
   
        @Override   
        public void mouseClicked(MouseEvent e) {   
            /**  
             * 该操作结果是:  
             * 设置编辑框文字为选中项，关闭结果列表，焦点回到编辑框，同时触发commit监听器  
             */   
            Point p = e.getPoint();   
            if (isExpandListContains(p)) {   
                /**  
                 * 鼠标点击列表项时  
                 */   
                int index = p.y / getListCellHeight();   
                // 选中该项   
                setSelectedIndex(index);   
                //    
                if (getSelectedIndex() == index) {   
                    commitTextBySelectedValue();   
                }   
                // 焦点回归到文本编辑框   
                setFocusOnTextComponent();   
            }   
        }   
    };   
    /**  
     * 默认提供的文本编辑框上键盘按键事件处理器  
     */   
    private final KeyAdapter DefaultTextFieldKeyAdapter = new KeyAdapter() {   
   
        @Override   
        public void keyPressed(KeyEvent e) {   
            /**  
             * 只对处于当前焦点时作处理  
             */   
            if (!e.getComponent().isFocusOwner()) {   
                return;   
            }   
   
            switch (e.getKeyCode()) {   
   
                case KeyEvent.VK_ENTER:   
                    /**  
                     * 该操作结果是:  
                     * 设置编辑框文字为选中项，关闭结果列表，焦点回到编辑框，同时触发commit监听器  
                     */   
                    commitTextBySelectedValue();   
                    break;   
   
                case KeyEvent.VK_DOWN:   
                    /**  
                     * 该操作结果是:  
                     * 1.如果结果列表未打开，打开结果列表，并选中第一项，设置编辑框文字  
                     * 2.如果当前选中项为最后一项，让焦点回到编辑框  
                     * 3.否则，下移选项，并改变编辑框文字为当前选项  
                     */   
                    if (isExpanded()) {   
                        /**  
                         * 如果列表处于展开状态  
                         */   
                        final int selectedIndex = getSelectedIndex();   
                        if (selectedIndex == getResultCount() - 1) {   
                            /**  
                             * 并且选中项为最后一项  
                             */   
                            // 将焦点集中到文本框   
                            changeFocusToTextField();   
                        } else {   
                            /**  
                             * 否则  
                             */   
                            // 下移一项   
                            setSelectedIndex(selectedIndex + 1);   
                            showCurrentSelectedValue();   
                            setFocusOnTextComponent();   
                        }   
                    } else {   
                        if (expand()) {   
                            /**  
                             * 成功打开结果列表  
                             */   
                            // 选中第一项   
                            setSelectedIndex(0);   
                        }   
                    }   
                    break;   
   
                case KeyEvent.VK_UP:   
                    /**  
                     * 该操作结果是:  
                     * 1.如果结果列表未打开，打开结果列表，并选中最后一项，设置编辑框文字  
                     * 2.如果当前选中项为第一项，让焦点回到编辑框  
                     * 3.否则，上移选项，并改变编辑框文字为当前选项  
                     */   
                    if (isExpanded()) {   
                        /**  
                         * 如果列表处于展开状态  
                         */   
                        final int selectedIndex = getSelectedIndex();   
                        if (selectedIndex == 0) {   
                            /**  
                             * 并且选中项为第一项  
                             */   
                            // 将焦点集中到文本框   
                            changeFocusToTextField();   
                        } else {   
                            /**  
                             * 否则  
                             */   
                            if (selectedIndex == -1) {   
                                // 移到最后一项   
                                setSelectedIndex(getResultCount() - 1);   
                            } else {   
                                // 上移一项   
                                setSelectedIndex(selectedIndex - 1);   
                            }   
                            showCurrentSelectedValue();   
                        }   
                    } else {   
                        if (expand()) {   
                            /**  
                             * 成功打开结果列表  
                             */   
                            // 选中最后一项   
                            setSelectedIndex(getResultCount() - 1);   
                        }   
                    }   
                    break;   
   
                case KeyEvent.VK_LEFT:   
                case KeyEvent.VK_RIGHT: // 左右的操作相同   
                    /**  
                     * 该操作结果是:  
                     * 设置编辑文字为选中项，并关闭结果列表，焦点回到编辑框  
                     */   
                    if (isExpanded()) {   
                        /**  
                         * 如果列表处于展开状态  
                         */   
                        if (getSelectedIndex() != -1) {   
                            /**  
                             * 并且有选项被选中了  
                             */   
                            showCurrentSelectedValue();   
                        }   
                        collapse();   
                    }   
                    // 转移焦点到文本编辑框   
                    changeFocusToTextField();   
                    break;   
            }   
            /**  
             * 为了确保焦点始终处于编辑框  
             */   
            setFocusOnTextComponent();   
        }   
   
        @Override   
        public void keyReleased(KeyEvent e) {   
            if (!e.getComponent().isFocusOwner()) {   
                return;   
            }   
   
            int keyCode = e.getKeyCode();   
            if (keyCode == KeyEvent.VK_UP   
                    || keyCode == KeyEvent.VK_DOWN   
                    || keyCode == KeyEvent.VK_LEFT   
                    || keyCode == KeyEvent.VK_RIGHT   
                    || keyCode == KeyEvent.VK_ENTER /*|| keyCode == KeyEvent.VK_BACK_SPACE*/) {   
                return;   
            }   
            /**  
             * 打开结果列表  
             */   
            expand();   
            /**  
             * 为了确保焦点始终处于编辑框  
             */   
            setFocusOnTextComponent();   
        }   
    };   
   
    /*********************************************************  
     *                 定义的一些接口  
     */   
    public interface CommitListener {   
   
        public void commit(String value);   
    }   
   
    /**  
     * 数据提供接口  
     * @author Univasity  
     */   
    public interface DataProvider {   
   
        public Object getData(int index);   
   
        public void appendData(Object value);   
   
        public void insertData(int index, Object value);   
   
        public void replaceData(int index, Object value);   
   
        public void replaceData(Object oldValue, Object newValue);   
   
        public void removeDataAt(int index);   
   
        public void removeData(Object value);   
   
        public void clear();   
   
        public int getSize();   
   
        public Object[] toArray();   
   
        public void setDataChangeListener(DataChangeListener listener);   
   
        /**  
         * 数据改变监听接口  
         */   
        public interface DataChangeListener {   
   
            public static final int APPEND = 1;   
            public static final int INSERT = 2;   
            public static final int REPLACE = 3;   
            public static final int REMOVE = 4;   
            public static final int CLEAR = 5;   
   
            public void dataChanged(int action, Object value);   
        }   
    }   
   
    public interface DataMatchHelper {   
   
        /**  
         * 判断指定的用于匹配文本是否被允许  
         * @param text  
         * @return  
         */   
        public boolean isMatchTextAccept(String text);   
   
        /**  
         * 判断给定的值是否与文本值匹配  
         * @param matchedText  
         * @param data  
         * @return  
         */   
        public boolean isDataMatched(String matchText, Object data);   
    }   
   
    /*********************************************************  
     *                       默认的实现  
     */   
    private class DefaultDataProvider implements DataProvider {   
   
        private ArrayList data;   
        private DataChangeListener listener;   
   
        public DefaultDataProvider() {   
            data = new ArrayList();   
        }   
   
        public synchronized Object getData(int index) {   
            return data.get(index);   
        }   
   
        public synchronized void appendData(Object value) {   
            if (data.add(value)) {   
                if (listener != null) {   
                    listener.dataChanged(DataChangeListener.APPEND, value);   
                }   
            }   
        }   
   
        public synchronized void insertData(int index, Object value) {   
            data.add(index, value);   
            if (listener != null) {   
                listener.dataChanged(DataChangeListener.INSERT, value);   
            }   
        }   
   
        public synchronized void replaceData(int index, Object value) {   
            if (data.set(index, value).equals(value)) {   
                if (listener != null) {   
                    listener.dataChanged(DataChangeListener.REPLACE, value);   
                }   
            }   
        }   
   
        public synchronized void replaceData(Object oldValue, Object newValue) {   
            int index = data.indexOf(oldValue);   
            if (data.set(index, newValue).equals(newValue)) {   
                if (listener != null) {   
                    listener.dataChanged(DataChangeListener.REPLACE, newValue);   
                }   
            }   
        }   
   
        public synchronized void removeDataAt(int index) {   
            Object value = data.get(index);   
            data.remove(index);   
            if (listener != null) {   
                listener.dataChanged(DataChangeListener.REMOVE, value);   
            }   
        }   
   
        public synchronized void removeData(Object value) {   
            if (data.remove(value)) {   
                if (listener != null) {   
                    listener.dataChanged(DataChangeListener.REMOVE, value);   
                }   
            }   
        }   
   
        public synchronized void clear() {   
            data.clear();   
            if (listener != null) {   
                listener.dataChanged(DataChangeListener.CLEAR, null);   
            }   
        }   
   
        public synchronized int getSize() {   
            return data.size();   
        }   
   
        public synchronized Object[] toArray() {   
            return data.toArray();   
        }   
   
        public synchronized void setDataChangeListener(DataChangeListener listener) {   
            this.listener = listener;   
        }   
    }   
   
    /**  
     * 默认的数据匹配助手  
     */   
    private class DefaultDataMatchHelper implements DataMatchHelper {   
   
        public boolean isMatchTextAccept(String text) {   
            return (text != null && text.length() > 0);   
        }   
   
        public boolean isDataMatched(String matchText, Object value) {   
            if (value != null && value.toString().indexOf(matchText) != -1) {   
                return true;   
            }   
            return false;   
        }   
    }   
}  
