package com.ponder.m3u8java.gui;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/13 14:57
 */
public class Menu {

    private JFrame jFrame;
    private JMenuBar jMenuBar;

    public Menu(JFrame jFrame) {
        this.jFrame = jFrame;
        jMenuBar = new JMenuBar();
        jFrame.setJMenuBar(jMenuBar);
    }

    public JMenu addMenu(String menu){
        JMenu jMenu = new JMenu(menu);
        jMenuBar.add(jMenu);
        return jMenu;
    }

    public JMenu addMenu(JMenu jMenu){
        jMenu.setMnemonic(KeyEvent.VK_T);
        jMenuBar.add(jMenu);
        return jMenu;
    }

    public JMenuItem addMenuItem(JMenu jMenu, JMenuItem jMenuItem){
        jMenu.add(jMenuItem);
        return jMenuItem;
    }

    public JMenuItem addMenuItem(JMenu jMenu, String menuItem){
        JMenuItem jMenuItem = new JMenuItem(menuItem);
        jMenu.add(jMenuItem);
        return jMenuItem;
    }

}
