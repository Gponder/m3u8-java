package com.ponder.m3u8java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    /**
     * 存储视频分片
     * @param bodyBytes
     * @param tsFile
     * @throws IOException
     */
    public static void writeBodyBytesToFile(byte[] bodyBytes, File tsFile) throws IOException {
        File tsParentFile = new File(tsFile.getParent());
        if (!tsParentFile.exists())tsParentFile.mkdirs();
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        tsOutputStream.write(bodyBytes);
        tsOutputStream.flush();
        tsOutputStream.close();
    }

    /**
     * 因为java utf-8 解码 编码 非码区数据无法还原所以不能通过字符串编码还原数据
     * @param bodyString
     * @param tsFile
     * @throws IOException
     */
    private void writeBodyStringToFile(String bodyString,File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        tsOutputStream.write(bodyString.getBytes("UTF-8"));
        tsOutputStream.flush();
        tsOutputStream.close();
    }

    private void writeStreamToFile(InputStream is, File tsFile) throws IOException {
        FileOutputStream tsOutputStream = new FileOutputStream(tsFile);
        byte[] buffer = new byte[1024];
        int l;
        while ((l = is.available()) != 0){
            is.read(buffer);
            tsOutputStream.write(buffer,0,l);
        }
    }

}
