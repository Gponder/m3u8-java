package com.ponder.m3u8java;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
        m3u8.downloadBodies();
    }

}
