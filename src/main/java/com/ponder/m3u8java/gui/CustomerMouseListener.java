package com.ponder.m3u8java.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/14 10:47
 */
public class CustomerMouseListener extends MouseAdapter implements MouseListener {
    // 取得当前操作系统的双击时间长度参数
    private long LAST_CLICK_TIME;
    private long delay = 200;
    private DoubleClickListener doubleClickListener;

    public CustomerMouseListener(DoubleClickListener doubleClickListener){
        this.doubleClickListener = doubleClickListener;
    }

    @Override
    public void mouseClicked(MouseEvent e){
        long delta = System.currentTimeMillis() - LAST_CLICK_TIME;
        LAST_CLICK_TIME = System.currentTimeMillis();
        if(delta < 200){
            LAST_CLICK_TIME = 0;
            doubleClickListener.onDoubleClick(e);
        }
    }


    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
    }

    interface DoubleClickListener{
        void onDoubleClick(MouseEvent event);
    }

}
