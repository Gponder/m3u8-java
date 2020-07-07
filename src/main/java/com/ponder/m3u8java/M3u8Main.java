package com.ponder.m3u8java;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class M3u8Main {
    static String host = "https://baidu.com-ok-baidu.com/20191011/15291_b2977bc7/1000k/hls/";

    public static void main(String[] args) throws Exception {
        InputStream index = ClassLoader.getSystemResourceAsStream("test.m3u8");
        M3u8 m3u8 = new M3u8(index, host);
        m3u8.parse();
        m3u8.downloadBodies();

        File f = new File("C:\\Users\\gh\\Downloads\\6e8563db012000000.ts");
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);
        String s = new String(bytes,"UTF-8");
//        m3u8.getKey();
    }

}
