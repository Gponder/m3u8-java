package com.ponder.m3u8java.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/13 16:42
 */
public class PopWindow extends JDialog {

    //弹出窗口的内容
    private final JPanel panel;
    private AddTaskView addTaskView;

    public PopWindow() {
        panel = new JPanel();
        setContentPane(panel);
        setBounds(250, 250, 300, 150);
    }

    public void addTaskView(AddCallback addCallback) {
        this.addTaskView = new AddTaskView();
        addTaskView.addToPanel(panel);
        addTaskView.addCallback(addCallback);
    }

    interface AddCallback{
        void added(String url,String name);
    }

    class AddTaskView {

        private final JLabel urlLabel;
        private final JTextField urlField;
        private final JLabel fileLabel;
        private final JTextField fileField;
        private final JButton jButton;

        public AddTaskView() {
            urlLabel = new JLabel("m3u8  地址:");
            urlField = new JTextField();//"请出入m3u8地址"
            urlField.setPreferredSize(new Dimension(200, 30));
            fileLabel = new JLabel("保存文件名:");
            fileField = new JTextField();//"请输入保存文件名"
            fileField.setPreferredSize(new Dimension(200, 30));
            jButton = new JButton("添加");
        }

        public void addToPanel(JPanel panel) {
            panel.add(urlLabel);
            panel.add(urlField);
            panel.add(fileLabel);
            panel.add(fileField);
            panel.add(jButton);
        }

        public void addCallback(AddCallback addCallback) {
            jButton.addActionListener((event) -> {
                String url = urlField.getText().trim();
                String name = fileField.getText().trim();
                addCallback.added(url, name);
                setVisible(false);
            });
        }
    }
}