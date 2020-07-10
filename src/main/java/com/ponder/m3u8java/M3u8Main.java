package com.ponder.m3u8java;

import com.ponder.m3u8java.base.M3u8;
import com.ponder.m3u8java.downloader.DownloadFactory;

import java.io.InputStream;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class M3u8Main {
    static String host = "https://baidu.com-ok-baidu.com/20191011/15291_b2977bc7/1000k/hls/";

    public static void main(String[] args) throws Exception {
        addExceptionHandler();
//        InputStream index = ClassLoader.getSystemResourceAsStream("test.m3u8");
//        M3u8 m3u8 = new M3u8(index, host);
//        M3u8 m3u8 = new M3u8("https://v8.yongjiu8.com/20180321/V8I5Tg8p/index.m3u8");
        M3u8 m3u8 = new M3u8("http://youku.cdn-iqiyi.com/20180523/11112_b1fb9d8b/index.m3u8");
//        M3u8 m3u8 = new M3u8("https://135zyv5.xw0371.com/2018/10/29/X05c7CG3VB91gi1M/playlist.m3u8");//会出错
        m3u8.download();
    }

    private static void addExceptionHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                DownloadFactory.closeThreadPool();
            }
        });
    }

}
