package com.ponder.m3u8java.gui;

import com.ponder.m3u8java.base.M3u8;
import com.ponder.m3u8java.config.Config;
import com.ponder.m3u8java.downloader.DownloadFactory;
import com.ponder.m3u8java.gui.pop.Pop;
import com.ponder.m3u8java.gui.pop.SettingPop;
import com.ponder.m3u8java.gui.pop.ThreadPop;
import com.ponder.m3u8java.util.Log;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class GuiMain {

    private final Vector<M3u8View> m3u8Vector = new Vector<>();
    private JList<M3u8View> jList;
    private boolean startAll = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            new GuiMain().mainFrame();
        });
    }

    //主窗口
    private void mainFrame() {
        //主题 JFrame.setDefaultLookAndFeelDecorated(true);
        Config.init();
        JFrame jFrame = new JFrame("m3u8");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setBounds(200,200,400,300);
        addMenu(jFrame);
        addList(jFrame);
        jFrame.setVisible(true);
    }

    private void addList(JFrame jFrame){
        jList = new JList<>();
        jList.setListData(m3u8Vector);
        jList.setCellRenderer(new ListCellRenderer<M3u8View>() {
            /**
             * 每次显示都会重新调用此方法,例如最小化后再次打开,被遮挡后点击任务栏图标重新显示
             * 所以必须跟android一样缓存视图
             * @param list
             * @param value
             * @param index
             * @param isSelected
             * @param cellHasFocus
             * @return
             */
            @Override
            public Component getListCellRendererComponent(JList<? extends M3u8View> list, M3u8View value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value.getView()==null){
                    JPanel jPanel = new JPanel();
                    jPanel.add(new JLabel(value.getM3u8().getName()));
                    JProgressBar jProgressBar = new JProgressBar();
                    value.getM3u8().setDownloadStateCallback(new M3u8.DownloadStateCallback() {
                        @Override
                        public void progress(int max, int current) {
                            jProgressBar.setMinimum(max);
                            jProgressBar.setMinimum(0);
                            jProgressBar.setValue(current);
                        }

                        @Override
                        public void complete(String path) {
                            if (startAll){
                                startDownLoad(index+1);
                            }
                        }
                    });
                    jPanel.add(jProgressBar);
                    value.setView(jPanel);
                }
                return value.getView();
            }
        });
        jList.addMouseListener(new CustomerMouseListener(new CustomerMouseListener.DoubleClickListener() {
            @Override
            public void onDoubleClick(MouseEvent event) {
                if (!startAll){
                    int index = jList.locationToIndex(event.getPoint());
                    startDownLoad(index);
                }
            }
        }));
        jFrame.add(jList, BorderLayout.CENTER);
    }

    private void startDownLoad(int index) {
        if (index>=m3u8Vector.size()){
            startAll=false;
            return;
        }
        try {
            m3u8Vector.get(index).getM3u8().download();
        }catch (IOException e){
            m3u8Vector.get(index).getView().setBackground(Color.RED);
            e.printStackTrace();
            Log.log(e.getMessage());
        }
    }

    private void addMenu(JFrame jFrame){
        Menu menu = new Menu(jFrame);

        JMenu task = menu.addMenu("任务");
        JMenuItem newTask = menu.addMenuItem(task, "新建");
        newTask.addActionListener((event)->{
            showTaskPop();
        });

        JMenu setting = menu.addMenu("设置");
        JMenuItem storePath = menu.addMenuItem(setting,"存储路径");
        storePath.addActionListener(e -> {
            showSettingPop();
        });

        JMenu thread = menu.addMenu("线程");
        JMenuItem count = menu.addMenuItem(thread,"线程数");
        count.addActionListener(e -> {
            showThreadPop();
        });

        JMenu start = menu.addMenu("开始全部");
        start.addActionListener(e -> {
            startAll = true;
            startDownLoad(0);
        });
    }

    private void showThreadPop() {
        ThreadPop threadPop = new ThreadPop();
        threadPop.setPopCallback(((event, data) -> {
            Object num = data.get(Pop.DEFAULT_KEY);
            for (char c:num.toString().toCharArray()){
                if (!Character.isDigit(c))return;
            }
            DownloadFactory.setPoolSize(Integer.parseInt(num.toString()));
        }));
        threadPop.setVisible(true);
    }

    private void showSettingPop() {
        SettingPop setting = new SettingPop();
        setting.setPopCallback(new Pop.PopCallback() {
            @Override
            public void callback(String event, Map<String, Object> data) {
                Config.setBaseDir(data.get(Pop.DEFAULT_KEY).toString());
            }
        });
        setting.showing();
    }

    private void showTaskPop() {
        PopWindow pop = new PopWindow();
        pop.addTaskView((url, name)->{
            if (TextUtils.isEmpty(url)){
                return;
            }
            addDownLoad(url,name);
        });
        pop.showing();
    }

    private void addDownLoad(String url, String name){
        try {
            M3u8 m3u8 = new M3u8(url);
            if (!TextUtils.isEmpty(name)){
                m3u8.setName(name);
            }
            m3u8Vector.add(new M3u8View(m3u8,null));
            jList.updateUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class M3u8View{
        private M3u8 m3u8;
        private Component view;

        public M3u8View(M3u8 m3u8, Component view) {
            this.m3u8 = m3u8;
            this.view = view;
        }

        public M3u8 getM3u8() {
            return m3u8;
        }

        public Component getView() {
            return view;
        }

        public void setView(Component view) {
            this.view = view;
        }
    }

}
