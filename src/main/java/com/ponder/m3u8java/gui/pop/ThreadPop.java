package com.ponder.m3u8java.gui.pop;

import javax.swing.*;
import java.awt.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/14 16:59
 */
public class ThreadPop extends Pop{

    private JLabel jLabel;
    private JTextField path;
    private JButton jButton;

    @Override
    void addCustomerView() {
        jLabel = new JLabel("线程数量");
        path = new JTextField();
        path.setPreferredSize(new Dimension(200,30));
        jButton = new JButton("确定");
        jPanel.add(jLabel);
        jPanel.add(path);
        jPanel.add(jButton);
        jButton.addActionListener(e->{
            setVisible(false);
            isShowing=false;
            map.put(DEFAULT_KEY,path.getText());
            popCallback.callback(DEFAULT_EVENT,map);
        });
    }
}
