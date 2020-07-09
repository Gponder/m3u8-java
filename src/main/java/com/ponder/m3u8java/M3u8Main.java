package com.ponder.m3u8java;

import com.ponder.m3u8java.downloader.okhttp.DownLoadRunnable;

import java.io.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class M3u8Main {
    static String host = "https://baidu.com-ok-baidu.com/20191011/15291_b2977bc7/1000k/hls/";

    public static void main(String[] args) throws Exception {
        addExceptionHandler();
        InputStream index = ClassLoader.getSystemResourceAsStream("test.m3u8");
        M3u8 m3u8 = new M3u8(index, host);
        m3u8.download();
    }

    private static void addExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                DownLoadRunnable.executorService.shutdown();
            }
        });
    }

}
