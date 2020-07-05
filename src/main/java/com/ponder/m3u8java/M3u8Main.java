package com.ponder.m3u8java;

import java.io.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class M3u8Main {
    static String host = "";

    public static void main(String[] args) throws IOException {
        InputStream index = ClassLoader.getSystemResourceAsStream("index1.m3u8");
        M3u8 m3u8 = new M3u8(index, host);
        m3u8.parse();
        m3u8.downloadBodies();
//        m3u8.downloadKey();
    }

}
