package com.ponder.m3u8java.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/14 16:12
 */
public class SettingPop extends Pop {

    private JLabel jLabel;
    private JTextField path;
    private JButton jButton;

    public static final String event = "enter";
    public static final String key = "path";

    @Override
    void addCustomerView() {
        jLabel = new JLabel("保存路径");
        path = new JTextField();
        path.setPreferredSize(new Dimension(200,30));
        jButton = new JButton("确定");
        jPanel.add(jLabel);
        jPanel.add(path);
        jPanel.add(jButton);
        jButton.addActionListener(e->{
            map.put(key,path.getText());
            popCallback.callback(event,map);
        });
    }


}
