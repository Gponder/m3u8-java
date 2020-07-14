package com.ponder.m3u8java.gui;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/14 16:16
 */
public abstract class Pop extends JDialog implements WindowListener {

    protected JPanel jPanel;
    protected PopCallback popCallback;
    protected Map<String,Object> map = new HashMap<>();
    public static boolean isShowing=false;

    public Pop() {
        this.jPanel = new JPanel();
        setContentPane(jPanel);
        setBounds(250, 250, 300, 150);
        addWindowListener(this);
    }

    public void showing(){
        if (!isShowing){
            isShowing=true;
            setVisible(true);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        isShowing=false;
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public void setPopCallback(PopCallback popCallback) {
        this.popCallback = popCallback;
    }

    abstract void addCustomerView();

    /**
     * 实现类添加事件标志字符串
     */
    public interface PopCallback{
        void callback(String event, Map<String,Object> data);
    }
}
