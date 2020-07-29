package com.ponder.m3u8java.gui.pop;

import com.ponder.m3u8java.base.M3u8;
import com.ponder.m3u8java.gui.pop.Pop;

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

    @Override
    void addCustomerView() {
        jLabel = new JLabel("保存路径");
        path = new JTextField();
        path.setText(M3u8.baseDir);
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
