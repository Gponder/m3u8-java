package com.ponder.m3u8java.gui;

import com.ponder.m3u8java.M3u8Main;
import com.ponder.m3u8java.base.M3u8;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class GuiMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            new GuiMain().mainFrame();
        });
    }

    //主窗口
    private void mainFrame() {
        //主题 JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame jFrame = new JFrame("m3u8");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setBounds(200,200,400,300);
        addMenu(jFrame);
        jFrame.setVisible(true);
    }

    private void addMenu(JFrame jFrame){
        Menu menu = new Menu(jFrame);
        JMenu task = menu.addMenu("任务");
        JMenuItem newTask = menu.addMenuItem(task, "新建");

        newTask.addActionListener((event)->{
            showTaskPop();
        });
    }

    private void showTaskPop() {
        PopWindow pop = new PopWindow();
        pop.addTaskView((url, name)->{
            if (url==null){
                return;
            }
            downLoad(url,name);
        });
        pop.setVisible(true);
    }

    private void downLoad(String url, String name){
        try {
            M3u8 m3u8 = new M3u8(url);
            if (name!=null){
                m3u8.setName(name);
            }
            m3u8.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
