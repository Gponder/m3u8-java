package com.ponder.m3u8java;

import java.io.*;

/**
 * @auth ponder
 * @Email gponder.g@gmail.com
 * @create 2020/7/4 17:08
 */
public class M3u8Main {


    public static void main(String[] args) throws IOException {

        InputStream index = ClassLoader.getSystemResourceAsStream("index1.m3u8");
        BufferedReader reader = new BufferedReader(new InputStreamReader(index));
        String line;
        while ((line = reader.readLine())!=null) {
            System.out.println(line);
        }

    }

}
